import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ClarityModule } from '@clr/angular';

@Component({
  selector: 'app-quick-action-card',
  standalone: true,
  imports: [CommonModule, RouterLink, ClarityModule],
  templateUrl: './quick-action-card.component.html',
  styleUrls: ['./quick-action-card.component.scss']
})
export class QuickActionCardComponent {
  @Input() link: string = '';
  @Input() queryParams: any = null;
  @Input() actionClass: string = 'action-primary';
  @Input() icon: string = '';
  @Input() title: string = '';
  @Input() description: string = '';
}
