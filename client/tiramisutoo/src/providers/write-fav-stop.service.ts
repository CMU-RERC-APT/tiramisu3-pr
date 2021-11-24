import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';

import { UrlGenerator } from './url-generator.service';
import { Stop } from '../model/stop';

@Injectable()
export class WriteFavStopService {

    private servletPath: string = "WriteFavStopServlet";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator) {
	
    }

    writeFavStop(device_id: string, user_lat: number, user_lon: number, stop: Stop) {

	let writeStopUrl = this.urlGenerator.generateTiramisuUrl(this.servletPath, {'device_id': device_id,
										    'user_lat': user_lat,
										    'user_lon': user_lon,
										    'agency_id': stop.agencyId,
										    'stop_id': stop.id,
										    'stop_name': stop.name,
										    'stop_lat': stop.lat,
										    'stop_lon': stop.lon});
	console.log(writeStopUrl);
	return this.http.get(writeStopUrl).pipe(map(this.writeSuccess));

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
