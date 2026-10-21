import { Component, OnInit, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, ReactiveFormsModule, Validators } from '@angular/forms';
import { ClarityModule } from '@clr/angular';
import { IncidenciasService } from '../../services/incidencias.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

// Subcomponentes y Modales
import { TipoPrivacidadComponent } from '../../components/tipo-privacidad/tipo-privacidad.component';
import { DetalleIncidenciaComponent } from '../../components/detalle-incidencia/detalle-incidencia.component';
import { ModalCargaArchivosComponent } from '../../../../shared/components/modal-carga-archivos/modal-carga-archivos.component';
import { fechaRangoIncidenciaValidator } from '../../../../shared/validators/fecha-rango.validator';

@Component({
  selector: 'app-registro-incidencia',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ClarityModule,
    RouterLink,
    TipoPrivacidadComponent,
    DetalleIncidenciaComponent,
    ModalCargaArchivosComponent
  ],
  templateUrl: './registro-incidencia.component.html',
  styleUrls: ['./registro-incidencia.component.scss']
})
export class RegistroIncidenciaComponent implements OnInit {
  incidenciaForm!: FormGroup;
  files: File[] = [];
  archivosExistentes: any[] = [];
  isSubmitting = false;

  incidenciaId: string | null = null;
  isReadOnly: boolean = false;
  isLoading: boolean = false;
  isApelacion: boolean = false;
  motivosRechazoFeedback: any[] = [];
  cargandoFeedback: boolean = false;
  contadorOriginal: string = '';
  incidenciaEstado: string = '';

  // Control de modal de carga de archivos desacoplada
  modalCargaArchivosOpen = false;
  archivosStaged: File[] = [];

  private fb = inject(FormBuilder);
  private incidenciasService = inject(IncidenciasService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);

  readonly dashboardUrl = computed(() => this.authService.getDashboardUrl());

  ngOnInit(): void {
    this.initForm();

    this.route.data.subscribe(data => {
      if (data['modo'] === 'apelar') {
        this.isApelacion = true;
      }
    });

    if (this.router.url.includes('/apelar/')) {
      this.isApelacion = true;
    }

    this.route.queryParams.subscribe(params => {
      if (params['tipo']) {
        this.tipoYPrivacidadForm.patchValue({ tipoIncidencia: params['tipo'] });
      }
    });

    this.route.paramMap.subscribe(params => {
      this.incidenciaId = params.get('id');
      if (this.incidenciaId) {
        this.cargarIncidencia(this.incidenciaId);
      }
    });
  }

