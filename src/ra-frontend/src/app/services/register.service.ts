import { BasicResp } from '@/model/BasicResp';
import { RegistrationRequest } from '@/model/RegistrationRequest';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'environments/environment.prod';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RegisterService {

  constructor(private http : HttpClient) { }

  public registerNow(email:string,password:string):Observable<BasicResp> {

    const headers = new HttpHeaders()
      .set("Content-Type", "application/json");

    let req:RegistrationRequest=new RegistrationRequest();
    req.email=email;
    req.password=password;
    
    let resp: Observable<BasicResp> = this.http.post<BasicResp>(environment.registerUrl,
      req,
      { headers });

      return resp;

  }
}
