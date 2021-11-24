import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { map } from 'rxjs/operators/map';

import { UrlGenerator } from './url-generator.service';
import { StringUtilityService } from './string-utility.service';

@Injectable()
export class ButtonLogService{
  private servletPath : string = "ButtonLogServlet";

  constructor(private http : HttpClient, private urlGenerator : UrlGenerator, 
    private stringUtilityService : StringUtilityService){

  }

  logButton(device_id : string, device_platform : string,
    user_lat : number, user_lon : number, page : string, button_type : string, optionalArgs){
    var argMap = {'device_id' : device_id,
      'device_platform' : device_platform,
      'user_lat' : user_lat,
      'user_lon' : user_lon,
      'page' : page,
      'button_type' : button_type};

    for (var attrname in optionalArgs) {argMap[attrname] = optionalArgs[attrname]};

    let logButtonUrl = this.urlGenerator.generateTiramisuUrl(this.servletPath, 
      argMap);

      return this.http.get(logButtonUrl).pipe(map(this.writeSuccess)).subscribe(
                (data)=>{
                    //console.log("button log service data: ", data);
                }, (error) => {
                    console.log("button log service failed:", error)
                });
  }

  private writeSuccess(res) {
      //let body = res.json();
      let rows_affected = res.data[0].rowsAffected;
      if (rows_affected == "1") {
          return true;
      } else {
          return false;
      }
  }

}
