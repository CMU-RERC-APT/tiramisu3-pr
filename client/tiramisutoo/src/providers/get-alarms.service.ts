import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';

import { UrlGenerator } from './url-generator.service';

@Injectable()
export class GetAlarmsService {

    private servletPath: string = "ReadAlarmServlet";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator) {

    }

    getAlarms(registration_id: string, device_id: string, device_platform: string, user_id: string) {
        // temporary
        let readAlarmsUrl = this.urlGenerator.generateTiramisuUrl(this.servletPath,
            {'registration_id': registration_id,
             'device_id': device_id,
             'device_platform': device_platform});
             //'user_id': user_id});
        console.log(readAlarmsUrl);

        return this.http.get(readAlarmsUrl).pipe(map(this.extractAlarms));
    }

    private extractAlarms(res) {
        //let body = res.json();
        let alarms = res.data;

        let alarmSet: { [key: string]: boolean } = {};

        for (let alarm of alarms) {
            let new_service_date = alarm.service_date.replace(" ", "T");
            let alarmKey = alarm.trip_id + '_' + Date.parse(new_service_date + "Z") + alarm.stop_id;
            //console.log(alarmKey);
            alarmSet[alarmKey] = true;
        }

        return alarmSet;
    }
}
