import { Component, DestroyRef, inject, Input, OnInit, signal } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { LocacionesService } from '../../../../core/services/locaciones.service';
import { LocacionModel } from '../../../../core/models/locacion.model';
import { CapitalizeWordsDirective } from '../../../../shared/directives/capitalize-words.directive';
import { OnlyDigitsDirective } from '../../../../shared/directives/only-digits.directive';

@Component({
  selector: 'app-ubicacion',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ClarityModule,
    CapitalizeWordsDirective,
    OnlyDigitsDirective
  ],
  templateUrl: './ubicacion.component.html'
})
export class UbicacionComponent implements OnInit {
  @Input({ required: true }) stepForm!: FormGroup;

  private locacionesService = inject(LocacionesService);
  private destroyRef = inject(DestroyRef);

  paises = signal<LocacionModel[]>([]);
  deptos = signal<LocacionModel[]>([]);
  munis = signal<LocacionModel[]>([]);
  comunas = signal<LocacionModel[]>([]);

  loadingPaises = signal<boolean>(true);
  loadingDeptos = signal<boolean>(false);
  loadingMunis = signal<boolean>(false);
  loadingComunas = signal<boolean>(false);

  ngOnInit(): void {
    // Carga inicial de Países
    const subPaises = this.locacionesService.getCatalogoPaises().subscribe({
      next: (resp) => {
        this.paises.set(resp);
        this.loadingPaises.set(false);
      },
      error: () => this.loadingPaises.set(false)
    });
    this.destroyRef.onDestroy(() => subPaises.unsubscribe());

    // Suscripción reactiva a cambios de País
    const subPais = this.stepForm.controls['pais_id']?.valueChanges.subscribe((paisId) => {
      this.stepForm.controls['departamento_id']?.reset(null);
      this.stepForm.controls['municipio_id']?.reset(null);
      this.stepForm.controls['comuna_id']?.reset(null);
      this.deptos.set([]);
      this.munis.set([]);
      this.comunas.set([]);

      if (paisId) {
        this.loadingDeptos.set(true);
        this.locacionesService.getCatalogoDepartamentos(Number(paisId)).subscribe({
          next: (resp) => {
            this.deptos.set(resp);
            this.loadingDeptos.set(false);
          },
          error: () => this.loadingDeptos.set(false)
        });
      }
    });
    if (subPais) {
      this.destroyRef.onDestroy(() => subPais.unsubscribe());
    }

    // Suscripción reactiva a cambios de Departamento
    const subDepto = this.stepForm.controls['departamento_id']?.valueChanges.subscribe((deptoId) => {
      this.stepForm.controls['municipio_id']?.reset(null);
      this.stepForm.controls['comuna_id']?.reset(null);
      this.munis.set([]);
      this.comunas.set([]);

      const paisId = this.stepForm.controls['pais_id']?.value;
      if (paisId && deptoId) {
        this.loadingMunis.set(true);
        this.locacionesService.getCatalogoMunicipios(Number(paisId), Number(deptoId)).subscribe({
          next: (resp) => {
            this.munis.set(resp);
            this.loadingMunis.set(false);
          },
          error: () => this.loadingMunis.set(false)
        });
      }
    });
    if (subDepto) {
      this.destroyRef.onDestroy(() => subDepto.unsubscribe());
    }

    // Suscripción reactiva a cambios de Municipio
    const subMuni = this.stepForm.controls['municipio_id']?.valueChanges.subscribe((muniId) => {
      this.stepForm.controls['comuna_id']?.reset(null);
      this.comunas.set([]);

      const paisId = this.stepForm.controls['pais_id']?.value;
      const deptoId = this.stepForm.controls['departamento_id']?.value;
      if (paisId && deptoId && muniId) {
        this.loadingComunas.set(true);
        this.locacionesService.getCatalogoComunas(Number(paisId), Number(deptoId), Number(muniId)).subscribe({
          next: (resp) => {
            this.comunas.set(resp);
            this.loadingComunas.set(false);
          },
          error: () => this.loadingComunas.set(false)
        });
      }
    });
    if (subMuni) {
      this.destroyRef.onDestroy(() => subMuni.unsubscribe());
    }
  }
}
