import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { DragDropDirective } from '../../../../shared/directives/drag-drop.directive';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-evidencias',
  standalone: true,
  imports: [CommonModule, ClarityModule, DragDropDirective, ReactiveFormsModule],
  templateUrl: './evidencias.component.html'
})
export class EvidenciasComponent {
  @Input({ required: true }) stepForm!: FormGroup;
  @Input({ required: true }) tipoIncidencia: string = '';
  @Output() filesChanged = new EventEmitter<File[]>();

  files: File[] = [];
  maxFileSize = 10 * 1024 * 1024; // 10MB
  fileError = '';

  onFilesDropped(files: File[]): void {
    this.handleFiles(files);
  }

  onFileSelected(event: any): void {
    this.handleFiles(event.target.files);
  }

  handleFiles(files: FileList | File[]): void {
    this.fileError = '';
    const arrayFiles = Array.from(files);
    
    for (let file of arrayFiles) {
      if (file.size > this.maxFileSize) {
        this.fileError = `El archivo ${file.name} excede los 10MB permitidos.`;
        return;
      }
      if (!file.type.match(/image\/(png|jpeg|jpg)|application\/pdf/)) {
        this.fileError = `El archivo ${file.name} no es un formato permitido (PNG, JPG, PDF).`;
        return;
      }
      this.files.push(file);
    }
    this.filesChanged.emit(this.files);
  }

  removeFile(index: number): void {
    this.files.splice(index, 1);
    this.filesChanged.emit(this.files);
  }
}
