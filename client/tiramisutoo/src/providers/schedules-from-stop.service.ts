import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Observable } from 'rxjs/Observable';
import { catchError } from 'rxjs/operators/catchError';

import {ScheduleEntry} from '../model/schedule-entry';
import { JsonResponse } from '../model/json-response';
import {UrlGenerator} from './url-generator.service';
import {StringUtilityService} from './string-utility.service';

@Injectable()
export class SchedulesFromStopService {

    private servletPath: string = "arrivals-and-departures-for-stop";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator, private utilityService: StringUtilityService) {
    }

    //get schedules from a stop
    getSchedulesFromStop(stop, minutesAfter = 35): Observable<ScheduleEntry[]> {

        let fullServletPath: string = `${this.servletPath}/${stop.id}.json`
        
        let url = this.urlGenerator.generateOBAUrl(fullServletPath,
                                                   { 'minutesBefore': '0', 'minutesAfter': minutesAfter});
        return Observable.create(observer => {
            //retrieves schedules on a single stop
            this.http.get(url)
                .pipe(catchError(this.handleError))
                .subscribe((res: JsonResponse) => {
                let schedules = res.data.entry.arrivalsAndDepartures;
                let trips = res.data.references.trips;
                let scheduleEntries = [];
                for (let schedule of schedules) {
                    let scheduleEntry = JSON.parse(JSON.stringify(schedule));
                    scheduleEntry.directionId = trips.find(x=>x.id==schedule.tripId).directionId;
                    scheduleEntry.stopName = this.utilityService.toTitleCase(stop.name);
                    scheduleEntry.tripHeadsign = this.utilityService.toTitleCase(scheduleEntry.tripHeadsign);
                    scheduleEntries.push(scheduleEntry);
                }
                observer.next(scheduleEntries);
                observer.complete();
                })
        });
    }

    private handleError (error: any) {
        let errMsg = (error.message) ? error.message :
            error.status ? `${error.status} - ${error.statusText}` : 'Server error';
        console.error(errMsg); // log to console instead
        return Observable.throw(errMsg);
    }

}
