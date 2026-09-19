import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { signal } from '@angular/core';
import { DashboardComponent } from './dashboard.component';
import { AuthService } from '../../../../core/services/auth.service';
import { CurrentUser } from '../../../../core/models/auth.model';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  const mockUser: CurrentUser = {
    id: 'vecino-456',
    name: 'Ana Lucía Gómez',
    email: 'agomez@correo.gt',
    username: 'agomez',
    roles: ['Vecino'],
    token: 'jwt-token-valido',
    expiresAt: Date.now() + 3600000,
  };

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['logout'], {
      currentUser: signal<CurrentUser | null>(mockUser),
    });

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        provideRouter([]),
        provideAnimations(),
        { provide: AuthService, useValue: authServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debe crearse correctamente', () => {
    expect(component).toBeTruthy();
  });

  it('debe mostrar el nombre de bienvenida del vecino', () => {
    expect(component.userName()).toBe('Ana Lucía Gómez');
  });

  it('debe mapear correctamente las clases de insignias según el estado del trámite', () => {
    expect(component.getEstadoBadgeClass('Resuelto')).toBe('badge-success');
    expect(component.getEstadoBadgeClass('Finalizado')).toBe('badge-success');
    expect(component.getEstadoBadgeClass('En Progreso')).toBe('badge-warning');
    expect(component.getEstadoBadgeClass('En Revisión')).toBe('badge-info');
    expect(component.getEstadoBadgeClass('Pendiente')).toBe('badge-danger');
  });

  it('debe cargar los trámites iniciales en la lista', () => {
    const list = component.tramites();
    expect(list.length).toBeGreaterThan(0);
    expect(list[0].id).toBe('ASIIC-2026-0042');
  });
});
