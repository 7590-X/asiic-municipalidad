import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ClarityModule } from '@clr/angular';

@Component({
  selector: 'app-tipo-privacidad',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ClarityModule],
  templateUrl: './tipo-privacidad.component.html'
})
export class TipoPrivacidadComponent {
  @Input({ required: true }) stepForm!: FormGroup;

  get tipoSeleccionado(): string {
    return this.stepForm.get('tipoIncidencia')?.value;
  }
}
