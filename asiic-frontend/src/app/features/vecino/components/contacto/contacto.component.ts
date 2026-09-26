import { Component, Input } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { OnlyDigitsDirective } from '../../../../shared/directives/only-digits.directive';

@Component({
  selector: 'app-contacto',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ClarityModule, OnlyDigitsDirective],
  templateUrl: './contacto.component.html',
})
export class ContactoComponent {
  @Input({ required: true }) stepForm!: FormGroup;
}
