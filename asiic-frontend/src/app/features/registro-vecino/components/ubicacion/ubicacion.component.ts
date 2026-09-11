import { Component, DestroyRef, inject, Input, OnInit } from '@angular/core';
import { FormGroup, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { LocacionesService } from '../../../../core/services/locaciones.service';
import { LocacionModel } from '../../../../core/models/locacion.model';
import { signal } from '@angular/core';
import { CapitalizeWordsDirective } from '../../../../shared/directives/capitalize-words.directive';
import { OnlyDigitsDirective } from '../../../../shared/directives/only-digits.directive';

@Component({
  selector: 'app-ubicacion',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, ClarityModule, CapitalizeWordsDirective, OnlyDigitsDirective],
  templateUrl: './ubicacion.component.html',
})
export class UbicacionComponent implements OnInit {

  @Input({ required: true }) stepForm!: FormGroup;

  private locacionesService = inject(LocacionesService)
  private destroyRef = inject(DestroyRef);

  paises = signal<LocacionModel[]>([])
  deptos = signal<LocacionModel[]>([])
  munis = signal<LocacionModel[]>([])
  comunas = signal<LocacionModel[]>([])

  paisId = signal<number>(0)
  deptoId = signal<number>(0)
  muniId = signal<number>(0)

  ngOnInit(): void {
    const subscription = this.locacionesService.getCatalogoPaises().subscribe({
      next: (resp) => this.paises.set(resp),
      error: (err) => {
        // handle error
      }
    });
    this.destroyRef.onDestroy(() => subscription.unsubscribe())
  }




  onPaisChange(paisId: number): void {
    if (paisId) {
      this.paisId.set(paisId);
      const subscription = this.locacionesService.getCatalogoDepartamentos(paisId)
        .subscribe({
          next: (resp) => this.deptos.set(resp),
          error: (err) => {
            // handle error
          }
        });
      this.destroyRef.onDestroy(() => subscription.unsubscribe())
    }
  }

  onDeptoChange(deptoId: number): void {
    if (deptoId) {
      this.deptoId.set(deptoId);
      const subscription = this.locacionesService.getCatalogoMunicipios(this.paisId(), deptoId)
        .subscribe({
          next: (resp) => this.munis.set(resp),
          error: (err) => {
            // handle error
          }
        })
      this.destroyRef.onDestroy(() => subscription.unsubscribe())
    }
  }

  onMuniChange(muniId: number): void {
    if (muniId) {
      this.muniId.set(muniId)
      const subscription = this.locacionesService.getCatalogoComunas(this.paisId(), this.muniId(), muniId)
        .subscribe({
          next: (resp) => this.comunas.set(resp),
          error: (err) => {
            // handle error
          }
        })
      this.destroyRef.onDestroy(() => subscription.unsubscribe())
    }
  }
}
