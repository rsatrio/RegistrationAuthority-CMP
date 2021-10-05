import { BrowserModule } from '@angular/platform-browser';
import { LOCALE_ID, NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from '@/app-routing.module';
import { AppComponent } from './app.component';
import { MainComponent } from '@modules/main/main.component';
import { LoginComponent } from '@modules/login/login.component';
import { HeaderComponent } from '@modules/main/header/header.component';
import { FooterComponent } from '@modules/main/footer/footer.component';
import { MenuSidebarComponent } from '@modules/main/menu-sidebar/menu-sidebar.component';
import { BlankComponent } from '@pages/blank/blank.component';
import { AbstractControl, FormControl, ReactiveFormsModule, ValidationErrors } from '@angular/forms';
import { ProfileComponent } from '@pages/profile/profile.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RegisterComponent } from '@modules/register/register.component';
import { DashboardComponent } from '@pages/dashboard/dashboard.component';
import { ToastrModule } from 'ngx-toastr';
import { MessagesDropdownMenuComponent } from '@modules/main/header/messages-dropdown-menu/messages-dropdown-menu.component';
import { NotificationsDropdownMenuComponent } from '@modules/main/header/notifications-dropdown-menu/notifications-dropdown-menu.component';
import { AppButtonComponent } from './components/app-button/app-button.component';

import { CommonModule, registerLocaleData } from '@angular/common';
import localeEn from '@angular/common/locales/en';
import { UserDropdownMenuComponent } from '@modules/main/header/user-dropdown-menu/user-dropdown-menu.component';
import { ForgotPasswordComponent } from '@modules/forgot-password/forgot-password.component';
import { RecoverPasswordComponent } from '@modules/recover-password/recover-password.component';
import { LanguageDropdownComponent } from '@modules/main/header/language-dropdown/language-dropdown.component';
import { PrivacyPolicyComponent } from './modules/privacy-policy/privacy-policy.component';
import { FormlyFieldConfig, FormlyModule } from '@ngx-formly/core';
import { FormlyMaterialModule } from '@ngx-formly/material';

import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { BlockUIModule } from 'ng-block-ui';
import { EnrollCertificateComponent } from '@pages/enroll-certificate/enroll-certificate.component';

import { MatFormFieldModule } from '@angular/material/form-field';
import { FormlyFieldFile } from '@components/file-type.component';
import { FileValueAccessor } from '@components/file-value-accessor';

import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { FileSaverModule } from 'ngx-filesaver';
import {MatDialog, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import { RequestListComponent } from '@pages/request-list/request-list.component';
import { AdminCertRequestDetailComponent, AdminCertRequestDetailComponentConfirm } from '@pages/admin-cert-request/admin-cert-request-detail.component';
import { ListCertificateUserComponent } from './pages/list-certificate-user/list-certificate-user.component';
import { ListCertificateAdminComponent } from './pages/list-certificate-admin/list-certificate-admin.component';
import { ChangePasswordComponent } from './pages/change-password/change-password.component';




registerLocaleData(localeEn, 'en-EN');

export function fieldMatchValidator(control: AbstractControl) {
    const { password, passwordConfirm } = control.value;
  
    // avoid displaying the message error when values are empty
    if (!passwordConfirm || !password) {
      return null;
    }
  
    if (passwordConfirm === password) {
      return null;
    }
  
    return { fieldMatch: { message: 'New Password Not Matching' } };
  }
  

export function minlengthValidationMessage(err, field) {
    return `Should have atleast ${field.templateOptions.minLength} characters`;
}

export function maxlengthValidationMessage(err, field) {
    return `This value should be less than ${field.templateOptions.maxLength} characters`;
}

export function fileUploadValidator(control: FormControl, field: FormlyFieldConfig, options = {}):
    ValidationErrors {
    if (control.value) {
        let files: FileList = control.value as FileList;
        let file: File = files.item(0);
        if (file.type != 'application/pdf') {
            return { 'file-type': { message: `File must be PDF` } };
        }
        if(file.size>1000000)    {
            return { 'file-type': { message: `File must be below 1 MB` }};
        }
    }

}


@NgModule({
    declarations: [
        AppComponent,
        MainComponent,
        LoginComponent,
        HeaderComponent,
        FooterComponent,
        MenuSidebarComponent,
        BlankComponent,
        ProfileComponent,
        RegisterComponent,
        DashboardComponent,
        MessagesDropdownMenuComponent,
        NotificationsDropdownMenuComponent,
        AppButtonComponent,
        UserDropdownMenuComponent,
        ForgotPasswordComponent,
        RecoverPasswordComponent,
        LanguageDropdownComponent,
        PrivacyPolicyComponent,
        EnrollCertificateComponent,
        FileValueAccessor,
        FormlyFieldFile,
        RequestListComponent,
        AdminCertRequestDetailComponent,
        AdminCertRequestDetailComponentConfirm,
        ListCertificateUserComponent,
        ListCertificateAdminComponent,
        ChangePasswordComponent,
    ],
    imports: [
        BrowserModule,
        HttpClientModule,
        AppRoutingModule,
        ReactiveFormsModule,
        BrowserAnimationsModule,
        ToastrModule.forRoot({
            timeOut: 3000,
            positionClass: 'toast-bottom-right',
            preventDuplicates: true
        }),
        FormlyModule.forRoot({
            validators: [
                { name: 'file-type', validation: fileUploadValidator },
                { name: 'fieldMatch', validation: fieldMatchValidator },
            ],
            validationMessages: [
                { name: 'required', message: 'This field is required' },
                { name: 'pattern', message: 'Not a valid pattern for this field' },
                { name: 'minlength', message: minlengthValidationMessage },
                { name: 'maxlength', message: maxlengthValidationMessage },
            ],
            types: [{ name: 'file', component: FormlyFieldFile, wrappers: ['form-field'] }
            ],

        }),
        FormlyMaterialModule,
        MatProgressSpinnerModule,
        BlockUIModule.forRoot(),
        MatFormFieldModule,
        NgxDatatableModule,
        FileSaverModule,
        MatDialogModule,       
        

    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule { }
