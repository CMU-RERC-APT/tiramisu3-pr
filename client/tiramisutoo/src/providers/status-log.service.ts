import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { map } from 'rxjs/operators/map';

import { UrlGenerator } from './url-generator.service';
import { StringUtilityService } from './string-utility.service';


@Injectable()
export class StatusLogService{
    private servletPath : string = "StatusLogServlet";

    constructor(private http : HttpClient, private urlGenerator : UrlGenerator,
                private stringUtilityService : StringUtilityService){

    }

    logStatus(device_id : string, device_platform : string, appVersion: string, screenReaderOn: boolean, locationAvailable: boolean){
        var argMap = {'device_id' : device_id,
                      'device_platform' : device_platform,
                      'app_version' : appVersion,
                      'screen_reader_on' : screenReaderOn,
                      'location_available' : locationAvailable
                     };

        //for (var attrname in optionalArgs) {argMap[attrname] = optionalArgs[attrname]};

        let logStatusUrl = this.urlGenerator.generateTiramisuUrl(this.servletPath,
                                                                 argMap);
        //console.log(logStatusUrl);

        return this.http.get(logStatusUrl).pipe(map(this.writeSuccess)).subscribe(
            (data)=>{
                console.log("status log service data: ", data);
            }, (error) => {
                console.log("status log service failed:", error)
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
