import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ClarityModule, ClrLoadingState } from '@clr/angular';
import { VecinoService } from '../../services/vecino.service';
import { ApiResponse, RegistrarVecinoRequest } from '../../models/registrar-vecino.model';
import { IdentificacionComponent } from '../../components/identificacion/identificacion.component';
import { ContactoComponent } from '../../components/contacto/contacto.component';
import { UbicacionComponent } from "../../components/ubicacion/ubicacion.component";
import { DocumentosComponent } from '../../components/documentos/documentos.component';
import { NotificationService } from '../../../../core/services/notification.service';
import { cuiValidator } from '../../../../shared/validators/cui.validator';
import { ValidacionesService } from '../../../../core/services/validaciones.service';
import { telefonoValidator } from '../../../../shared/validators/correo.validator';

@Component({
  selector: 'app-registro-vecino',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ClarityModule, IdentificacionComponent, ContactoComponent, UbicacionComponent, DocumentosComponent],
  templateUrl: './registro-vecino.component.html',
  styleUrl: './registro-vecino.component.scss',
})
export class RegistroVecinoComponent {
  // Servicios
  private fb = inject(FormBuilder);
  private vecinos = inject(VecinoService);
  private validaciones: ValidacionesService = inject(ValidacionesService)
  private notification = inject(NotificationService)

  // Formulario para creación de vecino
  form = this.fb.nonNullable.group({
    identificacion: this.fb.nonNullable.group({
      cui: [
        '',
        [
          Validators.required,
          Validators.pattern(/^\d{13}$/)
        ],
        cuiValidator(this.validaciones)
      ],
      nombres: ['', [Validators.required, Validators.maxLength(45)]],
      apellidos: ['', [Validators.required, Validators.maxLength(45)]],
      genero: ['', Validators.required],
      estado_civil_id: ['', Validators.required],
      profesion_id: ['', Validators.required],
    }),
    contacto: this.fb.nonNullable.group({
      telefono: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
      correo: ['',
        [
          Validators.required,
          Validators.email,
          Validators.maxLength(45)
        ],
        telefonoValidator(this.validaciones)
      ],
    }),
    ubicacion: this.fb.nonNullable.group({
      comuna_id: [null as number | null, Validators.required],
      direccion: ['', [Validators.required, Validators.maxLength(100)]],
    }),
    documentos: this.fb.nonNullable.group({
      nit: ['', [Validators.pattern(/^\d*[a-zA-Z]?$/), Validators.maxLength(13)]],
      pasaporte: ['', [Validators.pattern(/^[a-zA-Z0-9]*$/), Validators.maxLength(20)]],
    }),
  });

  // Estados
  submitBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

  submit(): void {

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitBtnState = ClrLoadingState.LOADING;
    const v = this.form.getRawValue();
    const body: RegistrarVecinoRequest = {
      cui: v.identificacion.cui,
      nombres: v.identificacion.nombres,
      apellidos: v.identificacion.apellidos,
      genero: v.identificacion.genero as 'M' | 'F',
      estado_civil_id: v.identificacion.estado_civil_id,
      profesion_id: v.identificacion.profesion_id,
      telefono: v.contacto.telefono,
      correo: v.contacto.correo,
      direccion: v.ubicacion.direccion,
      locacion_id: v.ubicacion.comuna_id as number,
      nit: v.documentos.nit || undefined,
      pasaporte: v.documentos.pasaporte || undefined,
    };

    this.vecinos.registrar(body).subscribe({
      next: (resp) => {
        this.submitBtnState = ClrLoadingState.SUCCESS;
        this.notification.success("Cuenta creada exitosamente")
      },
      error: (err: HttpErrorResponse) => {
        this.notification.error(err.message)
        this.submitBtnState = ClrLoadingState.ERROR;
      },
    });
  }
}
