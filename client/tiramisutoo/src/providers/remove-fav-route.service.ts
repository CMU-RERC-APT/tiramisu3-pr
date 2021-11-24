import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';

import { UrlGenerator } from './url-generator.service';

import { DisplayedRoute } from '../model/displayed-route';

@Injectable()
export class RemoveFavRouteService {

    private servletPath: string = "RemoveFavRouteServlet";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator) {
	
    }

    removeFavRoute(device_id: string, user_lat: number, user_lon: number, route: DisplayedRoute, event: string) {

	let removeRouteUrl = this.urlGenerator.generateTiramisuUrl(this.servletPath, {'device_id': device_id,
										      'user_lat': user_lat,
										      'user_lon': user_lon,
										      'route_id': route.routeId,
										      'route_short_name': route.routeShortName,
										      'agency_id': route.agencyId,
										      'event': event});

	console.log(removeRouteUrl);
	return this.http.get(removeRouteUrl).pipe(map(this.removeSuccess));

    }

    private removeSuccess(res) {
	//let body = res.json();
	let rows_affected = res.data[0].rowsAffected;
	if (rows_affected == "1") {
	    return true;
	} else {
	    return false;
	}
	
    }
}
