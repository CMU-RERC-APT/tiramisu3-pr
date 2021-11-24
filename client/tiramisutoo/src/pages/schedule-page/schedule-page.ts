import {Component} from '@angular/core';
import {NavParams, Config} from 'ionic-angular';
import {MobileAccessibility} from '@ionic-native/mobile-accessibility';
import {ScheduleEntry} from '../../model/schedule-entry';
import {SchedulesFromStopService} from '../../providers/schedules-from-stop.service';
import {StringUtilityService} from '../../providers/string-utility.service';
import {TimeUtilityService} from '../../providers/time-utility.service';
import {VoiceOverService } from '../../providers/voice-over.service';
import {LocationService} from '../../providers/location.service';
import {ButtonLogService} from "../../providers/button-logging.service" ;

@Component({
    templateUrl: 'schedule-page.html',
    selector: 'page-schedule',
    providers: [SchedulesFromStopService, TimeUtilityService, VoiceOverService,ButtonLogService,LocationService]
})

export class SchedulePage{

    public schedule: ScheduleEntry;
    private routeId: string;
    public pageName : string = 'schedule_page';
    private directionId: number;
    private scrollTimer;
    private logScrollStart: boolean = true; 
    private stop;
    public schedules: ScheduleEntry[];
    public today: string = new Date().toDateString();
    public loading: boolean = true;
    public voiceOverEnabled: boolean = false;
    private device_id: string;
    private device_platform: string;
    constructor(params: NavParams,
                public schedulesFromStopService: SchedulesFromStopService,
                public utilityService: StringUtilityService,
                public timeUtilityService: TimeUtilityService,
                public voiceOverService: VoiceOverService,
                public mobileAccessibility: MobileAccessibility,
                private locationService: LocationService,
                private buttonLogService : ButtonLogService,
                public config: Config) {
        this.schedule = params.data;
        this.routeId = this.schedule.routeId;
        this.directionId = this.schedule.directionId;
        this.stop = {id: this.schedule.stopId, name: this.schedule.stopName};
    }

    ngOnInit() {
        setInterval(() => { this.getSchedulesForStop;}, 30 * 1000);
        this.device_id = this.config.get('device_id');
        this.device_platform = this.config.get('device_platform');

        window.addEventListener('scroll', (event: any)=>{

            let elem = event.target;
            while(elem !== null) {
                if(elem.id == "schedule_page")
                {    
                    clearTimeout(this.scrollTimer);

                    if (this.logScrollStart == true)
                    {   
                        this.logScrollStart = false; 
                        this.logScrollBtn("scroll_start");
                    }

                    this.scrollTimer = setTimeout(()=>{
                        this.logScrollStart = true;
                        this.logScrollBtn("scroll_end"); 
                    }, 250);
                    break;
                }
                elem = elem.parentElement;
            }
            
        },true);
    }

    ionViewDidEnter() {
        this.today = this.timeUtilityService.getDateString();
        this.mobileAccessibility.isScreenReaderRunning().then(result => {
            this.voiceOverEnabled = result;
            if(this.voiceOverEnabled) {
                this.mobileAccessibility.speak(`Showing Schedule at ${this.schedule.stopName}`);
            }
        });
        this.getSchedulesForStop();

        let headerElem = document.getElementById('header');
        this.voiceOverService.setVoiceOverFocus(headerElem);
    }

    speakArrivalHrMin(schedule: ScheduleEntry) {
        let hr = this.timeUtilityService.showArrivalHour(schedule);
        let min = this.timeUtilityService.showArrivalRemainingMin(schedule);
        let time;
        if (hr > 0) {
            time = `${hr} hours ${min} minutes`;
        } else {
            time = `${min} minutes`;
        }

        if (schedule.predicted && !this.timeUtilityService.hasSwitched(schedule)) {
            return `Arriving in ${time}`;
        } else {
            return `Scheduled in ${time}`;
        }
    }

    getSchedulesForStop() {
        let minutesAfter = 12*60;
        this.schedulesFromStopService.getSchedulesFromStop(this.stop, minutesAfter).subscribe(
            result => {
                this.schedules = result.filter(schedule => this.matchRouteAndDirection(schedule.routeId, schedule.directionId));
                //this.schedules = this.schedules.sort(this.compareArrivalTime);
                this.loading = false;
            }
        );
    }
    logScrollBtn(event: string) {
        this.locationService.getLocation().then(
            user_loc => {
                let optionalArgs = {};
                //console.log(event);
                optionalArgs["event"] = event;
                this.buttonLogService.logButton(this.device_id, this.device_platform, user_loc.latitude,
                                                user_loc.longitude, this.pageName, "scroll", optionalArgs);
            });
    }
    /*
      compareArrivalTime(schedule1: ScheduleEntry, schedule2: ScheduleEntry) {
      if (this.timeUtilityService.getArrivalTime(schedule1) < this.timeUtilityService.getArrivalTime(schedule2)) {
      return -1;
      }
      if (this.timeUtilityService.getArrivalTime(schedule1) > this.timeUtilityService.getArrivalTime(schedule2)) {
      return 1;
      }
      return 0;
      }
    */

    matchRouteAndDirection(routeId: string, directionId: number) {
        return (routeId == this.routeId && directionId == this.directionId);
    }
}
