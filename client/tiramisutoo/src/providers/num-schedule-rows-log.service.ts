import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { map } from 'rxjs/operators/map';

import { UrlGenerator } from './url-generator.service';
import { StringUtilityService } from './string-utility.service';


@Injectable()
export class NumScheduleRowsLogService{
    private servletPath : string = "NumScheduleRowsLogServlet";

    constructor(private http : HttpClient, private urlGenerator : UrlGenerator,
                private stringUtilityService : StringUtilityService){

    }

    logNumRow(device_id : string, user_lat: number, user_lon: number, totalRows: number, numFavorite: number){
        var argMap = {'device_id' : device_id,
                      'user_lat': user_lat,
                      'user_lon': user_lon,
                      'total_rows': totalRows,
                      'num_favorite': numFavorite,
                     };

        //for (var attrname in optionalArgs) {argMap[attrname] = optionalArgs[attrname]};

        let logNumRowsUrl = this.urlGenerator.generateTiramisuUrl(this.servletPath,
                                                                 argMap);
        console.log(logNumRowsUrl);

        return this.http.get(logNumRowsUrl).pipe(map(this.writeSuccess)).subscribe(
            (data)=>{
                console.log("num row log service data: ", data);
            }, (error) => {
                console.log("num row log service failed:", error)
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
