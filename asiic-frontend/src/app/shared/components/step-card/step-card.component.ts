import { Component, Input } from '@angular/core';

export interface StepCardData {
  badge: number | string;
  title: string;
  description: string;
}

@Component({
  selector: 'app-step-card',
  standalone: true,
  imports: [],
  templateUrl: './step-card.component.html',
  styleUrl: './step-card.component.scss',
})
export class StepCardComponent {
  @Input({ required: true }) badge!: number | string;
  @Input({ required: true }) title!: string;
  @Input({ required: true }) description!: string;
}
