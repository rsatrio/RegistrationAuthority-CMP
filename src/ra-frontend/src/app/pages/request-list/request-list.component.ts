import { BasicResp } from '@/model/BasicResp';
import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { CommonService } from '@services/common.service';
import { SubmittedCertRequestService } from '@services/submitted-cert-request.service';
import { ColumnMode, DatatableComponent } from '@swimlane/ngx-datatable';
import { FileSaverService } from 'ngx-filesaver';
import { ToastrService } from 'ngx-toastr';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-request-list',
  templateUrl: './request-list.component.html',
  styleUrls: ['./request-list.component.scss']
})
export class RequestListComponent implements OnInit {
  rows = [];

  // columns = [{ prop: 'name' }, { name: 'Age' }, { name: 'Gender' }];
  // @ViewChild(DatatableComponent) table: DatatableComponent;

  // ColumnMode = ColumnMode;


  constructor(private dataService: SubmittedCertRequestService,
    private toast: ToastrService,
    private fileSave: FileSaverService,
    private router: Router,
    public commonService:CommonService,
    ) {
    // this.rows=[
    //   {
    //     "id": 0,
    //     "name": "Ramsey Cummings",
    //     "gender": "male",
    //     "age": 52,
    //     "address": {
    //       "state": "South Carolina",
    //       "city": "Glendale"
    //     }
    //   },
    //   {
    //     "id": 1,
    //     "name": "Stefanie Huff",
    //     "gender": "female",
    //     "age": 70,
    //     "address": {
    //       "state": "Arizona",
    //       "city": "Beaverdale"
    //     }
    //   },
    //   {
    //     "id": 2,
    //     "name": "Mabel David",
    //     "gender": "female",
    //     "age": 52,
    //     "address": {
    //       "state": "New Mexico",
    //       "city": "Grazierville"
    //     }
    //   },
    //   {
    //     "id": 3,
    //     "name": "Frank Bradford",
    //     "gender": "male",
    //     "age": 61,
    //     "address": {
    //       "state": "Wisconsin",
    //       "city": "Saranap"
    //     }
    //   },
    //   {
    //     "id": 4,
    //     "name": "Forbes Levine",
    //     "gender": "male",
    //     "age": 34,
    //     "address": {
    //       "state": "Vermont",
    //       "city": "Norris"
    //     }
    //   },
    //   {
    //     "id": 5,
    //     "name": "Santiago Mcclain",
    //     "gender": "male",
    //     "age": 38,
    //     "address": {
    //       "state": "Montana",
    //       "city": "Bordelonville"
    //     }
    //   },
    //   {
    //     "id": 6,
    //     "name": "Merritt Booker",
    //     "gender": "male",
    //     "age": 33,
    //     "address": {
    //       "state": "New Jersey",
    //       "city": "Aguila"
    //     }
    //   },
    // ];


  }

  ngOnInit(): void {
    let resp: Observable<BasicResp> = this.dataService.getData();
    resp.subscribe((response) => {
      // console.log(response);
      if (response.statusOk) {
        let data1 = response.data[0];

        this.rows = JSON.parse(data1);
        // console.log(this.rows);
      }
      else {
        this.toast.error('Failed to retrieve data. Please try again later.');
      }

    },
      (error) => {
        this.toast.error('Failed to retrieve data. Please try again later.');

      });



  }


  async onSelectBlue(row: any) {
    await localStorage.setItem('request-detail-admin', JSON.stringify(row));
    this.router.navigate(['/request-list-detail-admin']);
  }

  returnDate(data: any): any {
    let int1: number = data as number;
    return new Date(int1);
  }

}