  cargarIncidencia(id: string) {
    this.isLoading = true;
    this.incidenciasService.getIncidenciaById(id).subscribe({
      next: (res) => {
        if (!res) {
          alert('No se pudo encontrar la información de la incidencia.');
          this.isLoading = false;
          return;
        }

        this.incidenciaEstado = res.estado || '';
        let tipoStr = (res.tipoIncidencia?.nombre || res.tipo || '').toUpperCase().trim();
        if (!tipoStr && res.tipoIncidencia?.id) {
          switch (Number(res.tipoIncidencia.id)) {
            case 1: tipoStr = 'QUEJA'; break;
            case 2: tipoStr = 'RECLAMO'; break;
            case 3: tipoStr = 'DENUNCIA'; break;
            case 4: tipoStr = 'SUGERENCIA'; break;
          }
        }
        if (tipoStr.includes('QUEJA')) tipoStr = 'QUEJA';
        else if (tipoStr.includes('RECLAM')) tipoStr = 'RECLAMO';
        else if (tipoStr.includes('DENUNC')) tipoStr = 'DENUNCIA';
        else if (tipoStr.includes('SUGER')) tipoStr = 'SUGERENCIA';

        const formatFechaLocal = (isoStr: any): string => {
          if (!isoStr) return '';
          try {
            const d = new Date(isoStr);
            if (isNaN(d.getTime())) return '';
            const pad = (n: number) => n < 10 ? '0' + n : n;
            return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
          } catch {
            return typeof isoStr === 'string' && isoStr.length >= 16 ? isoStr.substring(0, 16) : '';
          }
        };

        const reversePrivacidadMap: any = {
          'PUB': 'PUBLICO',
          'PRIV': 'CONFIDENCIAL'
        };
        const privacidadStr = res.privacidad?.seudo ? (reversePrivacidadMap[res.privacidad.seudo] || 'PUBLICO') : 'PUBLICO';

        this.tipoYPrivacidadForm.patchValue({
          tipoIncidencia: tipoStr,
          privacidad: privacidadStr
        });

        // Extraer contador original (vital para validaciones del backend)
        const contadorVal = res.domicilio?.contador || res.contador || '';
        this.contadorOriginal = contadorVal;
        this.detalleIncidenciaForm.patchValue({ noContador: contadorVal });

        // Extraer coordenadas si existen en res o domicilio
        const lat = res.latitud || res.domicilio?.latitud || '';
        const lng = res.longitud || res.domicilio?.longitud || '';
        const gpsVal = (lat && lng) ? `${lat},${lng}` : '';

        // Extraer dirección referencial o del domicilio
        const direccionVal = res.direccionReferencial || res.domicilio?.direccion?.descripDireccion || '';

        if (tipoStr === 'QUEJA') {
          this.detalleIncidenciaForm.patchValue({
            dependenciaId: res.dependencia?.id || '',
            empleadoId: res.nombreEmpleado || '',
            fechaIncidencia: formatFechaLocal(res.fechaIncidencia),
            lugar: direccionVal,
            noContador: contadorVal,
            ubicacionGps: gpsVal,
            descripcion: res.descripcion || ''
          });

          if (res.testigos) {
            let listTestigos: any[] = [];
            if (Array.isArray(res.testigos)) {
              listTestigos = res.testigos;
            } else if (typeof res.testigos === 'string') {
              try { listTestigos = JSON.parse(res.testigos); } catch {}
            }
            const testigosFormArray = this.detalleIncidenciaForm.get('testigos') as FormArray;
            testigosFormArray.clear();
            listTestigos.forEach((t: any) => {
              testigosFormArray.push(this.fb.group({
                testigoNombre: [t.nombre || t.testigoNombre || ''],
                testigoTelefono: [t.telefono || t.testigoTelefono || ''],
                testigoCorreo: [t.correo || t.testigoCorreo || '']
              }));
            });
          }
        } else if (tipoStr === 'RECLAMO') {
          this.detalleIncidenciaForm.patchValue({
            tipoServicioId: res.tipoServicio?.id || res.dependencia?.id || '',
            ubicacionGps: gpsVal,
            direccion: direccionVal,
            noContador: contadorVal,
            descripcion: res.descripcion || ''
          });
        } else if (tipoStr === 'DENUNCIA') {
          let denunciadosNombres = '';
          const denunciadosArr = (res.denunciados && res.denunciados.length > 0) ? res.denunciados : (res.testigos || []);
          if (Array.isArray(denunciadosArr)) {
            denunciadosNombres = denunciadosArr.map((t: any) => t.nombre || t).filter(Boolean).join(', ');
          } else if (typeof denunciadosArr === 'string') {
            try {
              const parsed = JSON.parse(denunciadosArr);
              if (Array.isArray(parsed)) {
                denunciadosNombres = parsed.map((t: any) => t.nombre || t).filter(Boolean).join(', ');
              } else {
                denunciadosNombres = denunciadosArr;
              }
            } catch {
              denunciadosNombres = denunciadosArr;
            }
          }

          this.detalleIncidenciaForm.patchValue({
            tipoDenunciaId: res.tipoDenuncia?.id || res.dependencia?.id || '',
            denunciados: denunciadosNombres,
            fechaHoraHechos: formatFechaLocal(res.fechaIncidencia),
            ubicacionGps: gpsVal,
            direccion: direccionVal,
            noContador: contadorVal,
            relato: res.descripcion || ''
          });
        } else if (tipoStr === 'SUGERENCIA') {
          this.detalleIncidenciaForm.patchValue({
            areaId: res.area?.id || res.dependencia?.id || '',
            noContador: contadorVal,
            descripcionActual: res.descripcion || '',
            propuestaMejora: res.propuestaMejora || ''
          });
        }

        const estadoUpper = (res.estado || '').toUpperCase();
        const esBorrador = estadoUpper === 'BORRADOR';
        const esRechazada = estadoUpper.includes('RECHAZAD');

        // Modo Apelación o Edición de Borrador: permite modificar y subir más pruebas
        if (this.isApelacion || (esRechazada && this.router.url.includes('/apelar/'))) {
          this.isApelacion = true;
          this.isReadOnly = false;
          this.tipoYPrivacidadForm.enable();
          this.detalleIncidenciaForm.enable();

          const tipoFijado = tipoStr;
          const privFijada = privacidadStr;
          this.tipoYPrivacidadForm.valueChanges.subscribe(val => {
            if (val.tipoIncidencia !== tipoFijado || val.privacidad !== privFijada) {
              this.tipoYPrivacidadForm.patchValue(
                { tipoIncidencia: tipoFijado, privacidad: privFijada },
                { emitEvent: false }
              );
            }
          });

          this.incidenciasService.getArchivosIncidencia(id).subscribe({
            next: (archivos) => this.archivosExistentes = archivos,
            error: (err) => console.error('Error cargando archivos', err)
          });
          this.cargarFeedbackRechazo(id);
        } else if (esBorrador) {
          this.isReadOnly = false;
          this.tipoYPrivacidadForm.enable();
          this.detalleIncidenciaForm.enable();
          this.incidenciasService.getArchivosIncidencia(id).subscribe({
            next: (archivos) => this.archivosExistentes = archivos,
            error: (err) => console.error('Error cargando archivos', err)
          });
        } else {
          this.isReadOnly = true;
          this.clearFormValidators(this.incidenciaForm);
          this.incidenciasService.getArchivosIncidencia(id).subscribe({
            next: (archivos) => this.archivosExistentes = archivos,
            error: (err) => console.error('Error cargando archivos', err)
          });
        }
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error fetching incidencia', err);
        alert('No se pudo cargar la incidencia.');
        this.isLoading = false;
      }
    });
  }

