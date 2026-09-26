import { Component, Input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { UppercaseDirective } from '../../../../shared/directives/uppercase.directive';

@Component({
  selector: 'app-documentos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ClarityModule, UppercaseDirective],
  templateUrl: './documentos.component.html',
})
export class DocumentosComponent {
  @Input({ required: true }) stepForm!: FormGroup;
}
