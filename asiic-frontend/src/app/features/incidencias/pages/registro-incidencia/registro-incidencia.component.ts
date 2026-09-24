import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ClarityModule } from '@clr/angular';
import { IncidenciasService } from '../../services/incidencias.service';
import { ActivatedRoute, Router } from '@angular/router';

// Subcomponentes
import { TipoPrivacidadComponent } from '../../components/tipo-privacidad/tipo-privacidad.component';
import { DetalleIncidenciaComponent } from '../../components/detalle-incidencia/detalle-incidencia.component';
import { EvidenciasComponent } from '../../components/evidencias/evidencias.component';

@Component({
  selector: 'app-registro-incidencia',
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule, 
    ClarityModule,
    TipoPrivacidadComponent,
    DetalleIncidenciaComponent,
    EvidenciasComponent
  ],
  templateUrl: './registro-incidencia.component.html',
  styleUrls: ['./registro-incidencia.component.scss']
})
export class RegistroIncidenciaComponent implements OnInit {
  incidenciaForm!: FormGroup;
  files: File[] = [];
  isSubmitting = false;

  incidenciaId: string | null = null;
  isReadOnly: boolean = false;
  isLoading: boolean = false;

  private fb = inject(FormBuilder);
  private incidenciasService = inject(IncidenciasService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  ngOnInit(): void {
    this.initForm();

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
        this.tipoYPrivacidadForm.patchValue({
          tipoIncidencia: res.tipoIncidencia,
          privacidad: res.privacidad
        });
        
        if (res.tipoIncidencia === 'QUEJA') {
          this.detalleIncidenciaForm.patchValue({
            dependenciaId: res.dependenciaId,
            empleadoId: res.empleadoId,
            fechaIncidencia: res.fechaIncidencia ? res.fechaIncidencia.substring(0, 16) : '',
            lugar: res.lugar,
            descripcion: res.descripcion
          });
        } else if (res.tipoIncidencia === 'RECLAMO') {
          this.detalleIncidenciaForm.patchValue({
            tipoServicioId: res.tipoServicioId,
            ubicacionGps: res.ubicacionGps,
            direccion: res.direccion,
            noContador: res.noContador,
            descripcion: res.descripcion
          });
        } else if (res.tipoIncidencia === 'DENUNCIA') {
          this.detalleIncidenciaForm.patchValue({
            tipoDenunciaId: res.tipoDenunciaId,
            denunciados: res.denunciados,
            fechaHoraHechos: res.fechaHoraHechos ? res.fechaHoraHechos.substring(0, 16) : '',
            direccion: res.direccion,
            relato: res.relato
          });
        } else if (res.tipoIncidencia === 'SUGERENCIA') {
          this.detalleIncidenciaForm.patchValue({
            areaId: res.areaId,
            descripcionActual: res.descripcionActual,
            propuestaMejora: res.propuestaMejora
          });
        }

        if (res.estado !== 'BORRADOR') {
          this.isReadOnly = true;
          // No deshabilitamos el form entero porque el clrStepper requiere status=VALID para avanzar.
          // this.incidenciaForm.disable(); 
          this.clearFormValidators(this.incidenciaForm);
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
        fechaIncidencia: [''],
        lugar: [''],
        descripcion: [''],
        testigoNombre: [''],
        testigoTelefono: [''],
        testigoCorreo: [''],
        // Reclamo
        tipoServicioId: [''],
        ubicacionGps: [''],
        direccion: [''],
        noContador: [''],
        // Denuncia
        tipoDenunciaId: [''],
        denunciados: [''],
        fechaHoraHechos: [''],
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
    if (this.tipoSeleccionado === 'RECLAMO' || this.tipoSeleccionado === 'DENUNCIA') {
      return this.files.length > 0;
    }
    return true;
  }

  onFilesChanged(files: File[]): void {
    this.files = files;
  }

  enviarBorrador(): void {
    this.submit(true);
  }

  enviarIncidencia(): void {
    if (this.incidenciaForm.invalid || !this.evidenciasValidas) {
      this.incidenciaForm.markAllAsTouched();
      // Si faltan evidencias en reclamo o denuncia
      if (!this.evidenciasValidas) {
        alert('Debe adjuntar evidencias para Reclamos y Denuncias.');
      }
      return;
    }
    this.submit(false);
  }

  private submit(esBorrador: boolean): void {
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
      'PRIVADO': 'PRIV'
    };
    const privacidadVal = privacidadMap[formValue.tipoYPrivacidad.privacidad] || 'PUB';

    const detalle = formValue.detalleIncidencia;

    const payload: any = {
      incidenciaId: this.incidenciaId ? Number(this.incidenciaId) : 0,
      tipoIncidencia: tipoIncidenciaId,
      privacidad: privacidadVal,
      contador: detalle.noContador || '' 
    };

    if (formValue.tipoYPrivacidad.tipoIncidencia === 'QUEJA') {
      payload.detalleQueja = {
        dependenciaId: detalle.dependenciaId ? Number(detalle.dependenciaId) : null,
        nombreEmpleado: detalle.empleadoId || '',
        fechaIncidencia: detalle.fechaIncidencia ? new Date(detalle.fechaIncidencia).toISOString() : null,
        direccionReferencial: detalle.lugar || '',
        descripcion: detalle.descripcion || '',
        testigo: detalle.testigoNombre ? [
          {
            nombre: detalle.testigoNombre,
            telefono: detalle.testigoTelefono || '',
            correo: detalle.testigoCorreo || ''
          }
        ] : [],
        latitudGps: '',
        longitudGps: ''
      };
    } else if (formValue.tipoYPrivacidad.tipoIncidencia === 'RECLAMO') {
      payload.detalleReclamo = {
        tipoServicioId: detalle.tipoServicioId ? Number(detalle.tipoServicioId) : null,
        latitudGps: '',
        longitudGps: '',
        direccion: detalle.direccion || '',
        noContador: detalle.noContador || '',
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
        direccion: detalle.direccion || '',
        relato: detalle.relato || ''
      };
    } else if (formValue.tipoYPrivacidad.tipoIncidencia === 'SUGERENCIA') {
      payload.detalleSugerencia = {
        areaId: detalle.areaId ? Number(detalle.areaId) : null,
        descripcionActual: detalle.descripcionActual || '',
        propuestaMejora: detalle.propuestaMejora || ''
      };
    }

    const request$ = this.incidenciaId 
      ? this.incidenciasService.actualizarIncidencia(this.incidenciaId, payload, this.files)
      : this.incidenciasService.crearIncidencia(payload, this.files);

    request$.subscribe({
      next: (res) => {
        alert(this.incidenciaId ? 'Incidencia actualizada con éxito.' : 'Incidencia enviada con éxito. Código de seguimiento: ' + (res?.codigo || 'Pendiente'));
        this.router.navigate(['/vecino/dashboard']);
        this.isSubmitting = false;
      },
      error: (err) => {
        console.error('Error enviando', err);
        let errorMsg = 'Ocurrió un error al enviar la incidencia.';
        if (err.error && err.error.message) {
          errorMsg = err.error.message;
          // If there are field validation errors, show them
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
