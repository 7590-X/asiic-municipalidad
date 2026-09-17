import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ClarityModule, ClrLoadingState } from '@clr/angular';
import { AuthService } from '../../../../core/services/auth.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { RouterLink } from '@angular/router';

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

  form = this.fb.nonNullable.group({
    correo: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitBtnState = ClrLoadingState.LOADING;
    
    // Convertir contraseña a Base64 si el backend así lo requiere (como en confirmar-cuenta)
    // Asumiremos que usa la contraseña cruda o base64 dependiendo del diseño. 
    // Usaremos cruda por defecto para un Login estándar.
    const { correo, password } = this.form.getRawValue();

    this.authService.login({ correo, password }).subscribe({
      next: () => {
        this.submitBtnState = ClrLoadingState.SUCCESS;
        this.notification.success('Inicio de sesión exitoso.');
      },
      error: (err: HttpErrorResponse) => {
        this.submitBtnState = ClrLoadingState.ERROR;
        this.notification.error(err.error?.message || 'Error al iniciar sesión. Verifique sus credenciales.');
      }
    });
  }
}
