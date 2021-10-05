import { BasicResp } from '@/model/BasicResp';
import { CertRequest } from '@/model/CertRequest';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'environments/environment.prod';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SubmittedCertRequestService {

  constructor(private http:HttpClient) { }

  getData() {
    let token: string = localStorage.getItem('token');

    const headers = new HttpHeaders()      
      .set("Authorization", "Bearer " + token);
       
    let resp: Observable<BasicResp> = this.http.get<BasicResp>(environment.allSubmittedCertReqUrl,
      {headers});
      return resp;

  }

  public rejectCertRequest(reqId:string) {
    const headers = new HttpHeaders()
      .set("Content-Type", "application/json");
    
    const req={
      id:reqId,
      status:false,
    };
    let resp: Observable<BasicResp> = this.http.post<BasicResp>(environment.allSubmittedCertReqUrl,
      req,
      { headers });

    return resp;
  }

  public addCertificate(req: CertRequest): Observable<BasicResp> {
    const headers = new HttpHeaders()
      .set("Content-Type", "application/json");

    let resp: Observable<BasicResp> = this.http.post<BasicResp>(environment.addCertUrl,
      req,
      { headers });

      return resp;

    
  }

  public postUrl(url:string,req: any): Observable<BasicResp> {
    let token: string = localStorage.getItem('token');

    const headers = new HttpHeaders()
      .set("Content-Type", "application/json")
      .set("Authorization", "Bearer " + token);

    let resp: Observable<BasicResp> = this.http.post<BasicResp>(url,
      req,
      { headers });

      return resp;

    
  }
}
