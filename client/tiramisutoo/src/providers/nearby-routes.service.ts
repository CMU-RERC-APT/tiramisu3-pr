import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';
import { catchError } from 'rxjs/operators/catchError';

import {DisplayedRoute} from '../model/displayed-route';
import {UrlGenerator} from './url-generator.service';

@Injectable()
export class NearbyRoutesService {

    private servletPath: string = "routes-for-location.json";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator) {
    }

    getNearbyRoutes(lat: number, lon: number) {
        let radius = 500;
        let maxCount = 100;
        
        let getNearbyRoutesUrl = this.urlGenerator.generateOBAUrl(this.servletPath,
                                                                  { 'lat': lat, 'lon': lon, 'radius': radius, 'maxCount': maxCount });
        
        return this.http.get(getNearbyRoutesUrl).pipe(
            map(this.extractRouteList),
            catchError(this.handleError));
    }
    
    private extractRouteList(res) {
        //let body = res.json();
        let routeList = res.data.list;
        return routeList.map(route => {
            let shortName = (route.shortName === '') ? route.longName : route.shortName;
            let longName = (route.longName === '') ? route.shortName : route.longName;
            let displayedRoute: DisplayedRoute = { routeId: route.id, routeShortName: shortName, routeLongName: longName, agencyId: route.agencyId };
            return displayedRoute;
        });
    }

    private handleError(error: any) {
        let errMsg = (error.message) ? error.message :
            error.status ? `${error.status} - ${error.statusText}` : 'Server error';
        console.error(errMsg); // log to console instead
        return Observable.throw(errMsg);
    }
}
