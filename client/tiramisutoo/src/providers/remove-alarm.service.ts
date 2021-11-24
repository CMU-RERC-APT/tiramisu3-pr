import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';

import { ScheduleEntry } from '../model/schedule-entry';
import { StringUtilityService } from './string-utility.service';
import { UrlGenerator } from './url-generator.service';

@Injectable()
export class RemoveAlarmService {

    private servletPath: string = "RemoveAlarmServlet";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator,
        private stringUtilityService: StringUtilityService) {
    }

    removeAlarm(registration_id: string, device_id: string, device_platform: string,
                user_id: string, user_lat: number, user_lon: number, schedule: ScheduleEntry,
                route_name: string, trip_headsign: string) {
        //temporary
        let removeAlarmUrl = this.urlGenerator.generateTiramisuUrl(this.servletPath,
            {'registration_id': registration_id,
             'device_id': device_id,
             'device_platform': device_platform,
             'user_id': user_id,
             'user_lat': user_lat,
             'user_lon': user_lon,
             'trip_id': schedule.tripId,
             'stop_id': schedule.stopId,
             'service_date': schedule.serviceDate,
             'route_name': route_name,
             'stop_name': this.stringUtilityService.replacePoundSign(schedule.stopName),
             'trip_headsign': trip_headsign});
        console.log(removeAlarmUrl);

        return this.http.get(removeAlarmUrl).pipe(map(this.removeSuccess));
    }

    private removeSuccess(res) {
        //let body = res.json();
        let rows_affected = res.data[0].rowsAffected;
        if (rows_affected == "1") {
            return true;
        } else {
            return false;
        }
    }
}
