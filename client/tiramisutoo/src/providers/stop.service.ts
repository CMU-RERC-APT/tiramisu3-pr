import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';
import { catchError } from 'rxjs/operators/catchError';

import { UrlGenerator } from './url-generator.service';
import { StringUtilityService } from './string-utility.service';

import { Stop } from '../model/stop';
import { JsonResponse } from '../model/json-response';

@Injectable()
export class StopService {

    private servletPath: string = 'stop';

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator, private stringUtility: StringUtilityService) {
        
    }

    getStop(stopId: string) {

        let fullServletPath: string = `${this.servletPath}/${stopId}.json`;

        let stopUrl = this.urlGenerator.generateOBAUrl(fullServletPath, {});
        return this.http.get(stopUrl).pipe(
            map(
                (res: JsonResponse) => {
                    //let data = res.json().data;
                    //let stop: Stop = JSON.parse(JSON.stringify(data));
                    let stop = res.data;
                    stop.agencyId = stop.id.slice(0, stop.id.indexOf('_'));
                    stop.name = this.stringUtility.toTitleCase(stop.name);
                    return stop;
                }
            ),
            catchError(this.handleError));
    }

    private handleError (error: any) {
        let errMsg = (error.message) ? error.message :
            error.status ? `${error.status} - ${error.statusText}` : 'Server error';
        console.error(errMsg); // log to console instead
        return Observable.throw(errMsg);
    }    
}
