import { Injectable }   from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Config} from 'ionic-angular';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';

import {ScheduleEntry} from '../model/schedule-entry';
import { DisplayedRoute } from '../model/displayed-route';

import {StringUtilityService} from './string-utility.service';
import {UrlGenerator} from './url-generator.service';


@Injectable()
export class ArrivalForTripService {

    private servletPath: string = "ArrivalForTripServlet";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator, private config: Config,
        private utilityService: StringUtilityService) {
    }

    getArrivalTimeForTrip(schedule: ScheduleEntry) {

        /* Can only get sequence from schedule which is passed in from hompage*/
        console.log("schedule entry: ", schedule);
        let argMap = {'trip_id' : schedule.tripId,
                      'service_date' : schedule.serviceDate,
                      'stop_sequence' : schedule.stopSequence,
                      'current_stop_id' : schedule.stopId};
        
        
        let arrivalTimeForTripUrl = this.urlGenerator.generateTiramisuUrl(this.servletPath, argMap);
        console.log(arrivalTimeForTripUrl);
        
        return this.http.get(arrivalTimeForTripUrl).pipe(
            map(res => this.extractStopsAndIndex(res, schedule))
        );
        
        // return this.http.get(arrivalTimeForTripUrl).map(this.readSuccess).subscribe((data)=>{
        //   console.log("arrival time for trip data: ", data);
        //   let response = JSON.parse(data._body);
        //   return response;
        // }, (error)=>{
        //   console.log("arrival time for trip error: ", error);
        //   return null;
        // });
    }
    
    private findShapeId(tripId: string, trips: any[]) {
        for (let trip of trips) {
            if (trip.id === tripId) {
                return trip.shapeId;
            }
        }
        return '';
    }

    private findStopInfo(stopId: string, stops: any[]) {
        for (let stop of stops) {
            if (stop.id === stopId) {
                return {
                    name: stop.name,
                    lat: stop.lat,
                    lon: stop.lon,
                }
            }
        }
        return {
            name: '',
            lat: 0,
            lon: 0
        };
    }

    extractStopsAndIndex(res, schedule: ScheduleEntry) {
        //let body = res.json();
        let stopTimes = res.stopTimes;
        let stops = res.references.stops;
        let trips = res.references.trips;
        let shapeId = this.findShapeId(schedule.tripId, trips);
        console.log("in extract, stopTimes = ", stopTimes);
        let stopList = stopTimes.map(stopTime => {
            let stopId = stopTime.stopId;
            let stopInfo = this.findStopInfo(stopId, stops);
            let stopName = stopInfo.name;
            let stopLat = stopInfo.lat;
            let stopLon = stopInfo.lon;
            stopTime.stopId = stopId;
            stopTime.info = stopInfo;
            stopTime.stopLat = stopLat,
            stopTime.stopLon = stopLon,
            stopTime.stopName = this.utilityService.toTitleCase(stopName)
            return stopTime;
            // let returnObject = {stopId: stopId,
            //     stopLat: stopLat,
            //     stopLon: stopLon,
            //     stopName: this.utilityService.toTitleCase(stopName),
            // }
            // console.log("returnObject:", returnObject);
            // return returnObject;
        });

        return {
            stopList: stopList,
            shapeId: shapeId
        };
    }

    private handleError(error: any) {
        let errMsg = (error.message) ? error.message :
            error.status ? `${error.status} - ${error.statusText}` : 'Server error';
        console.error(errMsg); // log to console instead
        return Observable.throw(errMsg);
    }

    readSuccess(resp){
      return resp;
    }
}
