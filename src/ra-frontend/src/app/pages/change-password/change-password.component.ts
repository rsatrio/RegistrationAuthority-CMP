import { BasicResp } from '@/model/BasicResp';
import { Component, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { CertificateService } from '@services/certificate.service';
import { environment } from 'environments/environment.prod';
import { BlockUI, NgBlockUI } from 'ng-block-ui';
import { ToastrService } from 'ngx-toastr';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.scss']
})
export class ChangePasswordComponent implements OnInit {

  @BlockUI() blockUI: NgBlockUI;
  model: any = {};
  form = new FormGroup({});
  options: FormlyFormOptions = {
    formState: {
      awesomeIsForced: false,
    },
  };

  fields: FormlyFieldConfig[] = [{
    validators: {
      validation: [
        { name: 'fieldMatch', options: { errorPath: 'passwordConfirm' } },
      ],
    },
    fieldGroup: [
      {
        key: 'oldPassword',
        type: 'input',
        templateOptions: {
          type: 'password',
          label: 'Old Password',
          placeholder: 'Old Password',
          required: true,
        },
      },
      {
        key: 'password',
        type: 'input',
        templateOptions: {
          type: 'password',
          label: 'Password',
          placeholder: 'Must be at betweem 10-30 characters',
          required: true,
          minLength: 10,
        },
      },
      {
        key: 'passwordConfirm',
        type: 'input',
        templateOptions: {
          type: 'password',
          label: 'Confirm Password',
          placeholder: 'Please re-enter your password',
          required: true,
        },
      },
    ],
  }];
  constructor(private httpService: CertificateService,
    private toast: ToastrService,
    private router: Router,
  ) { }

  ngOnInit(): void {
  }

  public onSubmit() {

    const req = {
      oldPassword: this.model.oldPassword,
      newPassword: this.model.password,
    }
    let resp: Observable<BasicResp> = this.httpService.postUrl(environment.changePasswordUrl,
      req);
    this.blockUI.start();
    resp.subscribe((response) => {
      console.log(response);
      if (response.statusOk) {
        localStorage.clear();
        this.router.navigate(['/login']).then(()=>{
          this.toast.success('Password successfully changed, please re-login');
        });
        
      }
      else {
        if (response.message) {
          this.toast.error(response.message);
        }
        else {
          this.toast.error('Failed to change Password');
        }
      }

      this.blockUI.stop();
    },
      (error) => {
        this.toast.error('Failed to change Password');

        this.blockUI.stop();
      });

  }
}
