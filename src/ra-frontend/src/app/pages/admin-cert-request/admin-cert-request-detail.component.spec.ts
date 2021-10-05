import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminCertRequestDetailComponent } from './admin-cert-request-detail.component';

describe('AdminCertRequestDetailComponent', () => {
  let component: AdminCertRequestDetailComponent;
  let fixture: ComponentFixture<AdminCertRequestDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AdminCertRequestDetailComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AdminCertRequestDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
