import { Component, DestroyRef, inject, Input, input, OnInit, signal } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { OnlyDigitsDirective } from '../../../../shared/directives/only-digits.directive';
import { CapitalizeWordsDirective } from '../../../../shared/directives/capitalize-words.directive';
import { CatalogoItem } from '../../../../core/models/catalogo-item.model';
import { CatalogosService } from '../../../../core/services/catalogos.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-identificacion',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ClarityModule, OnlyDigitsDirective, CapitalizeWordsDirective],
  templateUrl: './identificacion.component.html',
})
export class IdentificacionComponent implements OnInit {

  @Input({ required: true }) stepForm!: FormGroup;

  catalogosService = inject(CatalogosService)
  destroyRef = inject(DestroyRef)

  estadoCivil = signal<CatalogoItem[]>([])
  profesiones = signal<CatalogoItem[]>([])

  generos = [
    {
      id: "M",
      value: "Masculino"
    }, {
      id: "F",
      value: "Femenino"
    }, {
      id: "G",
      value: "99 Tipos de Gays"
    }
  ]

  ngOnInit(): void {
    console.log(this.stepForm)
    const subscribe = forkJoin({
      ec: this.catalogosService.getCatalogoEstadoCivil(),
      pr: this.catalogosService.getProfesiones()
    }).subscribe({
      next: ({ ec, pr }) => {
        this.estadoCivil.set(ec)
        this.profesiones.set(pr)
      },
      error: (err) => {
        // handle error
      }
    })
    this.destroyRef.onDestroy(() => subscribe.unsubscribe())
  }
}
