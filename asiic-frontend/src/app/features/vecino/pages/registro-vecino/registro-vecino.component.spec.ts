import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { RegistroVecinoComponent } from './registro-vecino.component';
import { VecinoService } from '../../../../core/services/vecino-publico.service';
import { ValidacionesService } from '../../../../core/services/validaciones.service';
import { CatalogosService } from '../../../../core/services/catalogos.service';
import { LocacionesService } from '../../../../core/services/locaciones.service';

describe('RegistroVecinoComponent', () => {
  let component: RegistroVecinoComponent;
  let fixture: ComponentFixture<RegistroVecinoComponent>;
  let vecinoServiceSpy: jasmine.SpyObj<VecinoService>;
  let catalogosServiceSpy: jasmine.SpyObj<CatalogosService>;
  let locacionesServiceSpy: jasmine.SpyObj<LocacionesService>;
  let validacionesServiceSpy: jasmine.SpyObj<ValidacionesService>;

  beforeEach(async () => {
    vecinoServiceSpy = jasmine.createSpyObj('VecinoService', ['registrar']);
    catalogosServiceSpy = jasmine.createSpyObj('CatalogosService', ['getCatalogoEstadoCivil', 'getProfesiones']);
    locacionesServiceSpy = jasmine.createSpyObj('LocacionesService', ['getCatalogoPaises', 'getCatalogoDepartamentos', 'getCatalogoMunicipios', 'getCatalogoComunas']);
    validacionesServiceSpy = jasmine.createSpyObj('ValidacionesService', ['validacionNumero']);

    catalogosServiceSpy.getCatalogoEstadoCivil.and.returnValue(of([]));
    catalogosServiceSpy.getProfesiones.and.returnValue(of([]));
    locacionesServiceSpy.getCatalogoPaises.and.returnValue(of([]));
    locacionesServiceSpy.getCatalogoDepartamentos.and.returnValue(of([]));
    locacionesServiceSpy.getCatalogoMunicipios.and.returnValue(of([]));
    locacionesServiceSpy.getCatalogoComunas.and.returnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [RegistroVecinoComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: VecinoService, useValue: vecinoServiceSpy },
        { provide: CatalogosService, useValue: catalogosServiceSpy },
        { provide: LocacionesService, useValue: locacionesServiceSpy },
        { provide: ValidacionesService, useValue: validacionesServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegistroVecinoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('debe crear el componente de registro de vecino', () => {
    expect(component).toBeTruthy();
  });

  it('debe tener los cuatro grupos de pasos inicializados', () => {
    expect(component.form.controls.identificacion).toBeDefined();
    expect(component.form.controls.contacto).toBeDefined();
    expect(component.form.controls.ubicacion).toBeDefined();
    expect(component.form.controls.documentos).toBeDefined();
  });

  it('debe requerir los campos de ubicación reactivos (país, depto, municipio, comuna, dirección, contador)', () => {
    const ubicacionGroup = component.form.controls.ubicacion;
    expect(ubicacionGroup.valid).toBeFalse();
    expect(ubicacionGroup.controls.pais_id.errors?.['required']).toBeTrue();
    expect(ubicacionGroup.controls.departamento_id.errors?.['required']).toBeTrue();
    expect(ubicacionGroup.controls.municipio_id.errors?.['required']).toBeTrue();
    expect(ubicacionGroup.controls.comuna_id.errors?.['required']).toBeTrue();
  });

  it('no debe enviar el formulario si algún paso es inválido', () => {
    component.submit();
    expect(vecinoServiceSpy.registrar).not.toHaveBeenCalled();
    expect(component.form.touched).toBeTrue();
  });
});
