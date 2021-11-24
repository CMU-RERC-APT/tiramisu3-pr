import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';

import {ScheduleEntry} from '../model/schedule-entry';

import {UrlGenerator} from './url-generator.service';
import {StringUtilityService} from './string-utility.service';

@Injectable()
export class StopsOnTripService {
    private servletPath: string = "trip-details";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator, private utilityService: StringUtilityService) {
    }

    getStopsOnTrip(schedule: ScheduleEntry) {

        let fullServletPath: string = `${this.servletPath}/${schedule.tripId}.json`;

        let stopsOnTripURL = this.urlGenerator.generateOBAUrl(fullServletPath,
                                  { 'serviceDate': schedule.serviceDate });

        return this.http.get(stopsOnTripURL).pipe(map(res => this.extractStopsAndIndex(res, schedule)));
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
        let stopTimes = res.data.entry.schedule.stopTimes;
        let stops = res.data.references.stops;
        let trips = res.data.references.trips;
        let shapeId = this.findShapeId(schedule.tripId, trips);
        let stopList = stopTimes.map(stopTime => {
            let stopId = stopTime.stopId;
            let stopInfo = this.findStopInfo(stopId, stops);
            let stopName = stopInfo.name;
            let stopLat = stopInfo.lat;
            let stopLon = stopInfo.lon;
            return {stopId: stopId,
                stopLat: stopLat,
                stopLon: stopLon,
                stopName: this.utilityService.toTitleCase(stopName),
            }
        });

        return {
            stopList: stopList,
            shapeId: shapeId
        };
    }
}

