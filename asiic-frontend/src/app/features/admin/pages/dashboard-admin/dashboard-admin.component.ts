import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ClarityModule } from '@clr/angular';
import { MunicipalidadService } from '../../services/municipalidad.service';
import { UsuariosService } from '../../../../core/services/usuarios.service';

@Component({
  selector: 'app-dashboard-admin',
  standalone: true,
  imports: [CommonModule, RouterModule, ClarityModule],
  templateUrl: './dashboard-admin.component.html',
  styleUrls: ['./dashboard-admin.component.scss']
})
export class DashboardAdminComponent implements OnInit {
  municipalidades: any[] = [];
  usuarios: any[] = [];
  
  private muniService = inject(MunicipalidadService);
  private usuariosService = inject(UsuariosService);

  ngOnInit(): void {
    this.cargarMunicipalidades();
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
    this.usuariosService.getUsuarios().subscribe({
      next: (data) => {
        this.usuarios = data.map(u => ({
          ...u,
          rol: this.formatRole(u.rol)
        }));
      },
      error: (err) => console.error('Error fetching usuarios', err)
    });
  }

  formatRole(rawRole: string): string {
    const roleMap: Record<string, string> = {
      'sys_admin': 'Administrador',
      'sys_agente': 'Agente de Campo',
      'sys_analista': 'Analista',
      'ROLE_VECINO': 'Vecino'
    };
    return roleMap[rawRole] || rawRole;
  }

  cargarMunicipalidades(): void {
    this.muniService.getMunicipalidades().subscribe({
      next: (data) => {
        // Asignamos un estado por defecto si no viene de la API
        this.municipalidades = data.map(m => ({
          ...m,
          estado: m.estado || 'A'
        }));
      },
      error: (err) => {
        console.error('Error fetching municipalidades', err);
      }
    });
  }

  toggleEstadoMuni(muni: any): void {
    const nuevoEstado = muni.estado === 'A' ? 'I' : 'A';
    
    // Aquí idealmente llamaríamos al backend:
    // this.muniService.cambiarEstado(muni.id, nuevoEstado).subscribe(...)
    // Por ahora lo cambiamos en la vista
    muni.estado = nuevoEstado;
  }

  toggleEstadoUsuario(usuario: any): void {
    const nuevoEstado = usuario.estado === 'A' ? 'I' : 'A';
    
    // Aquí idealmente llamaríamos a un UserService
    usuario.estado = nuevoEstado;
  }
}
