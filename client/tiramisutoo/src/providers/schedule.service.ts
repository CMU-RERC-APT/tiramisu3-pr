import { Http } from '@angular/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

//import {DisplayedRoute} from '../model/displayed-route';
import {ScheduleEntry} from '../model/schedule-entry';
import {Stop} from '../model/stop';

import {DistanceService} from './distance.service';
import {SchedulesFromStopService} from './schedules-from-stop.service';

@Injectable()
export class ScheduleService {

    lat: number;
    lon: number;

    constructor(private http: Http,
        private distanceService: DistanceService,
        private schedulesFromStopService: SchedulesFromStopService) {
    }

    //get the schedules near the current locations

    getSchedules(lat: number, lon: number, stops: Stop[]): Observable<ScheduleEntry[]> {
        this.lat = lat;
        this.lon = lon;
        return Observable.create(observer => {
            //first get the stops nearby
            let scheduleEntries: {[key:string]:ScheduleEntry;} = {};
            //get combined schedules from every nearby stop
            this.getSchedulesFromStops(stops).subscribe({
                //merge the shedules from one stop to the final result
                next: x => this.mergeSchedules(scheduleEntries, x, stops),
                complete: () => {
                    let result = [];
                    for (let key in scheduleEntries) {
                        result.push(scheduleEntries[key]);
                    }
                    observer.next(result);
                    observer.complete();
                }
            });
        });
    }

    //retrieve schedules based on stops

    getSchedulesFromStops(stops: Stop[]): Observable<ScheduleEntry[]> {     
        let result = Observable.create(observer => {
            observer.complete();
        });
        //merge the schedule observables
        for (let stop of stops) {
            result = result.merge(this.schedulesFromStopService.getSchedulesFromStop(stop));
        }
        return result;
    }

    //merge schedules from one stop to the final result

    mergeSchedules(original: {[key:string]:ScheduleEntry;}, current: ScheduleEntry[], stops: Stop[]) {
        if(current.length==0) {
            return original;
        }
        let currentStop = stops.find(x=>x.id==current[0].stopId);
        for (let currentEntry of current) {
            //construct the key based on both routeId and directionId
            let key = currentEntry.routeId+currentEntry.directionId;
            let originalEntry = original[key];
            if (originalEntry==undefined) {
                //the current route-direction combination hasn't yet been found
                original[key] = currentEntry;
            }else{
                //the current route-direction combination has been found already
                let originalStop = stops.find(x=>x.id==originalEntry.stopId);
                if(originalStop.id==currentStop.id &&
                    (currentEntry.predictedArrivalTime==undefined?
                        currentEntry.scheduledArrivalTime:currentEntry.predictedArrivalTime)<
                    (originalEntry.predictedArrivalTime==undefined?
                        originalEntry.scheduledArrivalTime:originalEntry.predictedDepartureTime)) {
                    //the current route-direction combination has the same stop as the one already found
                    //but the current trip arrives sooner
                    original[key] = currentEntry;
                }else if(this.distanceService.compare(currentStop,originalStop,this.lat,this.lon)<0) {
                    //the current route-direction combination has a stop closer to the current location
                    original[key] = currentEntry;
                }
            }
        }
        return original;
    }
    
}
