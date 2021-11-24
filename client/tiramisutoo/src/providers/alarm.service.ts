import { Injectable } from '@angular/core';

import { Observable } from 'rxjs/Observable';

import { ScheduleEntry } from '../model/schedule-entry';

import { GetAlarmsService } from './get-alarms.service';
import { SetAlarmService } from './set-alarm.service';
import { RemoveAlarmService } from './remove-alarm.service';

@Injectable()
export class AlarmService {

    public alarmSet: { [key: string]: boolean } = {};

    constructor(private getAlarmsService: GetAlarmsService,
                private setAlarmService: SetAlarmService,
                private removeAlarmService: RemoveAlarmService) {}

    getAlarms(registration_id: string, device_id: string, device_platform: string, user_id: string) {
        this.getAlarmsService.getAlarms(registration_id, device_id, device_platform, user_id).subscribe(
            alarms => {
                this.alarmSet = alarms;
            },
            error => {
                console.log('getAlarms failed with code ' + error.code + ' and message ' + error.message);
            }
        );
    }

    setAlarm(registration_id: string, device_id: string, device_platform: string,
             user_id: string, user_lat: number, user_lon: number, schedule: ScheduleEntry,
             route_name: string, trip_headsign: string) {
        return this.setAlarmService.setAlarm(registration_id, device_id, device_platform,
            user_id, user_lat, user_lon, schedule, route_name, trip_headsign);
    }

    removeAlarm(registration_id: string, device_id: string, device_platform: string,
                user_id: string, user_lat: number, user_lon: number, schedule: ScheduleEntry,
                route_name: string, trip_headsign: string) {
        return this.removeAlarmService.removeAlarm(registration_id, device_id, device_platform,
            user_id, user_lat, user_lon, schedule, route_name, trip_headsign);
    }
    
}
