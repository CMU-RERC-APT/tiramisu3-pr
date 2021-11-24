import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';

import { UrlGenerator } from './url-generator.service';

import { Stop } from '../model/stop';


@Injectable()
export class ReadFavStopsService {

    private servletPath: string = "ReadFavStopsServlet";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator) {
	
    }

    getFavStops(device_id: string, user_lat: number, user_lon: number) {

	let readStopsUrl = this.urlGenerator.generateTiramisuUrl(this.servletPath, {'device_id': device_id,
									       'user_lat': user_lat,
									       'user_lon': user_lon});

	console.log(readStopsUrl);
	return this.http.get(readStopsUrl).pipe(map(this.extractStops));
	
    }

    private extractStops(res) {
	//let body = res.json();
	let favStops = res.data;

	favStops = favStops.map(
	    stop => {
		let favStop: Stop = { id: stop.stop_id, name: stop.stop_name, lat: stop.stop_lat, lon: stop.stop_lon, agencyId: stop.agency_id};
		return favStop;
	    });

	let favStopMap: { [key: string]: Stop } = {};

	for (let stop of favStops) {
	    favStopMap[stop.id] = stop;
	}

	return favStopMap;

    }
}
