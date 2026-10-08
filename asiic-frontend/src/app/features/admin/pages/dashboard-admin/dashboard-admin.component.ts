import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ClarityModule } from '@clr/angular';
import { MunicipalidadService } from '../../services/municipalidad.service';
import { UsuariosService } from '../../../../core/services/usuarios.service';
import { forkJoin } from 'rxjs';

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
    this.cargarDatos();
  }

  cargarDatos(): void {
    forkJoin({
      munis: this.muniService.getMunicipalidades(),
      users: this.usuariosService.getUsuarios()
    }).subscribe({
      next: ({ munis, users }) => {
        this.municipalidades = munis;

        this.usuarios = users
          // Filtramos: debe estar activo ('A') y tener al menos un rol de sistema o interno
          .filter(u => {
             const sysRoles = ['sys_admin', 'sys_agente', 'sys_analista', 'RANTA', 'ROPER'];
             return u.estado === 'A' && Array.isArray(u.roles) && u.roles.some((r: string) => sysRoles.includes(r));
          })
          .map(u => {
            let municipalidadesNombres = '';
            if (Array.isArray(u.municipalidades)) {
              municipalidadesNombres = u.municipalidades
                .map((mId: number) => {
                  const muni = this.municipalidades.find(m => m.id === mId);
                  return muni ? muni.nombre : String(mId);
                })
                .join(', ');
            }

            return {
              ...u,
              rol: u.roles.map((r: string) => this.formatRole(r)).join(', '),
              municipalidadesStr: municipalidadesNombres
            };
          });
      },
      error: (err) => console.error('Error fetching data', err)
    });
  }

  formatRole(rawRole: string): string {
    const roleMap: Record<string, string> = {
      'sys_admin': 'Administrador',
      'sys_agente': 'Agente de Campo',
      'sys_analista': 'Analista',
      'ROLE_VECINO': 'Vecino',
      'RANTA': 'Usuario Interno',
      'ROPER': 'Operador'
    };
    return roleMap[rawRole] || rawRole;
  }
}
