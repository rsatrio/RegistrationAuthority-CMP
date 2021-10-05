import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {MainComponent} from '@modules/main/main.component';
import {BlankComponent} from '@pages/blank/blank.component';
import {LoginComponent} from '@modules/login/login.component';
import {ProfileComponent} from '@pages/profile/profile.component';
import {RegisterComponent} from '@modules/register/register.component';
import {DashboardComponent} from '@pages/dashboard/dashboard.component';
import {AuthGuard} from '@guards/auth.guard';
import {NonAuthGuard} from '@guards/non-auth.guard';
import {ForgotPasswordComponent} from '@modules/forgot-password/forgot-password.component';
import {RecoverPasswordComponent} from '@modules/recover-password/recover-password.component';
import {PrivacyPolicyComponent} from '@modules/privacy-policy/privacy-policy.component';

import { EnrollCertificateComponent } from '@pages/enroll-certificate/enroll-certificate.component';

import { RequestListComponent } from '@pages/request-list/request-list.component';
import { AdminCertRequestDetailComponent } from '@pages/admin-cert-request/admin-cert-request-detail.component';
import { ListCertificateUserComponent } from '@pages/list-certificate-user/list-certificate-user.component';
import { ListCertificateAdminComponent } from '@pages/list-certificate-admin/list-certificate-admin.component';
import { ChangePasswordComponent } from '@pages/change-password/change-password.component';


const routes: Routes = [
    {
        path: '',
        component: MainComponent,
        canActivate: [AuthGuard],
        canActivateChild: [AuthGuard],
        children: [
            {
                path: 'profile',
                component: ProfileComponent
            },
            {
                path: 'blank',
                component: BlankComponent
            },
            {
                path: '',
                component: DashboardComponent
            },
            {
                path: 'add-certificate',
                component: EnrollCertificateComponent
            },
            {
                path: 'request-list',
                component: RequestListComponent 
            },
            {
                path: 'request-list-detail-admin',
                component: AdminCertRequestDetailComponent
            },
            {
                path: 'user-certificate',
                component: ListCertificateUserComponent
            },
            {
                path: 'all-user-certificate',
                component: ListCertificateAdminComponent
            },
            {
                path: 'change-password',
                component: ChangePasswordComponent
            },
           
        ]
    },
    {
        path: 'login',
        component: LoginComponent,
        canActivate: [NonAuthGuard]
    },
    {
        path: 'register',
        component: RegisterComponent,
        canActivate: [NonAuthGuard]
    },
    {
        path: 'forgot-password',
        component: ForgotPasswordComponent,
        canActivate: [NonAuthGuard]
    },
    {
        path: 'recover-password',
        component: RecoverPasswordComponent,
        canActivate: [NonAuthGuard]
    },
    {
        path: 'privacy-policy',
        component: PrivacyPolicyComponent,
        canActivate: [NonAuthGuard]
    },
    {path: '**', redirectTo: ''}
];

@NgModule({
    imports: [RouterModule.forRoot(routes, {relativeLinkResolution: 'legacy'})],
    exports: [RouterModule]
})
export class AppRoutingModule {}
