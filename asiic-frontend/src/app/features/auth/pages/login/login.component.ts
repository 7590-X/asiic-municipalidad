import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { ClarityModule, ClrLoadingState } from '@clr/angular';
import { AuthService } from '../../../../core/services/auth.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ClarityModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private notification = inject(NotificationService);

  submitBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;
  errorMessage = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
    recordar: [false]
  });

  submit(): void {
    this.errorMessage.set(null);

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitBtnState = ClrLoadingState.LOADING;
    const { username, password, recordar } = this.form.getRawValue();

    this.authService.login({ username, password, recordar }).subscribe({
      next: (response) => {
        this.submitBtnState = ClrLoadingState.SUCCESS;
        this.notification.success(response?.message || 'Inicio de sesión exitoso.');
      },
      error: (err: HttpErrorResponse) => {
        this.submitBtnState = ClrLoadingState.ERROR;

        let msg = 'Error al iniciar sesión. Verifique sus credenciales.';
        if (err.error && typeof err.error === 'object') {
          msg = err.error.message || err.error.error_description || msg;
        } else if (err.status === 401) {
          msg = 'Credenciales inválidas. Por favor verifique su correo y contraseña.';
        } else if (err.status === 0) {
          msg = 'No se pudo conectar con el servidor de autenticación. Verifique su conexión.';
        }

        this.errorMessage.set(msg);
        this.notification.error(msg);
      }
    });
  }
}
