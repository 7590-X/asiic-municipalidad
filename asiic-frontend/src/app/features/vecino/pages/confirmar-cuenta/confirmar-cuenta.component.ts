import { CommonModule } from '@angular/common';
import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ClarityModule, ClrLoadingState } from '@clr/angular';
import { NotificationService } from '../../../../core/services/notification.service';
import { AsidePanelComponent } from '../../components/aside-panel/aside-panel.component';
import { AuthService } from '../../../../core/services/auth.service';
import { passwordMatchValidator } from '../../../../shared/validators/password-match.validator';

@Component({
  selector: 'app-confirmar-cuenta',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ClarityModule, AsidePanelComponent],
  templateUrl: './confirmar-cuenta.component.html',
  styleUrl: './confirmar-cuenta.component.scss'
})
export class ConfirmarCuentaComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private oauthService = inject(AuthService);
  private notification = inject(NotificationService);
  private destroyRef = inject(DestroyRef);

  token = signal<string>('');
  submitBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;
  isSuccess = signal<boolean>(false);
  successMessage = signal<string>('');

  form = this.fb.nonNullable.group({
    password: ['', [
      Validators.required,
      Validators.minLength(8),
      Validators.pattern(/^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$/)
    ]],
    confirmPassword: ['', [Validators.required]]
  }, { validators: passwordMatchValidator });

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['token']) {
        this.token.set(params['token']);
      } else {
        this.notification.error('Token no válido o no proporcionado.');
      }
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { password, confirmPassword } = this.form.getRawValue();

    if (password !== confirmPassword) {
      this.notification.error('Las contraseñas no coinciden.');
      return;
    }

    if (!this.token()) {
      this.notification.error('Token no válido.');
      return;
    }

    this.submitBtnState = ClrLoadingState.LOADING;

    const payload = {
      token: this.token(), //el token de la URL
      password: btoa(password) //La contraseña convertida en base64
    };

    const subscription = this.oauthService.confirmarCuenta(payload).subscribe({
      next: (resp) => {
        this.submitBtnState = ClrLoadingState.SUCCESS;
        this.isSuccess.set(true);
        this.successMessage.set(resp.message || 'Cuenta confirmada y contraseña generada con éxito.');
        setTimeout(() => this.router.navigate(['/']), 3000);
      },
      error: (err: HttpErrorResponse) => {
        this.submitBtnState = ClrLoadingState.ERROR;
        this.notification.error(err.error?.message || 'Error al confirmar la cuenta');
      }
    });
    this.destroyRef.onDestroy(() => subscription.unsubscribe())
  }
}
