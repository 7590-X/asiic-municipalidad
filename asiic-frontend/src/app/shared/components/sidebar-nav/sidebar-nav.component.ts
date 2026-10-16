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
  @Output() collapsedChange = new EventEmitter<boolean>();

  readonly isAdmin = computed(() => {
    return this.authService.isAdmin();
  });

  onCollapseChange(isCollapsed: boolean): void {
    this.collapsed = isCollapsed;
    this.collapsedChange.emit(isCollapsed);
  }
}
