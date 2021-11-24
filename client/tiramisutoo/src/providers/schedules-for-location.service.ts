import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { UrlGenerator } from './url-generator.service';
import { StringUtilityService } from './string-utility.service';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';

//import {DisplayedRoute} from '../model/displayed-route';
import {ScheduleEntry} from '../model/schedule-entry';
import { JsonResponse } from '../model/json-response';
import {Stop} from '../model/stop';

@Injectable()
export class SchedulesForLocationService {

    private servletPath: string = "SchedulesForLocationServlet";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator, private stringUtility: StringUtilityService) {
    }

    //get the schedules near the current locations
    getSchedules(lat: number, lon: number): Observable<ScheduleEntry[]> {

        let scheduleForLocationUrl = this.urlGenerator.generateTiramisuUrl(this.servletPath, { 'lat': lat, 'lon': lon});

        return this.http.get(scheduleForLocationUrl).pipe(map(
            //this cannot be a separate function, otherwise this.stringUtility will be undefined
            (res: JsonResponse) => {
                //let body = res.json();
                let scheduleEntries: ScheduleEntry[] = res.data;
                
                /*scheduleEntries = scheduleEntries.map(
                    scheduleEntry => {
                        //let scheduleEntry: ScheduleEntry = JSON.parse(JSON.stringify(entry));
                        scheduleEntry.stopName = this.stringUtility.toTitleCase(scheduleEntry.stopName); 
                        scheduleEntry.tripHeadsign = this.stringUtility.toTitleCase(scheduleEntry.tripHeadsign);
                        return scheduleEntry;
                        
                    });*/

                for (let entry of scheduleEntries) {
                    entry.stopName = this.stringUtility.toTitleCase(entry.stopName); 
                    entry.tripHeadsign = this.stringUtility.toTitleCase(entry.tripHeadsign);
                }

                //console.log(scheduleEntries);

                return scheduleEntries;
            }));
    }

}
