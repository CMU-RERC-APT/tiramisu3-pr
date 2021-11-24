import { Injectable }     from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';

import {ScheduleEntry} from '../model/schedule-entry';
import {UrlGenerator} from './url-generator.service';

@Injectable()
export class ArrivalForStopService {

    private servletPath: string = "arrival-and-departure-for-stop";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator) {
    }

    getArrivalTimeForStop(schedule: ScheduleEntry) {

        let fullServletPath: string = `${this.servletPath}/${schedule.stopId}.json`;

        let arrivalForStopURL = this.urlGenerator.generateOBAUrl(fullServletPath,
                                    { 'tripId': schedule.tripId, 'serviceDate': schedule.serviceDate, 'stopSequence': schedule.stopSequence });

        return this.http.get(arrivalForStopURL).pipe(
            map(res => this.extractArrivalInfo(res, schedule))
        );
        
    }

    private extractArrivalInfo(res, schedule: ScheduleEntry) {
        //let body = res.json();
        let arrival = res.data.entry;
        schedule.scheduledArrivalTime = arrival.scheduledArrivalTime;
        schedule.scheduledDepartureTime = arrival.scheduledDepartureTime;
        schedule.predicted = arrival.predicted;
        schedule.predictedArrivalTime = arrival.predictedArrivalTime;
        schedule.predictedDepartureTime = arrival.predictedDepartureTime;
        return schedule;
    }
}
