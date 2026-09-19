import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ClrLoadingState } from '@clr/angular';
import { LoginComponent } from './login.component';
import { AuthService } from '../../../../core/services/auth.service';
import { AuthLoginResponse } from '../../../../core/models/auth.model';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['login']);

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: AuthService, useValue: authServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debe crearse correctamente', () => {
    expect(component).toBeTruthy();
  });

  it('debe inicializar el formulario con campos vacíos e inválidos', () => {
    expect(component.form.valid).toBeFalse();
    expect(component.form.controls.username.value).toBe('');
    expect(component.form.controls.password.value).toBe('');
  });

  it('debe validar formato de correo electrónico', () => {
    component.form.controls.username.setValue('correo-invalido');
    expect(component.form.controls.username.hasError('email')).toBeTrue();

    component.form.controls.username.setValue('usuario@muni.gob.gt');
    expect(component.form.controls.username.hasError('email')).toBeFalse();
  });

  it('no debe llamar a authService.login si el formulario es inválido', () => {
    component.submit();
    expect(authServiceSpy.login).not.toHaveBeenCalled();
    expect(component.form.touched).toBeTrue();
  });

  it('debe enviar credenciales y actualizar estado a SUCCESS ante autenticación válida', () => {
    const mockResponse: AuthLoginResponse = {
      code: 200,
      action: '/api/v1/asiic/auth/login',
      message: 'Inicio de sesión exitoso',
      payload: {
        access_token: 'mock-token',
        expires_in: 60,
        refresh_expires_in: 1800,
        token_type: 'Bearer'
      }
    };
    authServiceSpy.login.and.returnValue(of(mockResponse));

    component.form.controls.username.setValue('vecino@muni.gob.gt');
    component.form.controls.password.setValue('Secreto123*');
    component.submit();

    expect(authServiceSpy.login).toHaveBeenCalledWith({
      username: 'vecino@muni.gob.gt',
      password: 'Secreto123*',
      recordar: false
    });
    expect(component.submitBtnState).toBe(ClrLoadingState.SUCCESS);
  });

  it('debe mostrar mensaje de error y estado ERROR ante fallo de autenticación', () => {
    authServiceSpy.login.and.returnValue(
      throwError(() => ({
        status: 401,
        error: { message: 'Credenciales inválidas' }
      }))
    );

    component.form.controls.username.setValue('vecino@muni.gob.gt');
    component.form.controls.password.setValue('PasswordErroneo');
    component.submit();

    expect(component.submitBtnState).toBe(ClrLoadingState.ERROR);
    expect(component.errorMessage()).toBe('Credenciales inválidas');
  });
});
