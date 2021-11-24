import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { UrlGenerator } from './url-generator.service';
import { StringUtilityService } from './string-utility.service';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';

import { JsonResponse } from '../model/json-response';


@Injectable()
export class RemoveSettingsService {

    private servletPath: string = "RemoveSettingsServlet";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator, private stringUtility: StringUtilityService) {
    }
    removeSettings(device_id: string, user_lat: number, user_lon: number, disability_info: string){
        let removeSettingsUrl = this.urlGenerator.generateTiramisuUrl(this.servletPath, { 'device_id': device_id, 'user_lat': user_lat, 'user_lon': user_lon,'disability_info': disability_info});
        console.log(removeSettingsUrl);
        return this.http.get(removeSettingsUrl).pipe(map(this.removeSuccess));
    } 
    private removeSuccess(res) 
    {
        console.log(res);
        //let body = res.json();
        // let rows_affected = res.data[0].rowsAffected;
        // if (rows_affected == "1") {
        //     return true;
        // } else {
        //     return false;
        // }
    }


}