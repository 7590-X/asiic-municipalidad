import { Directive, ElementRef, HostListener, inject } from '@angular/core';
import { NgControl } from '@angular/forms';

@Directive({
  selector: '[appOnlyDigits]',
  standalone: true,
})
export class OnlyDigitsDirective {
  private el = inject<ElementRef<HTMLInputElement>>(ElementRef);
  private ngControl = inject(NgControl, { optional: true });

  // Bloquea que se escriba cualquier cosa que no sea dígito (incluye 'e', '+', '-', '.', ',')
  @HostListener('beforeinput', ['$event'])
  onBeforeInput(e: InputEvent): void {
    if (e.inputType === 'insertText' && e.data != null && /\D/.test(e.data)) {
      e.preventDefault();
    }
  }

  @HostListener('input')
  onInput(): void {
    const input = this.el.nativeElement;
    const max = input.maxLength > 0 ? input.maxLength : Infinity;
    const limpio = input.value.replace(/\D/g, '').slice(0, max);

    if (limpio !== input.value) {
      input.value = limpio;
      this.ngControl?.control?.setValue(limpio, { emitEvent: true });
    }
  }
}
