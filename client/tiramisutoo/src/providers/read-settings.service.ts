import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { UrlGenerator } from './url-generator.service';
import { StringUtilityService } from './string-utility.service';
import { Observable } from 'rxjs/Observable';

import 'rxjs/add/operator/map';
import { JsonResponse } from '../model/json-response';
import {Stop} from '../model/stop';

@Injectable()
export class ReadSettingsService {

    private servletPath: string = "ReadSettingsServlet";
    private result:Object;
    constructor(private http: HttpClient, private urlGenerator: UrlGenerator) {
    }
    readSettings(device_id: string, user_lat: number, user_lon: number) 
    {
        let readSettingsUrl = this.urlGenerator.generateTiramisuUrl(this.servletPath, {'device_id': device_id,
                                             'user_lat': user_lat,
                                             'user_lon': user_lon});
        return this.http.get(readSettingsUrl).map(this.extractSettings);
    }

    private extractSettings(result){
        var settingsDictionary = (result.data)[0];
        for (var key in settingsDictionary){
            if (settingsDictionary[key] == 't'){
                settingsDictionary[key] = true;
            }
            else if(settingsDictionary[key] == 'f'){
                settingsDictionary[key] = false;
            }
            else{
                settingsDictionary[key] = false;
            }
        }
        return settingsDictionary;
    }
}
