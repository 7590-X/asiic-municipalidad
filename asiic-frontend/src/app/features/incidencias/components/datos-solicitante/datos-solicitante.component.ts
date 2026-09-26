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
    // Los datos del usuario ya son manejados por el JWT en el backend.
    // No necesitamos extraer ni pre-llenar los datos aquí.
  }
}
