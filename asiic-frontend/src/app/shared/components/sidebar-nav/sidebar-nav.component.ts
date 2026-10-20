import { Component, EventEmitter, Input, Output, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { ClarityModule } from '@clr/angular';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-sidebar-nav',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, ClarityModule],
  templateUrl: './sidebar-nav.component.html',
  styleUrls: ['./sidebar-nav.component.scss'],
})
export class SidebarNavComponent {
  private authService = inject(AuthService);
  @Input() collapsed = false;
  @Input() isMobile = false;
  @Output() collapsedChange = new EventEmitter<boolean>();
  @Output() navItemClicked = new EventEmitter<void>();

  readonly isAdmin = computed(() => {
    return this.authService.isAdmin();
  });

  readonly isAnalista = computed(() => {
    return this.authService.isAnalista();
  });

  readonly isAgente = computed(() => {
    return this.authService.isAgenteCampo();
  });

  readonly isInternal = computed(() => {
    return this.authService.isInternalUser();
  });

  readonly isVecino = computed(() => {
    return !this.isAdmin() && !this.isAnalista() && !this.isAgente();
  });

  readonly dashboardLink = computed(() => {
    return this.authService.getDashboardUrl();
  });

  onCollapseChange(isCollapsed: boolean): void {
    this.collapsed = isCollapsed;
    this.collapsedChange.emit(isCollapsed);
  }

  onLinkClick(): void {
    this.navItemClicked.emit();
  }
}

