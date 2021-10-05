import { TestBed } from '@angular/core/testing';

import { SubmittedCertRequestService } from './submitted-cert-request.service';

describe('SubmittedCertRequestService', () => {
  let service: SubmittedCertRequestService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SubmittedCertRequestService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
