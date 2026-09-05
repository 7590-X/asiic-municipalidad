import { Component, Input, input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { CatalogoItem } from '../../models/catalogo-item.model';

@Component({
  selector: 'app-ubicacion',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ClarityModule],
  templateUrl: './ubicacion.component.html',
})
export class UbicacionComponent {
  @Input({ required: true }) stepForm!: FormGroup;
  paises = input<CatalogoItem[]>([]);
  departamentos = input<CatalogoItem[]>([]);
  municipios = input<CatalogoItem[]>([]);
  zonas = input.required<CatalogoItem[]>(); // Comuna
}
