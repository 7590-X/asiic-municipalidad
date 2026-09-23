import { Component, Input, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ClarityModule } from '@clr/angular';
import { IncidenciasService } from '../../services/incidencias.service';

@Component({
  selector: 'app-datos-solicitante',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ClarityModule],
  templateUrl: './datos-solicitante.component.html'
})
export class DatosSolicitanteComponent implements OnInit {
  @Input({ required: true }) stepForm!: FormGroup;

  private incidenciasService = inject(IncidenciasService);

  ngOnInit(): void {
    this.incidenciasService.getDatosSolicitanteActual().subscribe({
      next: (datos) => this.stepForm.patchValue(datos),
      error: (err) => console.error('Error al obtener perfil del vecino', err)
    });
  }
}
