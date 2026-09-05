import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ClarityModule } from '@clr/angular';
import { forkJoin } from 'rxjs';
import { OnlyDigitsDirective } from '../../../../shared/directives/only-digits.directive';

import { CatalogoService } from '../../services/catalogo.service';
import { VecinoService } from '../../services/vecino.service';
import { CatalogoItem } from '../../models/catalogo-item.model';
import { ApiResponse, RegistrarVecinoRequest } from '../../models/registrar-vecino.model';

@Component({
  selector: 'app-registro-vecino',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ClarityModule, OnlyDigitsDirective],
  templateUrl: './registro-vecino.component.html',
  styleUrl: './registro-vecino.component.scss',
})
export class RegistroVecinoComponent implements OnInit {
  private fb = inject(FormBuilder);
  private catalogos = inject(CatalogoService);
  private vecinos = inject(VecinoService);

  estadoCivil = signal<CatalogoItem[]>([]);
  profesiones = signal<CatalogoItem[]>([]);
  zonas = signal<CatalogoItem[]>([]);

  cargandoCatalogos = signal(true);
  enviando = signal(false);
  exito = signal(false);
  errorMsg = signal<string | null>(null);

  // Un FormGroup por paso del wizard (lo exige clrStepper).
  form = this.fb.nonNullable.group({
    identificacion: this.fb.nonNullable.group({
      cui: ['', [Validators.required, Validators.pattern(/^\d{13}$/)]],
      nombres: ['', [Validators.required, Validators.maxLength(45)]],
      apellidos: ['', [Validators.required, Validators.maxLength(45)]],
      genero: ['', Validators.required],
      estado_civil_id: ['', Validators.required],
      profesion_id: ['', Validators.required],
    }),
    contacto: this.fb.nonNullable.group({
      telefono: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
      correo: ['', [Validators.required, Validators.email, Validators.maxLength(45)]],
      direccion: ['', [Validators.required, Validators.maxLength(100)]],
    }),
    ubicacion: this.fb.nonNullable.group({
      locacion_id: [null as number | null, Validators.required],
    }),
    documentos: this.fb.nonNullable.group({
      nit: ['', [Validators.pattern(/^\d{0,12}$/)]],
      pasaporte: ['', [Validators.maxLength(20)]],
    }),
  });

  ngOnInit(): void {
    forkJoin({
      ec: this.catalogos.estadoCivil(),
      pr: this.catalogos.profesiones(),
      zo: this.catalogos.zonas(),
    }).subscribe({
      next: ({ ec, pr, zo }) => {
        this.estadoCivil.set(ec);
        this.profesiones.set(pr);
        this.zonas.set(zo);
        this.cargandoCatalogos.set(false);
      },
      error: () => {
        this.errorMsg.set('No se pudieron cargar los catálogos. Recargue la página.');
        this.cargandoCatalogos.set(false);
      },
    });
  }

  enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.errorMsg.set(null);

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
      direccion: v.contacto.direccion,
      locacion_id: v.ubicacion.locacion_id as number,
      nit: v.documentos.nit || undefined,
      pasaporte: v.documentos.pasaporte || undefined,
    };

    this.vecinos.registrar(body).subscribe({
      next: () => {
        this.enviando.set(false);
        this.exito.set(true); // CU paso 2.3.10
      },
      error: (err: HttpErrorResponse) => {
        this.enviando.set(false);
        this.errorMsg.set(this.mensajeDeError(err));
      },
    });
  }

  private mensajeDeError(err: HttpErrorResponse): string {
    try {
      const parsed = JSON.parse(err.error) as ApiResponse;
      if (parsed?.message) return parsed.message;
    } catch {
      if (typeof err.error === 'string' && err.error.trim()) return err.error;
    }
    return 'No se pudo completar el registro. Intente más tarde.';
  }
}
