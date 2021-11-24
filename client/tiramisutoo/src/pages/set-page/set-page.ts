import {Component, ViewChild} from '@angular/core';
import {NavParams, Content,  NavController, Config} from 'ionic-angular';
import {ScheduleEntry} from '../../model/schedule-entry';
import {Stop} from '../../model/stop';
import {StringUtilityService} from '../../providers/string-utility.service';
import {LocationService} from '../../providers/location.service';
import {FavStopsService} from '../../providers/fav-stops.service';
import {RouteSchedulePage} from '../route-schedule-page';
import {ButtonLogService} from "../../providers/button-logging.service";
import {VoiceOverService} from '../../providers/voice-over.service';
import {HandleSettingsService} from '../../providers/handle-settings.service';


@Component({
    templateUrl: 'set-page.html',
    selector: 'page-set',
    providers: [LocationService, ButtonLogService, VoiceOverService,HandleSettingsService]
})
export class SettingsPage {
    @ViewChild(Content) content: Content;
    public pageName: string = 'settings_page';
    private map: google.maps.Map;
    private currentLocationMarker: google.maps.Marker;
    private stopLocationMarker: google.maps.Marker;
    private stopLatLng: google.maps.LatLng;
    private userLat: number;
    private userLon: number;
    private device_id: string;
    private userLatLng: google.maps.LatLng;
    private settingsDic;
    private origin = new google.maps.Point(0, 0);
    private current_location_icon = {
        url: "assets/circle_marker.png", // url
        scaledSize: new google.maps.Size(25, 25), // scaled size
        origin: this.origin,
        anchor: new google.maps.Point(12,12)
    };
    private spriteSize = new google.maps.Size(200,235);
    private anchor = null; // defaults to center of bottom
    private blue_stop_icon = {
        url: "assets/icon_set_v1.png", // url
        scaledSize: this.spriteSize,
        size: new google.maps.Size(34,29),
        origin: new google.maps.Point(7,98),
        anchor: this.anchor
    };

    constructor(private buttonLogService : ButtonLogService,
                private locationService: LocationService,
                private config: Config,
                private handleSettingsService : HandleSettingsService,
                public voiceOverService: VoiceOverService) {
    }
   
    private write_Settings(event,item)
    {
        this.locationService.getLocation().then(
            user_loc => {
                if (event["checked"] == true){
                    this.handleSettingsService.writeSettings(this.device_id, user_loc.latitude, user_loc.longitude,item).subscribe(
                    () => {
                        console.log('writeSettings succeed');
                    },    
                );
                }else{
                    this.handleSettingsService.removeSettings(this.device_id, user_loc.latitude, user_loc.longitude,item).subscribe(
                    () => {
                        console.log('removeSettings succeed');
                    },    
                );
                }
            });
    }

    private read_Settings(){
        this.settingsDic = {};
        this.locationService.getLocation().then(
            user_loc => {
                            this.handleSettingsService.readSettings(this.device_id, user_loc.latitude, user_loc.longitude).subscribe(
                                response=> {
                                    for (var key in response){
                                        this.settingsDic[key] = response[key];
                                        }
                                    }
                            );
                        });
    }

    private getVal(event)
    {
        return this.settingsDic[event];
    }

    ngOnInit() {
        this.device_id = this.config.get('device_id');
        this.read_Settings();
    }
}