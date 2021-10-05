import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnrollCertificateComponent } from "./enroll-certificate.component";

describe('TestFormlyComponent', () => {
  let component: EnrollCertificateComponent;
  let fixture: ComponentFixture<EnrollCertificateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EnrollCertificateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EnrollCertificateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
