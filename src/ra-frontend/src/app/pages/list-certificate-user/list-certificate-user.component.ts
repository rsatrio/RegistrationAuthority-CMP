import { BasicResp } from '@/model/BasicResp';
import { Component, Inject, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { CertificateService } from '@services/certificate.service';
import { CommonService } from '@services/common.service';
import { environment } from 'environments/environment.prod';
import { BlockUI, NgBlockUI } from 'ng-block-ui';
import { FileSaverService } from 'ngx-filesaver';
import { ToastrService } from 'ngx-toastr';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-list-certificate-user',
  templateUrl: './list-certificate-user.component.html',
  styleUrls: ['./list-certificate-user.component.scss']
})
export class ListCertificateUserComponent implements OnInit {
  @BlockUI() blockUI: NgBlockUI;
  rows = [];
  constructor(private toast: ToastrService,
    private certificateService: CertificateService,
    public commonService: CommonService,
    private dialog: MatDialog,  
    private router:Router,  
    private fileSave: FileSaverService,) { }


  ngOnInit(): void {
    let url:string=environment.userCertUrl;
    let resp: Observable<BasicResp> = this.certificateService.getUrl(url);
    resp.subscribe((response) => {
      // console.log(response);
      if (response.statusOk) {
        let data1 = response.data[0];
        this.rows = JSON.parse(data1);

      }
      else {
        this.toast.error('Failed to retrieve data. Please try again later.');
      }

    },
      (error) => {
        this.toast.error('Failed to retrieve data. Please try again later.');

      });


  }
   

  async onDownloadCert(cert: any) {
    // console.log(cert);
    const blob = await this.commonService.convertBase64ToBlob("text/plain", cert);
    this.fileSave.save(blob, "user.crt", "application/x-x509-user-cert");
  }

  async onRevoke(certId:string) {

    const dialogRef = this.dialog.open(RevokeComponentConfirm, {
      width: '250px',
      data: { classBg: 'text-success', accept: true, message: 'Accept' }
    });
    dialogRef.afterClosed().subscribe((result) => {
      //Accept Enroll
      if (result.accept == true) {
        this.blockUI.start();
        const req = {
          id: certId,
        }
        let resp: Observable<BasicResp> = this.certificateService.postUrl(environment.revokeCertUrl,req);

        resp.subscribe((response) => {
          if (response.statusOk) {
            let rowIndex:any=this.rows.findIndex((obj=>obj.id==certId));
            this.rows[rowIndex].status='Revoked';
           
            this.router.navigate(['/user-certificate']).then(() => {
              // window.location.reload();
              this.toast.success('Certificate Revocation Successful');
            })          
          }
          else {
            this.toast.error('Failed processing. Please try again later.');
          }
          
          this.blockUI.stop();
        },
          (error) => {
            this.toast.error('Failed processing. Please try again later.');            
            this.blockUI.stop();
          });




      }
    });
  }



}

@Component({
  selector: 'app-admin-cert-request-detail-confirm',
  templateUrl: './revoke-dialog-confirmation.html',
  // styleUrls: ['./admin-cert-request-detail.component.scss']
})
export class RevokeComponentConfirm {

  @BlockUI() blockUI: NgBlockUI;

  constructor(
    public dialogRef: MatDialogRef<RevokeComponentConfirm>,
    @Inject(MAT_DIALOG_DATA) public data: any) { }

  onYesClick() {

    const result = {
      accept: true,     
    };
    console.log(result);
    this.dialogRef.close(result);
  }

  onNoClick() {

    const result = {
      accept: false,

    };
    console.log(result);
    this.dialogRef.close(result);
  }
}
