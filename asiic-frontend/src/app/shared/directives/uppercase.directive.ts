import { Directive, HostListener, ElementRef, inject } from '@angular/core';
import { NgControl } from '@angular/forms';

@Directive({
  selector: '[appUppercase]',
  standalone: true
})
export class UppercaseDirective {

  private el = inject(ElementRef);
  private ngControl = inject(NgControl, { optional: true, self: true });

  constructor() { }

  @HostListener('input', ['$event.target'])
  public onInput(target: EventTarget | null): void {
    const input = target as HTMLInputElement;
    if (!input) return;

    const start = input.selectionStart;
    const end = input.selectionEnd;

    const uppercased = input.value.toUpperCase();

    if (input.value !== uppercased) {
      input.value = uppercased;

      if (this.ngControl && this.ngControl.control) {
        this.ngControl.control.setValue(uppercased, { emitModelToViewChange: false });
      }

      // Restore cursor position
      if (start !== null && end !== null) {
        input.setSelectionRange(start, end);
      }
    }
  }
}
