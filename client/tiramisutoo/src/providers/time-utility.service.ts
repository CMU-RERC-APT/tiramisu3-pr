import { Injectable } from '@angular/core';
import {ScheduleEntry} from '../model/schedule-entry';

@Injectable()
export class TimeUtilityService {

    msToMin(time: number) {
        return Math.round(time / 60000);
    }

    minToMs(time: number) {
        return Math.round(time * 60000);
    }

    msToTime(time: number){
        if (isNaN(time)) {
            return '';
        }
        let date = new Date(time);
        let ampm = "am"
        let hours = date.getHours();
        if (hours >= 12 && hours <= 23) {
            ampm = "pm";
        }
        if (hours > 12) {
            hours -= 12;
        }
        let minutes = "0" + date.getMinutes();
        return hours + ':' + minutes.substr(-2) + ampm;
    }

    getArrivalTime(schedule: ScheduleEntry) {
        if (schedule.predicted) {
            if (this.msToMin(schedule.predictedArrivalTime - new Date().getTime()) > 45) return schedule.scheduledArrivalTime;
            return schedule.predictedArrivalTime;
        } else {
            return schedule.scheduledArrivalTime;
        }
    }

    getDateString() {
        let today = new Date();
        return this.getDayString(today.getDay()) + ' ' + this.getMonthString(today.getMonth()) + ' ' + today.getDate() + ' ' + today.getFullYear();
    }

    getMonthString(month: number) {
        let months = ['January', 'Febuary', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
        return months[month];
        
    }

    getDayString(day: number) {
        let days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
        return days[day];
    }

    isInThePast(schedule: ScheduleEntry) {
        return this.getArrivalTime(schedule) < new Date().getTime();
    }

    showArrivalMin(schedule: ScheduleEntry) {
        return this.msToMin(this.getArrivalTime(schedule) - new Date().getTime());
    }

    showAbsArrivalMin(schedule: ScheduleEntry) {
        let result = Math.abs(this.showArrivalMin(schedule));
        return isNaN(result) ? '' : result;
    }

    showArrivalHour(schedule: ScheduleEntry) {
        return Math.floor(this.showArrivalMin(schedule) / 60);
    }

    showArrivalRemainingMin(schedule: ScheduleEntry) {
        return this.showArrivalMin(schedule) % 60;
    }

    showArrivalTime(schedule: ScheduleEntry) {
        return this.msToTime(this.getArrivalTime(schedule));
    }

    showArrivalHourMin(schedule: ScheduleEntry) {
        if (this.showArrivalHour(schedule) <= 0) {
            return this.showArrivalMin(schedule).toString();
        } else {
            if (this.showArrivalRemainingMin(schedule) < 10) {
                return this.showArrivalHour(schedule) + ':0' + this.showArrivalRemainingMin(schedule);
            } else {
                return this.showArrivalHour(schedule) + ':' + this.showArrivalRemainingMin(schedule);
            }
        }
    }

    speakArrivalHourMin(schedule: ScheduleEntry) {
        if (this.showArrivalHour(schedule) <= 0) {
            if (this.showArrivalMin(schedule) == 1) return this.showArrivalMin(schedule).toString() + ' minute';
            return this.showArrivalMin(schedule).toString() + ' minutes';
        } else {
            if (this.showArrivalHour(schedule) == 1) {
                if (this.showArrivalRemainingMin(schedule) == 1) {
                    return this.showArrivalHour(schedule) + ' hour ' + this.showArrivalRemainingMin(schedule) + ' minute';
                }
                return this.showArrivalHour(schedule) + ' hour ' + this.showArrivalRemainingMin(schedule) + ' minutes';
            }
            if (this.showArrivalRemainingMin(schedule) == 1) {
                return this.showArrivalHour(schedule) + ' hours ' + this.showArrivalRemainingMin(schedule) + ' minute';
            }
            return this.showArrivalHour(schedule) + ' hours ' + this.showArrivalRemainingMin(schedule) + ' minutes';
        }
    }

    hasSwitched(schedule: ScheduleEntry) {
        return this.msToMin(schedule.predictedArrivalTime - new Date().getTime()) > 45;
    }

}
