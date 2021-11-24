import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';

import { UrlGenerator } from './url-generator.service';


@Injectable()
export class WriteSearchHistoryService {

    private servletPath: string = "WriteSearchHistoryServlet";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator) {
	
    }

    writeSearch(device_id: string, user_lat: number, user_lon: number, search) {

        let lat: number = search.place_lat;
        let lon: number = search.place_lon;
        let is_recent: boolean = search.is_recent ? search.is_recent: false;

	let writeRouteUrl = this.urlGenerator.generateTiramisuUrl(this.servletPath, {'device_id': device_id, 'user_lat': user_lat, 'user_lon': user_lon, 
										     'query': search.name, 'place_lat': lat, 'place_lon':lon, 'is_recent':is_recent});

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
