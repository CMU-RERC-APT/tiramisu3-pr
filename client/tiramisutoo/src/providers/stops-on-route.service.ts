import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';

import {ScheduleEntry} from '../model/schedule-entry';
import {UrlGenerator} from './url-generator.service';
import {StringUtilityService} from './string-utility.service';

@Injectable()
export class StopsOnRouteService {
    private servletPath: string = "stops-for-route";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator, private utilityService: StringUtilityService) {
    }

    getStopsOnRoute(schedule: ScheduleEntry) {

    	let fullServletPath: string = `${this.servletPath}/${schedule.routeId}.json`;

    	let stopsOnRouteURL = this.urlGenerator.generateOBAUrl(fullServletPath, {});
    	return this.http.get(stopsOnRouteURL).pipe(map(res => this.extractStopsAfter(res, schedule)));
    }

    private findCurrentStopIndex(stopIdList: string[], currentStop: string) {
        for (let id in stopIdList) {
            if (currentStop === stopIdList[id]) {
                return +id;
            }
        }
        return -1;
    }

    private findStopInfo (stopId: string, stops: any[]) {
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

    private extractStopsAfter(res, schedule: ScheduleEntry) {
        //let body = res.json();
        let stopGroups = res.data.entry.stopGroupings[0].stopGroups;
        let group;
        if (stopGroups[0].id != schedule.directionId) {
            group = 1;
        } else {
            group = 0;
        }

        var pointString = stopGroups[group].polylines[0].points;

        let stopIdList = stopGroups[group].stopIds;
        var stops = res.data.references.stops;
        

        var routeId = schedule.routeId;
        var tripId = schedule.tripId;
        var serviceDate = schedule.serviceDate;
        var routeShortName = schedule.routeShortName;
        var routeLongName = schedule.routeLongName;
        var tripHeadsign = schedule.tripHeadsign;
        var stopSequence = schedule.stopSequence;
        return {
            stopList : stopIdList.map(stopId => {
                let stopInfo = this.findStopInfo(stopId, stops);
                let stopName = stopInfo.name;
                let stopLat = stopInfo.lat;
                let stopLon = stopInfo.lon;
                return {
                    routeId: routeId,
                    tripId: tripId,
                    serviceDate: serviceDate,
                    stopId: stopId,
                    stopSequence: stopSequence,
                    stopLat: stopLat,
                    stopLon: stopLon,
                    stopName: this.utilityService.toTitleCase(stopName),
                    routeShortName: routeShortName,
                    routeLongName: routeLongName,
                    tripHeadsign: tripHeadsign
                }
            }),

            points : pointString,
            currentStopIndex: this.findCurrentStopIndex(stopIdList, schedule.stopId)
        };
    }
}
