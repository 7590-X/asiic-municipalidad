import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { ClarityModule } from '@clr/angular';
import { HeaderComponent } from '../../components/header/header.component';
import { SidebarNavComponent } from '../../components/sidebar-nav/sidebar-nav.component';

@Component({
  selector: 'app-portal-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    ClarityModule,
    HeaderComponent,
    SidebarNavComponent,
  ],
  templateUrl: './portal-layout.component.html',
  styleUrls: ['./portal-layout.component.scss'],
})
export class PortalLayoutComponent {
  isSidebarCollapsed = false;

  toggleSidebar(): void {
    this.isSidebarCollapsed = !this.isSidebarCollapsed;
  }
}
