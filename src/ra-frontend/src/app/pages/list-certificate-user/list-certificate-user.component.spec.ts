import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListCertificateUserComponent } from './list-certificate-user.component';

describe('ListCertificateUserComponent', () => {
  let component: ListCertificateUserComponent;
  let fixture: ComponentFixture<ListCertificateUserComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ListCertificateUserComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ListCertificateUserComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
