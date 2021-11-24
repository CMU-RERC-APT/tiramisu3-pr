import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { UrlGenerator } from './url-generator.service';
import { StringUtilityService } from './string-utility.service';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';

import { JsonResponse } from '../model/json-response';


@Injectable()
export class WriteSettingsService {

    private servletPath: string = "WriteSettingsServlet";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator) {
    }
    writeSettings(device_id: string, user_lat: number, user_lon: number, disability_info: string){
        console.log(disability_info);
        let writeSettingsUrl = this.urlGenerator.generateTiramisuUrl(this.servletPath, { 'device_id': device_id, 
                                                                                         'user_lat': user_lat, 
                                                                                         'user_lon': user_lon,
                                                                                         'disability_info': disability_info});
        console.log(writeSettingsUrl);
        return this.http.get(writeSettingsUrl).pipe(map(this.writeSuccess));
    } 
    private writeSuccess(res) {
        console.log("writing eventually succeeds");
    }
}