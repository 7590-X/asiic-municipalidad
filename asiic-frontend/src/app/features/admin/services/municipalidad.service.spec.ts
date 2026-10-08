import { TestBed } from '@angular/core/testing';

import { MunicipalidadService } from './municipalidad.service';

describe('MunicipalidadService', () => {
  let service: MunicipalidadService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MunicipalidadService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