  cargarFeedbackRechazo(id: string | number): void {
    this.cargandoFeedback = true;
    this.incidenciasService.getFeedbackRechazo(id).subscribe({
      next: (feedbacks) => {
        this.motivosRechazoFeedback = feedbacks || [];
        this.cargandoFeedback = false;
      },
      error: (err) => {
        console.warn('Aviso cargando feedback de rechazo:', err);
        this.motivosRechazoFeedback = [];
        this.cargandoFeedback = false;
      }
    });
  }

  private clearFormValidators(group: FormGroup): void {
    Object.keys(group.controls).forEach(key => {
      const control = group.get(key);
      if (control instanceof FormGroup) {
        this.clearFormValidators(control);
      } else {
        control?.clearValidators();
        control?.updateValueAndValidity({ emitEvent: false });
      }
    });
  }

  initForm(): void {
    this.incidenciaForm = this.fb.group({
      tipoYPrivacidad: this.fb.group({
        tipoIncidencia: ['', Validators.required],
        privacidad: ['PUBLICO', Validators.required]
      }),
      detalleIncidencia: this.fb.group({
        // Queja
        dependenciaId: [''],
        empleadoId: [''],
        fechaIncidencia: ['', [fechaRangoIncidenciaValidator()]],
        lugar: [''],
        descripcion: [''],
        testigos: this.fb.array([]),
        // Reclamo
        tipoServicioId: [''],
        ubicacionGps: [''],
        direccion: [''],
        noContador: [''],
        // Denuncia
        tipoDenunciaId: [''],
        denunciados: [''],
        fechaHoraHechos: ['', [fechaRangoIncidenciaValidator()]],
        relato: [''],
        // Sugerencia
        areaId: [''],
        descripcionActual: [''],
        propuestaMejora: ['']
      }),
      evidencias: this.fb.group({})
    });
  }

  // Getters para enviar al @Input de los subcomponentes
  get tipoYPrivacidadForm(): FormGroup { return this.incidenciaForm.get('tipoYPrivacidad') as FormGroup; }
  get detalleIncidenciaForm(): FormGroup { return this.incidenciaForm.get('detalleIncidencia') as FormGroup; }
  get evidenciasForm(): FormGroup { return this.incidenciaForm.get('evidencias') as FormGroup; }

  get tipoSeleccionado(): string {
    return this.tipoYPrivacidadForm.get('tipoIncidencia')?.value || '';
  }

  get evidenciasValidas(): boolean {
    return true;
  }

