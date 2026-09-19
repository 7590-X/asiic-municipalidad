import { Component, EventEmitter, Output, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { ClarityModule } from '@clr/angular';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, ClarityModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent {
  private authService = inject(AuthService);

  @Output() toggleSidebar = new EventEmitter<void>();

  readonly currentUser = this.authService.currentUser;

  readonly userName = computed(() => {
    return this.currentUser()?.name || 'Vecino Municipal';
  });

  readonly userEmail = computed(() => {
    return this.currentUser()?.email || '';
  });

  readonly userRole = computed(() => {
    return this.authService.getRole() || 'Vecino';
  });

  readonly userInitials = computed(() => {
    const name = this.userName();
    if (!name) return 'VM';
    const parts = name.trim().split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return name.slice(0, 2).toUpperCase();
  });

  onLogout(): void {
    this.authService.logout();
  }
}
