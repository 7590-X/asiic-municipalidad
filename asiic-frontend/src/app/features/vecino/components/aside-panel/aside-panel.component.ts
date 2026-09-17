import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-aside-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './aside-panel.component.html',
  styleUrls: ['./aside-panel.component.scss']
})
export class AsidePanelComponent {
  @Input() title: string = '';
  @Input() description: string = '';
  @Input() footerText: string = '';
}
