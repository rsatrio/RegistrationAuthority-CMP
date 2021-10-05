import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListCertificateAdminComponent } from './list-certificate-admin.component';

describe('ListCertificateAdminComponent', () => {
  let component: ListCertificateAdminComponent;
  let fixture: ComponentFixture<ListCertificateAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ListCertificateAdminComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ListCertificateAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
