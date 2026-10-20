import { Component, OnInit, OnDestroy, HostListener, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet, NavigationEnd } from '@angular/router';
import { ClarityModule } from '@clr/angular';
import { filter, Subscription } from 'rxjs';
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
export class PortalLayoutComponent implements OnInit, OnDestroy {
  private router = inject(Router);
  private routerSub?: Subscription;

  isMobile = false;
  isMobileOpen = false;
  isSidebarCollapsed = false;

  ngOnInit(): void {
    this.checkScreenSize();

    // Cuando el usuario navega a cualquier pantalla ("entra a una opción"),
    // cerramos automáticamente el menú móvil para dejar todo el espacio a la vista.
    this.routerSub = this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe(() => {
        if (this.isMobile && this.isMobileOpen) {
          this.closeMobileSidebar();
        }
      });
  }

  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
  }

  @HostListener('window:resize')
  onResize(): void {
    this.checkScreenSize();
  }

  private checkScreenSize(): void {
    if (typeof window !== 'undefined') {
      const wasMobile = this.isMobile;
      this.isMobile = window.innerWidth < 992;

      // Si la pantalla cambia de móvil a escritorio, cerramos el drawer móvil
      if (!this.isMobile && wasMobile) {
        this.isMobileOpen = false;
      }
    }
  }

  toggleSidebar(): void {
    if (this.isMobile) {
      this.isMobileOpen = !this.isMobileOpen;
    } else {
      this.isSidebarCollapsed = !this.isSidebarCollapsed;
    }
  }

  closeMobileSidebar(): void {
    this.isMobileOpen = false;
  }

  onSidebarCollapseChange(collapsed: boolean): void {
    this.isSidebarCollapsed = collapsed;
  }
}

