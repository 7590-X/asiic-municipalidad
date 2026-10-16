import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ClarityModule } from '@clr/angular';
import { UsuariosService } from '../../../../core/services/usuarios.service';
import { MunicipalidadService } from '../../services/municipalidad.service';

@Component({
  selector: 'app-registro-usuario',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ClarityModule],
  templateUrl: './registro-usuario.component.html',
  styleUrls: ['./registro-usuario.component.scss']
})
export class RegistroUsuarioComponent implements OnInit {
  private fb = inject(FormBuilder);
  private usuariosService = inject(UsuariosService);
  private muniService = inject(MunicipalidadService);

  userForm!: FormGroup;
  roles: any[] = [];
  municipalidades: any[] = [];
  
  loading = false;
  successMessage = '';
  errorMessage = '';
  isConfirmOpen = false;

  ngOnInit(): void {
    this.initForm();
    this.loadRoles();
    this.loadMunicipalidades();
  }

  private initForm(): void {
    this.userForm = this.fb.group({
      nombres: ['', [Validators.required, Validators.minLength(2)]],
      apellidos: ['', [Validators.required, Validators.minLength(2)]],
      correo: ['', [Validators.required, Validators.email]],
      rol: ['', Validators.required],
      municipalidadId: ['', Validators.required]
    });
  }

  private loadRoles(): void {
    this.usuariosService.getRoles().subscribe({
      next: (roles) => {
        this.roles = roles;
      },
      error: (err) => {
        console.error('Error al cargar roles', err);
      }
    });
  }

  private loadMunicipalidades(): void {
    this.muniService.getMunicipalidades().subscribe({
      next: (munis) => {
        this.municipalidades = munis;
      },
      error: (err) => {
        console.error('Error al cargar municipalidades', err);
      }
    });
  }

  formatRole(rawRole: string): string {
    const roleMap: Record<string, string> = {
      'sys_admin': 'Administrador',
      'sys_agente': 'Agente de Campo',
      'sys_analista': 'Analista'
    };
    return roleMap[rawRole] || rawRole;
  }

  openConfirm(): void {
    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      this.errorMessage = 'Por favor complete todos los campos obligatorios y con el formato correcto.';
      setTimeout(() => this.errorMessage = '', 5000);
      return;
    }
    this.isConfirmOpen = true;
  }

  cancelConfirm(): void {
    this.isConfirmOpen = false;
  }

  onSubmit(): void {
    this.isConfirmOpen = false;
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const formValue = this.userForm.value;
    const payload = {
      nombres: formValue.nombres,
      apellidos: formValue.apellidos,
      correo: formValue.correo,
      roles: [formValue.rol],
      municipalidades: [Number(formValue.municipalidadId)]
    };

    this.usuariosService.crearUsuario(payload).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Cuenta de usuario creada exitosamente. Se ha enviado un correo con las credenciales.';
        this.userForm.reset();
        setTimeout(() => this.successMessage = '', 5000);
      },
      error: (err) => {
        this.loading = false;
        console.error('Error al registrar usuario', err);
        if (err.status === 409) {
          this.errorMessage = 'El correo electrónico ingresado ya se encuentra asociado a una cuenta existente';
        } else {
          this.errorMessage = 'Ocurrió un error al registrar el usuario. Verifique los datos o intente nuevamente.';
        }
      }
    });
  }
}
