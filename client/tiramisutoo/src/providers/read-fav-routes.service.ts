import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';

import { UrlGenerator } from './url-generator.service';

import { DisplayedRoute } from '../model/displayed-route';

@Injectable()
export class ReadFavRoutesService {

    private servletPath: string = "ReadFavRoutesServlet";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator) {
    }

    getFavRoutes(device_id: string, user_lat: number, user_lon: number) {

      let argMap = {'device_id': device_id,
                      'user_lat': user_lat,
                      'user_lon': user_lon};

    	let readRoutesUrl = this.urlGenerator.generateTiramisuUrl(this.servletPath, argMap);
    	console.log(readRoutesUrl);

    	return this.http.get(readRoutesUrl).pipe(map(this.extractRoutes));
	
    }

    private extractRoutes(res) {
    	//let body = res.json();
    	let favRoutes = res.data;

    	favRoutes = favRoutes.map(
    	    route => {
                let favRoute: DisplayedRoute = { routeId: route.route_id, routeShortName: route.route_short_name, agencyId: route.agency_id, selector: "system" };
                return favRoute;
            });

    	let favRouteMap: { [key: string]: DisplayedRoute } = {};

    	for (let route of favRoutes) {
    	    favRouteMap[route.routeId] = route;
    	}

    	return favRouteMap;
	
    }
}
