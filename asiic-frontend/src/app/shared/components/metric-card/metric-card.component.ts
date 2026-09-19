import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';

@Component({
  selector: 'app-metric-card',
  standalone: true,
  imports: [CommonModule, ClarityModule],
  templateUrl: './metric-card.component.html',
  styleUrls: ['./metric-card.component.scss']
})
export class MetricCardComponent {
  @Input() color: string = 'blue';
  @Input() label: string = '';
  @Input() icon: string = '';
  @Input() value: string | number = '';
  @Input() valueClass?: string = '';
  @Input() detail: string = '';
  @Input() badgeClass: string = 'badge-info';
  @Input() badgeText: string = '';
}
