import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';

import { UrlGenerator } from './url-generator.service';


@Injectable()
export class ReadSearchHistoryService {

    private servletPath: string = "ReadSearchHistoryServlet";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator) {
	
    }

    getSearchHistory(device_id: string) {

	let readHistoryUrl = this.urlGenerator.generateTiramisuUrl(this.servletPath, {'device_id': device_id});

	return this.http.get(readHistoryUrl).pipe(map(this.extractRecentSearch));

    }

    private extractRecentSearch(res) {
	//let body = res.json();
	let recentList = res.data;

	recentList = recentList.map(
	    recent => {
	        return {
	    	    name: recent.query,
	    	    place_lat:recent.place_lat,
	    	    place_lon:recent.place_lon
	        };
	    });


	return recentList;
	
    }

    
}
