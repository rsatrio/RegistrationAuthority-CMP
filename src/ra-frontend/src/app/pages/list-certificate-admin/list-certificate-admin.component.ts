import { BasicResp } from '@/model/BasicResp';
import { Component, OnInit } from '@angular/core';
import { CertificateService } from '@services/certificate.service';
import { CommonService } from '@services/common.service';
import { environment } from 'environments/environment.prod';
import { ToastrService } from 'ngx-toastr';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-list-certificate-admin',
  templateUrl: './list-certificate-admin.component.html',
  styleUrls: ['./list-certificate-admin.component.scss']
})
export class ListCertificateAdminComponent implements OnInit {

  rows=[];
  constructor(private certificateService:CertificateService,
    public commonService:CommonService,
    private toast:ToastrService,) { }

  ngOnInit(): void {

    let url:string=environment.adminCertUrl;
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

}
