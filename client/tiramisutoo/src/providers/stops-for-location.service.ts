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
export class StopsForLocationService {

    private servletPath: string = "stops-for-location.json";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator, private stringUtility: StringUtilityService) {
    }

    //get stops near the current location
    getStopsForLocation(lat: number, lon: number): Observable<Stop[]>  {
        let radius = 500;
        let maxCount = 100;

        let url = this.urlGenerator.generateOBAUrl(this.servletPath,
            { 'lat': lat, 'lon': lon, 'radius': radius, 'maxCount': maxCount });

        console.log(url);

        return this.http.get(url).pipe(
            map(
                (res: JsonResponse) => {
                    //map json data
                    //var stops = [];
                    //var data = res.json().data.list;
                    let stops = res.data.list
                    for (let stop of stops) {
                        //let stop: Stop = JSON.parse(JSON.stringify(current));
                        stop.agencyId = stop.id.slice(0, stop.id.indexOf('_'));
                        stop.name = this.stringUtility.toTitleCase(stop.name);
                        //stops.push(stop);
                    }
                    //console.log(stops);
                    return stops;
                }),
            catchError(this.handleError));
    }

    private handleError (error: any) {
        let errMsg = (error.message) ? error.message :
            error.status ? `${error.status} - ${error.statusText}` : 'Server error';
        console.error(errMsg); // log to console instead
        return Observable.throw(errMsg);
    }

}
