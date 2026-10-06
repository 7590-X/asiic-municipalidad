import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegistroMunicipalidadComponent } from './registro-municipalidad.component';

describe('RegistroMunicipalidadComponent', () => {
  let component: RegistroMunicipalidadComponent;
  let fixture: ComponentFixture<RegistroMunicipalidadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegistroMunicipalidadComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegistroMunicipalidadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
