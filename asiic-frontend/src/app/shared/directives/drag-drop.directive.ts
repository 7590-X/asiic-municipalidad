import { Directive, EventEmitter, HostBinding, HostListener, Output } from '@angular/core';

@Directive({
  selector: '[appDragDrop]',
  standalone: true
})
export class DragDropDirective {
  @Output() fileDropped = new EventEmitter<File[]>();
  @HostBinding('class.fileover') fileOver: boolean = false;

  // Manejar Drag Over (cuando el archivo pasa por encima del contenedor)
  @HostListener('dragover', ['$event']) onDragOver(evt: DragEvent) {
    evt.preventDefault();
    evt.stopPropagation();
    this.fileOver = true;
  }

  // Manejar Drag Leave (cuando el archivo sale del contenedor)
  @HostListener('dragleave', ['$event']) public onDragLeave(evt: DragEvent) {
    evt.preventDefault();
    evt.stopPropagation();
    this.fileOver = false;
  }

  // Manejar Drop (cuando se suelta el archivo)
  @HostListener('drop', ['$event']) public ondrop(evt: DragEvent) {
    evt.preventDefault();
    evt.stopPropagation();
    this.fileOver = false;
    
    const files = evt.dataTransfer?.files;
    if (files && files.length > 0) {
      const filesArray = Array.from(files);
      this.fileDropped.emit(filesArray);
    }
  }
}
