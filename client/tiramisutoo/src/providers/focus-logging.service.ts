import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { map } from 'rxjs/operators/map';

import { UrlGenerator } from './url-generator.service';
import { StringUtilityService } from './string-utility.service';


@Injectable()
export class FocusLogService{
  private servletPath : string = "FocusLogServlet";

  constructor(private http : HttpClient, private urlGenerator : UrlGenerator,
    private stringUtilityService : StringUtilityService){

  }

  logFocus(device_id : string, device_platform : string,
    user_lat : number, user_lon : number, event : string, optionalArgs){
    var argMap = {'device_id' : device_id,
      'device_platform' : device_platform,
      'user_lat' : user_lat,
      'user_lon' : user_lon,
      'event' : event};

    for (var attrname in optionalArgs) {argMap[attrname] = optionalArgs[attrname]};

    let logFocusUrl = this.urlGenerator.generateTiramisuUrl(this.servletPath,
      argMap);

      return this.http.get(logFocusUrl).pipe(map(this.writeSuccess)).subscribe(
                (data)=>{
                    console.log("focus log service data: ", data);
                }, (error) => {
                    console.log("focus log service failed:", error)
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
