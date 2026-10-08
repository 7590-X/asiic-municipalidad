import { Component, DestroyRef, Input, OnInit, inject, signal, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, FormArray, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { ClarityModule } from '@clr/angular';
import { forkJoin } from 'rxjs';
import { IncidenciasService, DomicilioDto } from '../../services/incidencias.service';
import { CatalogoItem } from '../../../../core/models/catalogo.model';

@Component({
  selector: 'app-detalle-incidencia',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ClarityModule],
  templateUrl: './detalle-incidencia.component.html'
})
export class DetalleIncidenciaComponent implements OnInit, OnChanges {
  @Input({ required: true }) stepForm!: FormGroup;
  @Input({ required: true }) tipoIncidencia: string = '';

  private incidenciasService = inject(IncidenciasService);
  private destroyRef = inject(DestroyRef);

  dependencias = signal<CatalogoItem[]>([]);
  tiposServicio = signal<CatalogoItem[]>([]);
  tiposDenuncia = signal<CatalogoItem[]>([]);
  areasSugerencia = signal<CatalogoItem[]>([]);
  contadores = signal<DomicilioDto[]>([]);
  loadingCatalogos = signal<boolean>(true);

  get testigosArray() {
    return this.stepForm.get('testigos') as FormArray;
  }

  agregarTestigo() {
    this.testigosArray.push(
      new FormGroup({
        testigoNombre: new FormControl(''),
        testigoTelefono: new FormControl(''),
        testigoCorreo: new FormControl('')
      })
    );
  }

  removerTestigo(index: number) {
    this.testigosArray.removeAt(index);
  }

  obtenerUbicacion() {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const lat = position.coords.latitude.toFixed(6);
          const lng = position.coords.longitude.toFixed(6);
          this.stepForm.get('ubicacionGps')?.setValue(`${lat}, ${lng}`);
        },
        (error) => {
          console.error('Error obteniendo ubicación', error);
          alert('No se pudo obtener tu ubicación. Por favor, asegúrate de haber dado los permisos en el navegador o ingrésala manualmente.');
        }
      );
    } else {
      alert('Tu navegador no soporta geolocalización.');
    }
  }


  ngOnInit(): void {
    const sub = forkJoin({
      dep: this.incidenciasService.getCatalogosDependencias(),
      serv: this.incidenciasService.getCatalogosTiposServicio(),
      den: this.incidenciasService.getCatalogosTiposDenuncia(),
      sug: this.incidenciasService.getCatalogosAreasSugerencia(),
      contadores: this.incidenciasService.getMisDomicilios()
    }).subscribe({
      next: (data) => {
        this.dependencias.set(data.dep);
        this.tiposServicio.set(data.serv);
        this.tiposDenuncia.set(data.den);
        this.areasSugerencia.set(data.sug);
        this.contadores.set(data.contadores);

        if (data.contadores.length === 1) {
          this.stepForm.get('noContador')?.setValue(data.contadores[0].contador);
        }

        this.loadingCatalogos.set(false);
      },
      error: () => {
        this.loadingCatalogos.set(false);
      }
    });

    this.destroyRef.onDestroy(() => sub.unsubscribe());

    // Escuchar cambios en el tipo de servicio para validar contador
    this.stepForm.get('tipoServicioId')?.valueChanges.subscribe(servicioId => {
      const isAguaOrIusi = servicioId === 1 || servicioId === 2 || servicioId === '1' || servicioId === '2';
      const contadorControl = this.stepForm.get('noContador');
      if (isAguaOrIusi) {
        contadorControl?.setValidators([Validators.required]);
      } else {
        contadorControl?.clearValidators();
      }
      contadorControl?.updateValueAndValidity();
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['tipoIncidencia'] && changes['tipoIncidencia'].currentValue) {
      this.updateDynamicValidators(changes['tipoIncidencia'].currentValue);
    }
  }

  updateDynamicValidators(tipo: string): void {
    const detalle = this.stepForm;

    // Limpiar todos los validadores
    Object.keys(detalle.controls).forEach(key => {
      detalle.get(key)?.clearValidators();
      detalle.get(key)?.updateValueAndValidity();
    });

    if (tipo === 'QUEJA') {
      detalle.get('dependenciaId')?.setValidators([Validators.required]);
      detalle.get('fechaIncidencia')?.setValidators([Validators.required]);
      detalle.get('lugar')?.setValidators([Validators.required]);
      detalle.get('descripcion')?.setValidators([Validators.required]);
    } else if (tipo === 'RECLAMO') {
      detalle.get('tipoServicioId')?.setValidators([Validators.required]);
      detalle.get('ubicacionGps')?.setValidators([Validators.required]);
      detalle.get('descripcion')?.setValidators([Validators.required]);
    } else if (tipo === 'DENUNCIA') {
      detalle.get('tipoDenunciaId')?.setValidators([Validators.required]);
      detalle.get('denunciados')?.setValidators([Validators.required]);
      detalle.get('fechaHoraHechos')?.setValidators([Validators.required]);
      detalle.get('direccion')?.setValidators([Validators.required]);
      detalle.get('relato')?.setValidators([Validators.required]);
    } else if (tipo === 'SUGERENCIA') {
      detalle.get('areaId')?.setValidators([Validators.required]);
      detalle.get('descripcionActual')?.setValidators([Validators.required]);
      detalle.get('propuestaMejora')?.setValidators([Validators.required]);
    }

    // Actualizar validez
    Object.keys(detalle.controls).forEach(key => detalle.get(key)?.updateValueAndValidity());
  }
}
