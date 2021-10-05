import { BasicResp } from '@/model/BasicResp';
import { CertRequest } from '@/model/CertRequest';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'environments/environment.prod';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CertificateService {

  constructor(private http: HttpClient) { }


  public postUrl(url: string, req: any): Observable<BasicResp> {
    let token: string = localStorage.getItem('token');

    const headers = new HttpHeaders()
      .set("Content-Type", "application/json")
      .set("Authorization", "Bearer " + token);

    let resp: Observable<BasicResp> = this.http.post<BasicResp>(url,
      req,
      { headers });

    return resp;


  }

  public revokeCert(certId: string) {
    let token: string = localStorage.getItem('token');

    const headers = new HttpHeaders()
      .set("Content-Type", "application/json")
      .set("Authorization", "Bearer " + token);

    const req = {
      id: certId,
    }

    let resp: Observable<BasicResp> = this.http.post<BasicResp>(environment.revokeCertUrl,
      req,
      { headers });

    return resp;
  }

  public getUrl(url: string) {

    let token: string = localStorage.getItem('token');
    const headers = new HttpHeaders()
      .set("Authorization", "Bearer " + token);

    let resp: Observable<BasicResp> = this.http.get<BasicResp>
      (url, { headers });

    return resp;

  }



}
