import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { ClarityModule, ClrLoadingState } from '@clr/angular';
import { VecinoService } from '../../../../core/services/vecino-publico.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-confirmar-cuenta',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ClarityModule],
  templateUrl: './confirmar-cuenta.component.html',
  styleUrl: './confirmar-cuenta.component.scss'
})
export class ConfirmarCuentaComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private vecinoService = inject(VecinoService);
  private notification = inject(NotificationService);

  token = signal<string>('');
  submitBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;
  isSuccess = signal<boolean>(false);
  successMessage = signal<string>('');

  form = this.fb.nonNullable.group({
    password: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', [Validators.required]]
  });

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

    this.vecinoService.confirmarCuenta(payload).subscribe({  //Luego el payload que envia el token y la contraseña codificada en base64 al endpoint de confirmar cuenta
      next: (resp) => {
        this.submitBtnState = ClrLoadingState.SUCCESS;
        this.isSuccess.set(true);
        this.successMessage.set(resp.message || 'Cuenta confirmada y contraseña generada con éxito.');
        setTimeout(() => this.router.navigate(['/']), 3000); //despues de 3 segundos se redirige al usuario a la página principal
      },
      error: (err: HttpErrorResponse) => {
        this.submitBtnState = ClrLoadingState.ERROR;
        this.notification.error(err.error?.message || 'Error al confirmar la cuenta');
      }
    });
  }
}
