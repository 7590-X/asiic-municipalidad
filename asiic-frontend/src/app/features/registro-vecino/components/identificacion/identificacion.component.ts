import { Component, Input, input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { OnlyDigitsDirective } from '../../../../shared/directives/only-digits.directive';
import { CapitalizeWordsDirective } from '../../../../shared/directives/capitalize-words.directive';
import { CatalogoItem } from '../../models/catalogo-item.model';

@Component({
  selector: 'app-identificacion',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ClarityModule, OnlyDigitsDirective, CapitalizeWordsDirective],
  templateUrl: './identificacion.component.html',
})
export class IdentificacionComponent {
  @Input({ required: true }) stepForm!: FormGroup;
  estadoCivil = input.required<CatalogoItem[]>();
  profesiones = input.required<CatalogoItem[]>();
}
