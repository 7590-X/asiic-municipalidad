import { Component, DestroyRef, inject, Input, OnInit, signal } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { OnlyDigitsDirective } from '../../../../shared/directives/only-digits.directive';
import { CapitalizeWordsDirective } from '../../../../shared/directives/capitalize-words.directive';
import { CatalogoItemModel } from '../../../../core/models/catalogo-item.model';
import { CatalogosService } from '../../../../core/services/catalogos.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-identificacion',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ClarityModule, OnlyDigitsDirective, CapitalizeWordsDirective],
  templateUrl: './identificacion.component.html'
})
export class IdentificacionComponent implements OnInit {
  @Input({ required: true }) stepForm!: FormGroup;

  private catalogosService = inject(CatalogosService);
  private destroyRef = inject(DestroyRef);

  estadoCivil = signal<CatalogoItemModel[]>([]);
  profesiones = signal<CatalogoItemModel[]>([]);
  loadingCatalogos = signal<boolean>(true);

  generos = [
    { id: 'M', value: 'Masculino' },
    { id: 'F', value: 'Femenino' }
  ];

  ngOnInit(): void {
    const sub = forkJoin({
      ec: this.catalogosService.getCatalogoEstadoCivil(),
      pr: this.catalogosService.getProfesiones()
    }).subscribe({
      next: ({ ec, pr }) => {
        this.estadoCivil.set(ec);
        this.profesiones.set(pr);
        this.loadingCatalogos.set(false);
      },
      error: () => {
        this.loadingCatalogos.set(false);
      }
    });

    this.destroyRef.onDestroy(() => sub.unsubscribe());
  }
}
