
import { Injectable } from '@angular/core';
import { JwtHelperService } from '@auth0/angular-jwt';

@Injectable({
  providedIn: 'root'
})
export class CommonService {

  constructor() { }

  public async convertBase64ToBlob(fileType:string,fileBase64:string):Promise<Blob>  {
    const base64Js = "data:"+fileType+";base64," + fileBase64;

    const base64Resp = await fetch(base64Js);
    return base64Resp.blob();    
    
  }

  public convertDate(dateString:any):any  {
    return new Date(dateString);
  }

  public getLoggedInEmail():string {
    let helper=new JwtHelperService();
    return helper.decodeToken(localStorage.getItem('token')).email;
  }

  public getLoggedInRole():string {
    let helper=new JwtHelperService();
    return helper.decodeToken(localStorage.getItem('token')).role;
  }
}
