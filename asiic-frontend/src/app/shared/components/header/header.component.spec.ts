import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { HeaderComponent } from './header.component';
import { AuthService } from '../../../core/services/auth.service';
import { CurrentUser } from '../../../core/models/auth.model';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  const mockUser: CurrentUser = {
    id: 'user-123',
    name: 'Carlos Mendoza',
    email: 'cmendoza@muniguate.gob.gt',
    username: 'cmendoza',
    roles: ['Vecino'],
    token: 'fake-jwt',
    expiresAt: Date.now() + 3600000,
  };

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['logout', 'getRole'], {
      currentUser: signal<CurrentUser | null>(mockUser),
    });
    authServiceSpy.getRole.and.returnValue('Vecino');

    await TestBed.configureTestingModule({
      imports: [HeaderComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debe crearse correctamente', () => {
    expect(component).toBeTruthy();
  });

  it('debe computar el nombre y las iniciales del usuario', () => {
    expect(component.userName()).toBe('Carlos Mendoza');
    expect(component.userInitials()).toBe('CM');
    expect(component.userRole()).toBe('Vecino');
  });

  it('debe emitir toggleSidebar al hacer clic en el botón de menú', () => {
    spyOn(component.toggleSidebar, 'emit');
    const toggleBtn = fixture.nativeElement.querySelector('.header-toggle-btn');
    toggleBtn.click();
    expect(component.toggleSidebar.emit).toHaveBeenCalled();
  });

  it('debe invocar authService.logout al cerrar sesión', () => {
    component.onLogout();
    expect(authServiceSpy.logout).toHaveBeenCalled();
  });
});
