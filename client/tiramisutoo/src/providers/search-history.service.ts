import { Injectable } from '@angular/core';

import { Observable } from 'rxjs/Observable';

import { ReadSearchHistoryService } from './read-search-history.service';
import { WriteSearchHistoryService } from './write-search-history.service';

@Injectable()
export class SearchHistoryService {

    constructor(private readSearchHistoryService: ReadSearchHistoryService,
		private writeSearchHistoryervice: WriteSearchHistoryService){}

    getSearchHistory(device_id: string) {

	return this.readSearchHistoryService.getSearchHistory(device_id);
    }

    writeSearchHistory(device_id: string, user_lat: number, user_lon: number, search) {

	return this.writeSearchHistoryervice.writeSearch(device_id, user_lat, user_lon, search);
    }

    
}
