import { Directive, HostListener } from '@angular/core';
import { NgControl } from '@angular/forms';

@Directive({
  selector: '[appCapitalizeWords]',
  standalone: true
})
export class CapitalizeWordsDirective {
  constructor(private ngControl: NgControl) {}

  @HostListener('input', ['$event'])
  onInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = input.value;
    
    if (!value) return;
    
    // Save cursor position to prevent jumping
    const start = input.selectionStart;
    const end = input.selectionEnd;
    
    // Capitalize the first letter of every word
    const capitalized = value.replace(/\b[a-záéíóúüñ]/g, (char) => char.toUpperCase());
    
    // Update the form control value
    this.ngControl.control?.setValue(capitalized, { emitEvent: false });
    
    // Restore cursor position
    if (start !== null && end !== null) {
      input.setSelectionRange(start, end);
    }
  }
}