  onArchivosActualizados(archivos: any[]): void {
    this.archivosExistentes = archivos || [];
  }

  onArchivosSeleccionados(files: File[]): void {
    this.archivosStaged = files || [];
  }

  abrirModalCargaArchivos(): void {
    this.modalCargaArchivosOpen = true;
  }

  enviarBorrador(abrirModalAlGuardar = false): void {
    this.submit(true, abrirModalAlGuardar);
  }

  enviarIncidencia(): void {
    if (this.incidenciaForm.invalid) {
      this.incidenciaForm.markAllAsTouched();
      return;
    }
    this.submit(false, false);
  }

  private submit(esBorrador: boolean, abrirModalAlGuardar = false): void {
    this.isSubmitting = true;
    const formValue = this.incidenciaForm.getRawValue();

    let tipoIncidenciaId = 0;
    switch(formValue.tipoYPrivacidad.tipoIncidencia) {
      case 'QUEJA': tipoIncidenciaId = 1; break;
      case 'RECLAMO': tipoIncidenciaId = 2; break;
      case 'DENUNCIA': tipoIncidenciaId = 3; break;
      case 'SUGERENCIA': tipoIncidenciaId = 4; break;
    }

    const privacidadMap: any = {
      'PUBLICO': 'PUB',
      'PRIVADO': 'PRIV',
      'CONFIDENCIAL': 'PRIV',
      'ANONIMO': 'PRIV'
    };
    const privacidadVal = privacidadMap[formValue.tipoYPrivacidad.privacidad] || 'PUB';

    const detalle = formValue.detalleIncidencia;

    const payload: any = {
      incidenciaId: this.incidenciaId ? Number(this.incidenciaId) : null,
      tipoIncidencia: tipoIncidenciaId,
      privacidad: privacidadVal,
      contador: detalle.noContador || this.contadorOriginal || '',
      esBorrador: this.isApelacion ? false : esBorrador
    };

    if (formValue.tipoYPrivacidad.tipoIncidencia === 'QUEJA') {
      payload.detalleQueja = {
        dependenciaId: detalle.dependenciaId ? Number(detalle.dependenciaId) : null,
        nombreEmpleado: detalle.empleadoId || '',
        fechaIncidencia: detalle.fechaIncidencia ? new Date(detalle.fechaIncidencia).toISOString() : null,
        direccionReferencial: detalle.lugar || '',
        descripcion: detalle.descripcion || '',
        testigo: (detalle.testigos && detalle.testigos.length > 0) ? detalle.testigos.map((t: any) => ({
          nombre: t.testigoNombre ? t.testigoNombre.trim() : '',
          telefono: t.testigoTelefono ? t.testigoTelefono.trim() : '',
          correo: t.testigoCorreo ? t.testigoCorreo.trim() : ''
        })).filter((t: any) => t.nombre) : [],
        latitudGps: '',
        longitudGps: ''
      };
      if (detalle.ubicacionGps) {
        const parts = detalle.ubicacionGps.split(',');
        if (parts.length === 2) {
          payload.detalleQueja.latitudGps = parts[0].trim();
          payload.detalleQueja.longitudGps = parts[1].trim();
        }
      }
    } else if (formValue.tipoYPrivacidad.tipoIncidencia === 'RECLAMO') {
      payload.detalleReclamo = {
        tipoServicioId: detalle.tipoServicioId ? Number(detalle.tipoServicioId) : null,
        latitudGps: '',
        longitudGps: '',
        descripcion: detalle.descripcion || ''
      };
      if (detalle.ubicacionGps) {
        const parts = detalle.ubicacionGps.split(',');
        if (parts.length === 2) {
          payload.detalleReclamo.latitudGps = parts[0].trim();
          payload.detalleReclamo.longitudGps = parts[1].trim();
        }
      }
    } else if (formValue.tipoYPrivacidad.tipoIncidencia === 'DENUNCIA') {
      payload.detalleDenuncia = {
        tipoDenunciaId: detalle.tipoDenunciaId ? Number(detalle.tipoDenunciaId) : null,
        denunciados: detalle.denunciados ? [{ nombre: detalle.denunciados }] : [],
        fechaHoraHechos: detalle.fechaHoraHechos ? new Date(detalle.fechaHoraHechos).toISOString() : null,
        relato: detalle.relato || '',
        latitudGps: '',
        longitudGps: ''
      };
      if (detalle.ubicacionGps) {
        const parts = detalle.ubicacionGps.split(',');
        if (parts.length === 2) {
          payload.detalleDenuncia.latitudGps = parts[0].trim();
          payload.detalleDenuncia.longitudGps = parts[1].trim();
        }
      }
    } else if (formValue.tipoYPrivacidad.tipoIncidencia === 'SUGERENCIA') {
      payload.detalleSugerencia = {
        areaId: detalle.areaId ? Number(detalle.areaId) : null,
        descripcionActual: detalle.descripcionActual || '',
        propuestaMejora: detalle.propuestaMejora || ''
      };
    }

    console.log('Payload a enviar:', JSON.stringify(payload, null, 2));

    // Si es modo apelación, llamar al endpoint de apelación
    if (this.isApelacion && this.incidenciaId) {
      this.incidenciasService.apelarIncidencia(this.incidenciaId, payload).subscribe({
        next: () => {
          alert(`Apelación presentada con éxito para la incidencia #${this.incidenciaId}. El caso fue devuelto a revisión municipal.`);
          this.router.navigate([this.dashboardUrl()]);
          this.isSubmitting = false;
        },
        error: (err) => {
          console.error('Error enviando apelación', err);
          let errorMsg = 'Ocurrió un error al enviar la apelación.';
          if (err.error && err.error.message) {
            errorMsg = err.error.message;
            if (err.error.errors && Array.isArray(err.error.errors)) {
              errorMsg += '\n' + err.error.errors.join('\n');
            }
          }
          alert(errorMsg);
          this.isSubmitting = false;
        }
      });
      return;
    }

    const request$ = this.incidenciaId
      ? this.incidenciasService.actualizarIncidencia(this.incidenciaId, payload)
      : this.incidenciasService.crearIncidencia(payload);

    request$.subscribe({
      next: (res) => {
        const idGenerado = res?.id || this.incidenciaId;
        if (idGenerado) {
          this.incidenciaId = idGenerado.toString();
        }

        // Si el usuario preparó archivos en el modal antes de guardar, subirlos ahora con el ID generado
        if (idGenerado && this.archivosStaged.length > 0) {
          this.incidenciasService.uploadEvidencias(idGenerado, this.archivosStaged).subscribe({
            next: () => {
              this.archivosStaged = [];
            },
            error: (errUp) => {
              console.warn('Aviso subiendo archivos preparados:', errUp);
            }
          });
        }

        if (abrirModalAlGuardar) {
          this.isSubmitting = false;
          this.modalCargaArchivosOpen = true;
          return;
        }

        if (!esBorrador && idGenerado) {
          // Ejecuta el cambio de estado de BORRADOR a ENVIADA usando el PATH de evento del backend:
          // POST /api/v1/asiic/incidencias/{id}/event/enviar-incidencia
          this.incidenciasService.enviarIncidencia(idGenerado).subscribe({
            next: () => {
              alert(`Incidencia enviada con éxito. Estado: ENVIADA. Código: #${idGenerado}`);
              this.router.navigate([this.dashboardUrl()]);
              this.isSubmitting = false;
            },
            error: (errEv) => {
              console.warn('Aviso en transición enviar-incidencia:', errEv);
              alert(`Incidencia registrada y remitida a revisión municipal. Código: #${idGenerado}`);
              this.router.navigate([this.dashboardUrl()]);
              this.isSubmitting = false;
            }
          });
        } else {
          alert(this.incidenciaId ? 'Borrador de incidencia actualizado con éxito.' : 'Borrador guardado con éxito. Código de seguimiento: #' + (idGenerado || 'Pendiente'));
          this.router.navigate([this.dashboardUrl()]);
          this.isSubmitting = false;
        }
      },
      error: (err) => {
        console.error('Error enviando', err);
        let errorMsg = 'Ocurrió un error al enviar la incidencia.';
        if (err.error && err.error.message) {
          errorMsg = err.error.message;
          // Si hay errores de validación de campos, mostrarlos
          if (err.error.errors && Array.isArray(err.error.errors)) {
            errorMsg += '\n' + err.error.errors.join('\n');
          }
        }
        alert(errorMsg);
        this.isSubmitting = false;
      }
    });
  }
}
