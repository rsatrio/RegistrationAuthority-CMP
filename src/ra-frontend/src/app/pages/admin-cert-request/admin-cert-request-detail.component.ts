import { BasicResp } from '@/model/BasicResp';
import { Component, Inject, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ProgressSpinnerMode } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';
import { FormlyFieldConfig, FormlyFormOptions } from '@ngx-formly/core';
import { CertificateService } from '@services/certificate.service';
import { CommonService } from '@services/common.service';
import { SubmittedCertRequestService } from '@services/submitted-cert-request.service';
import { environment } from 'environments/environment.prod';
import { BlockUI, NgBlockUI } from 'ng-block-ui';
import { FileSaverService } from 'ngx-filesaver';
import { ToastrService } from 'ngx-toastr';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-admin-cert-request-detail',
  templateUrl: './admin-cert-request-detail.component.html',
  styleUrls: ['./admin-cert-request-detail.component.scss']
})
export class AdminCertRequestDetailComponent implements OnInit {

  @BlockUI() blockUI: NgBlockUI;
  clicked: boolean = false;
  isSuccess: boolean = false;
  mode: ProgressSpinnerMode = 'indeterminate';
  form = new FormGroup({});
  constructor(private toast: ToastrService,
    private fileSave: FileSaverService,
    private dialog: MatDialog,
    private router: Router,
    private certRequest: SubmittedCertRequestService,
    private enrollRequest: CertificateService,
    private commonService:CommonService,
  ) { }

  ngOnInit(): void {
    this.model = JSON.parse(localStorage.getItem('request-detail-admin'));
  }

  model: any = {
  };

  options: FormlyFormOptions = {
    formState: {
      awesomeIsForced: false,
    },
  };
  fields: FormlyFieldConfig[] = [
    {
      key: 'email',
      type: 'input',
      templateOptions: {
        label: 'Email address',
        type: 'email',
        placeholder: 'Enter email',
        readonly: true,

      },
    },
    {
      key: 'common_name',
      type: 'input',
      templateOptions: {
        label: 'Common Name',
        type: 'text',
        placeholder: 'Enter Common Name',
        required: true,
        maxLength: 30,
        readonly: true,
      },
    },
    {
      key: 'state',
      type: 'input',
      templateOptions: {
        label: 'State',
        type: 'text',
        placeholder: 'Enter State',
        readonly: true,
      },
    },
    {
      key: 'country',
      type: 'input',
      templateOptions: {
        label: 'Country',
        type: 'text',
        readonly: true,
      },
    },

  ];

  async onFetchFile() {   
    const blob = await this.commonService.convertBase64ToBlob("application/pdf",this.model.identity)

    this.fileSave.save(blob, "identity.pdf");
  }

  async onAccept() {

    const dialogRef = this.dialog.open(AdminCertRequestDetailComponentConfirm, {
      width: '250px',
      data: { classBg: 'text-success', accept: true, message: 'Accept' }
    });
    dialogRef.afterClosed().subscribe((result) => {
      //Accept Enroll
      if (result.accept == true && result.type == 'Accept'
        && this.model.id != null) {
        this.blockUI.start();
        const req = {
          id: this.model.id,
        }
        let resp: Observable<BasicResp> = this.enrollRequest.postUrl(environment.enrollCertUrl,
          req);

        resp.subscribe((response) => {
          if (response.statusOk) {
            this.toast.success('Certificate Creation Successful');
            this.isSuccess = true;
            localStorage.removeItem('request-detail-admin');
            this.model = {};
            this.router.navigate(['/request-list']).then(() => {
              // window.location.reload();
              this.toast.success('Certificate Creation Successful');
            })
            // .then(()=>{
            //   this.toast.success('Certificate Creation Successful');
            // });

          }
          else {
            this.toast.error('Failed processing. Please try again later.');
          }
          this.clicked = false;
          this.blockUI.stop();
        },
          (error) => {
            this.toast.error('Failed processing. Please try again later.');
            this.clicked = false;
            this.blockUI.stop();
          });




      }
    });
  }

  async onReject() {

    const dialogRef = this.dialog.open(AdminCertRequestDetailComponentConfirm, {
      width: '250px',
      data: { classBg: 'text-danger', accept: false, message: 'Reject' }
    });
    dialogRef.afterClosed().subscribe((result) => {
      //Reject Enroll
      if (result.accept == true && result.type == 'Reject'
        && this.model.id != null) {
        this.blockUI.start();
        let url:string=environment.allSubmittedCertReqUrl;

        const req={
          id:this.model.id,
          status:false,
        };

        let resp: Observable<BasicResp> = this.certRequest.postUrl(url,req);

        resp.subscribe((response) => {
          if (response.statusOk) {
            this.toast.success('Certificate Request Rejected');
            this.isSuccess = true;
            localStorage.removeItem('request-detail-admin');
            this.model = {};
            this.router.navigate(['/request-list']).then(() => {
              // window.location.reload();     
              this.toast.success('Certificate Request Rejected');
            });
          }
          else {
            this.toast.error('Failed processing. Please try again later.');
          }
          this.clicked = false;
          this.blockUI.stop();
        },
          (error) => {
            this.toast.error('Failed processing. Please try again later.');
            this.clicked = false;
            this.blockUI.stop();
          });

      }
    });
  }


}


@Component({
  selector: 'app-admin-cert-request-detail-confirm',
  templateUrl: './cert-request-detail-confirm.component.html',
  // styleUrls: ['./admin-cert-request-detail.component.scss']
})
export class AdminCertRequestDetailComponentConfirm {

  @BlockUI() blockUI: NgBlockUI;

  constructor(
    public dialogRef: MatDialogRef<AdminCertRequestDetailComponentConfirm>,
    @Inject(MAT_DIALOG_DATA) public data: any) { }

  onYesClick() {

    const result = {
      accept: true,
      type: this.data.message
    };
    console.log(result);
    this.dialogRef.close(result);
  }

  onNoClick() {

    const result = {
      accept: false,
      type: this.data.message
    };
    console.log(result);
    this.dialogRef.close(result);
  }

}
