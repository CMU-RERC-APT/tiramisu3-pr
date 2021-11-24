import { Component } from '@angular/core';
import { NavParams } from 'ionic-angular';
import { RoutePage } from './route-page/route-page';
import { SchedulePage } from './schedule-page/schedule-page';
import { ScheduleEntry } from '../model/schedule-entry';

import { ButtonLogService } from "../providers/button-logging.service" 
import { LocationService } from "../providers/location.service"

@Component({
    templateUrl: 'route-schedule-page.html',
    selector: 'page-route-schedule',
    providers: [ButtonLogService, LocationService]
})

export class RouteSchedulePage {
    public schedule: ScheduleEntry;
    public routePage;
    public schedulePage;
    private isRoutePage: boolean;
    public pageName: string = 'route_schedule_page';
    private lat;
    private lon;
    private device_id;
    private device_platform;

    constructor(params: NavParams, 
                private buttonLogService: ButtonLogService,
                private locationService: LocationService) {
        this.schedule = params.data.schedule;
        this.routePage = RoutePage;
        this.schedulePage = SchedulePage;
        this.isRoutePage = false;
        this.device_id = params.data.config.get('device_id');
        this.device_platform = params.data.config.get('device_platform');
        // grab pointer to original function

    }

    ionViewDidLeave(){
        this.logBackBtn();
    }


    logBackBtn(){
        this.locationService.getLocation().then(
            user_loc => {
                let optionalArgs = {};
                optionalArgs["route_id"] = this.schedule.routeShortName;
                optionalArgs["stop_id"] = this.schedule.stopId;
                optionalArgs["trip_id"] = this.schedule.tripId;
                optionalArgs["user_id"] = null;
                
                console.log("logging back button");
                this.buttonLogService.logButton(this.device_id, this.device_platform, user_loc.latitude, 
                                                user_loc.longitude, this.pageName, "back", optionalArgs);
            });
    }

    logSelectRouteTabBtn(schedule){
        this.locationService.getLocation().then(
            user_loc => {
                console.log("Logging SELECT_ROUTE_TAB");
                let optionalArgs = {};
                optionalArgs["route_id"] = schedule.routeShortName;
                optionalArgs["stop_id"] = schedule.stopId;
                optionalArgs["trip_id"] = schedule.tripId;
                optionalArgs["user_id"] = null;
                
                this.buttonLogService.logButton(this.device_id, this.device_platform, user_loc.latitude,
                                                user_loc.longitude, this.pageName, "route_tab", optionalArgs);
                
            });
    }

    logSelectScheduleTabBtn(schedule){
        this.locationService.getLocation().then(
            user_loc => {
                console.log("logging SELECT_SCHEDULE_TAB");
                let optionalArgs = {};
                optionalArgs["route_id"] = schedule.routeShortName;
                optionalArgs["stop_id"] = schedule.stopId;
                optionalArgs["trip_id"] = schedule.tripId;
                optionalArgs["user_id"] = null;
                
                this.buttonLogService.logButton(this.device_id, this.device_platform, user_loc.latitude, 
                                                user_loc.longitude, this.pageName, "schedule_tab", optionalArgs);
            });
    }

    /*
    toggleTab() {
        this.isRoutePage = !this.isRoutePage;
        console.log('routepage: ' + this.isRoutePage);
        console.log(this.isRoutePage ? 'Route tab' : 'Schedule tab');
    }

    speakRouteTab() {
        return this.isRoutePage ? 'Showing Route' : 'Show Route';
    }

    speakScheduleTab() {
        return this.isRoutePage ? `Show Schedule` : `Showing Schedule at ${this.schedule.stopName}`;
    }
    */
}
