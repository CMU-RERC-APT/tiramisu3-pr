import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';

import { UrlGenerator } from './url-generator.service';
import { DisplayedRoute } from '../model/displayed-route';

@Injectable()
export class WriteFavRouteService {

    private servletPath: string = "WriteFavRouteServlet";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator) {
	
    }

    writeFavRoute(device_id: string, user_lat: number, user_lon: number, route: DisplayedRoute) {

	let writeRouteUrl = this.urlGenerator.generateTiramisuUrl(this.servletPath, {'device_id': device_id,
										     'user_lat': user_lat,
										     'user_lon': user_lon,
										     'agency_id': route.agencyId,
										     'route_id': route.routeId,
										     'route_short_name': route.routeShortName,
                                                                                     'event': 'write'});
	console.log(writeRouteUrl);

	return this.http.get(writeRouteUrl).pipe(map(this.writeSuccess));

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
