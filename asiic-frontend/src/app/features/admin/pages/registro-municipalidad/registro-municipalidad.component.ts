import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ClarityModule } from '@clr/angular';
import { MunicipalidadService } from '../../services/municipalidad.service';
import { LocacionesService } from '../../../../core/services/locaciones.service';
import { LocacionModel } from '../../../../core/models/locacion.model';
import { distinctUntilChanged } from 'rxjs/operators';

import { AsidePanelComponent } from '../../../vecino/components/aside-panel/aside-panel.component';

@Component({
  selector: 'app-registro-municipalidad',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ClarityModule],
  templateUrl: './registro-municipalidad.component.html',
  styleUrls: ['./registro-municipalidad.component.scss']
})
export class RegistroMunicipalidadComponent implements OnInit {
  muniForm!: FormGroup;
  paises: LocacionModel[] = [];
  departamentos: LocacionModel[] = [];
  municipios: LocacionModel[] = [];

  showConfirmDialog = false;
  showCancelDialog = false;

  private fb = inject(FormBuilder);
  private muniService = inject(MunicipalidadService);
  private locacionesService = inject(LocacionesService);

  ngOnInit(): void {
    this.initForm();
    this.cargarPaises();

    this.muniForm.get('paisId')?.valueChanges.pipe(distinctUntilChanged()).subscribe(paisId => {
      this.departamentos = [];
      this.municipios = [];
      this.muniForm.get('departamentoId')?.setValue('', { emitEvent: false });
      this.muniForm.get('municipioId')?.setValue('', { emitEvent: false });
      if (paisId) {
        this.cargarDepartamentos(paisId);
      }
    });

    this.muniForm.get('departamentoId')?.valueChanges.pipe(distinctUntilChanged()).subscribe(deptoId => {
      this.municipios = [];
      this.muniForm.get('municipioId')?.setValue('', { emitEvent: false });
      const paisId = this.muniForm.get('paisId')?.value;
      if (paisId && deptoId) {
        this.cargarMunicipios(paisId, deptoId);
      }
    });
  }

  initForm(): void {
    this.muniForm = this.fb.group({
      paisId: ['', Validators.required],
      departamentoId: ['', Validators.required],
      municipioId: ['', Validators.required],
      nombreOficial: ['', Validators.required],
      nit: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(13), Validators.pattern('^\\d*[A-Z]?$')]],
      estado: ['A', Validators.required],
      fechaFundacion: [''],
      direccionFiscal: ['', Validators.required],
      coordenadasGps: ['', Validators.required],
      pbx: [''],
      correo: ['', [Validators.required, Validators.email]]
    });
  }

  cargarPaises(): void {
    this.locacionesService.getCatalogoPaises().subscribe({
      next: (data) => this.paises = data,
      error: (err) => console.error('Error cargando paises', err)
    });
  }

  cargarDepartamentos(paisId: number): void {
    this.locacionesService.getCatalogoDepartamentos(paisId).subscribe({
      next: (data) => this.departamentos = data,
      error: (err) => console.error('Error cargando departamentos', err)
    });
  }

  cargarMunicipios(paisId: number, deptoId: number): void {
    this.locacionesService.getCatalogoMunicipios(paisId, deptoId).subscribe({
      next: (data) => this.municipios = data,
      error: (err) => console.error('Error cargando municipios', err)
    });
  }

  onRegistrarClick(): void {
    if (this.muniForm.invalid) {
      this.muniForm.markAllAsTouched();
      alert('Campos requeridos incompletos');
      return;
    }
    this.showConfirmDialog = true;
  }

  onCancelarClick(): void {
    this.showCancelDialog = true;
  }

  confirmarCancelar(): void {
    this.showCancelDialog = false;
    this.muniForm.reset();
    this.muniForm.get('estado')?.setValue('A');
    // En un sistema real aquí se retornaría a la pantalla anterior
    alert('Cancelado. Se borró la información ingresada.');
  }

  cerrarConfirmDialog(): void {
    this.showConfirmDialog = false;
  }

  cerrarCancelDialog(): void {
    this.showCancelDialog = false;
  }

  confirmarRegistro(): void {
    this.showConfirmDialog = false;
    const formVal = this.muniForm.value;

    let latitudGps = '';
    let longitudGps = '';
    if (formVal.coordenadasGps) {
      const parts = formVal.coordenadasGps.split(',');
      if (parts.length === 2) {
        latitudGps = parts[0].trim();
        longitudGps = parts[1].trim();
      } else {
        latitudGps = formVal.coordenadasGps;
        longitudGps = formVal.coordenadasGps; // fallback
      }
    }

    let fechaFundacionFormat = formVal.fechaFundacion;
    if (fechaFundacionFormat && fechaFundacionFormat.includes('/')) {
      const parts = fechaFundacionFormat.split('/');
      if (parts.length === 3) {
        // Asumiendo formato MM/DD/YYYY que es el default de navegadores en US o Clarity
        // Si el año está al final
        if (parts[2].length === 4) {
          fechaFundacionFormat = `${parts[2]}-${parts[0].padStart(2, '0')}-${parts[1].padStart(2, '0')}`;
        } else if (parts[0].length === 4) { // YYYY/MM/DD
          fechaFundacionFormat = `${parts[0]}-${parts[1].padStart(2, '0')}-${parts[2].padStart(2, '0')}`;
        }
      }
    }

    const payload = {
      municipioId: Number(formVal.municipioId),
      nombre: formVal.nombreOficial,
      nit: formVal.nit,
      fechaFundacion: fechaFundacionFormat ? fechaFundacionFormat : undefined,
      direccionFiscal: formVal.direccionFiscal,
      latitudGps,
      longitudGps,
      pbx: formVal.pbx ? formVal.pbx : undefined,
      correo: formVal.correo,
      estado: formVal.estado
    };

    alert('Enviando payload con municipioId: ' + payload.municipioId);

    this.muniService.registrarMunicipalidad(payload).subscribe({
      next: () => {
        alert('Municipalidad registrada exitosamente');
        this.muniForm.reset();
        this.muniForm.get('estado')?.setValue('A');
      },
      error: (err) => {
        let errorMsg = 'Ocurrió un error al registrar la municipalidad.';
        if (err.error && err.error.message) {
          errorMsg = err.error.message;
        }
        alert(errorMsg);
      }
    });
  }
}
