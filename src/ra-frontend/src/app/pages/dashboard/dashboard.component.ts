import { BasicResp } from '@/model/BasicResp';
import { Component, OnInit } from '@angular/core';
import { CertificateService } from '@services/certificate.service';
import { CommonService } from '@services/common.service';
import { environment } from 'environments/environment.prod';
import { ToastrService } from 'ngx-toastr';
import { Observable } from 'rxjs';

@Component({
    selector: 'app-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

    constructor(private httpService: CertificateService,
        private toast: ToastrService,
        public commonService:CommonService,) { }
    data1 = [];
    ngOnInit() {
        
        let resp: Observable<BasicResp> = this.httpService.getUrl(environment.dashboardUrl);
        resp.subscribe((response) => {
            // console.log(response);
            if (response.statusOk) {
                this.data1 = response.data;
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
