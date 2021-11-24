import {Component} from '@angular/core';
import {NavController, NavParams, Picker, Config} from 'ionic-angular';
import {MobileAccessibility} from '@ionic-native/mobile-accessibility';
import {StopsOnTripService} from '../../providers/stops-on-trip.service';
import {ShapeService} from '../../providers/shape.service';
import {ArrivalForStopService} from '../../providers/arrival-for-stop.service';
import {AlarmService} from '../../providers/alarm.service';
import {ScheduleEntry} from '../../model/schedule-entry';
import {LocationService} from '../../providers/location.service';
import {StringUtilityService} from '../../providers/string-utility.service';
import {TimeUtilityService} from '../../providers/time-utility.service';
import {ArrivalForTripService} from '../../providers/arrival-for-trip.service';
import { VoiceOverService } from '../../providers/voice-over.service';
import {ButtonLogService} from "../../providers/button-logging.service" ;

@Component({
    templateUrl: 'route-page.html',
    selector: 'page-route',
    providers: [StopsOnTripService, ShapeService, ArrivalForStopService, ArrivalForTripService,
                LocationService, TimeUtilityService, ButtonLogService,VoiceOverService]
})

export class RoutePage{

    private map: google.maps.Map;
    private shouldInitializeMap: boolean = null;
    private currentLocationMarker: google.maps.Marker;
    public pageName : string = 'route_page';
    private lat: number;
    private lon: number;
    private pointsForRoute: string;
    private routePath: google.maps.Polyline;
    private routePathDone: boolean = false;
    private markersDone: boolean = false;
    private firstLoaded: boolean = true;
    private zoomDependentMarkers = [];
    private scrollTimer;
    private logScrollStart: boolean = true; 
    private errorMessage: string;
    private schedule: ScheduleEntry;
    private alarmKeyPrefix: string;
    public stopsScheduleList: ScheduleEntry[];
    private nav: NavController;
    private alarmId: number = 0;
    private registration_id: string;
    private device_id: string;
    private device_platform: string;
    public is_ios: boolean;
    public mapLoading: boolean = true;
    public scheduleLoading: boolean = true;
    private enteredView: boolean = false;
    public voiceOverEnabled: boolean = false;
    private origin = new google.maps.Point(0, 0);
    private current_location_icon = {
        url: "assets/circle_marker.png", // url
        scaledSize: new google.maps.Size(25, 25), // scaled size
        origin: this.origin,
        anchor: new google.maps.Point(12,12)
    };
    constructor(nav: NavController, params: NavParams,
                public stopsOnTripService: StopsOnTripService,
                public shapeService: ShapeService,
                public arrivalForStopService: ArrivalForStopService,
                public locationService : LocationService,
                public utilityService: StringUtilityService,
                public arrivalForTripService : ArrivalForTripService,
                public timeUtilityService: TimeUtilityService,
                public voiceOverService: VoiceOverService,
                private buttonLogService : ButtonLogService,
                private config: Config,
                public alarmService: AlarmService,
                public mobileAccessibility: MobileAccessibility) {
        this.nav = nav;
        this.schedule = params.data;
        console.log("this.schedule = ", this.schedule);
        this.alarmKeyPrefix = this.schedule.tripId + '_' + this.schedule.serviceDate;
        //console.log(this.alarmKeyPrefix);
        this.registration_id = this.config.get('registration_id');
        this.is_ios = this.config.get('is_ios');
        this.device_id = this.config.get('device_id');
        this.device_platform = this.config.get('device_platform');
    }
    private stopIcon = {
        url: "assets/stop_icon.gif",
        scaledSize: new google.maps.Size(10, 10),
        origin: this.origin,
        //anchor: new google.maps.Point(5,5)
        anchor: null
    };
    private stopIconCurrent = {
        url: "assets/icon_set_v1.png", // url
        scaledSize: new google.maps.Size(120,140),
        size: new google.maps.Size(16,14),
        origin: new google.maps.Point(37,93),
        anchor: null
    }

    ngOnInit() {

        //Using window event listener because (onScroll) is not detected on Ionic some elements
        window.addEventListener('scroll', (event: any)=>{
            //console.log(event);
            let elem = event.target;
            while(elem !== null)
            {    
                if(elem.id == "stop_list" || elem.id == "stop") {
                    //console.log("Found route_page");
                    clearTimeout(this.scrollTimer);
                    if (this.logScrollStart == true)
                    {   
                        this.logScrollStart = false; 
                        this.logScrollBtn(elem.id, "scroll_start");
                    }

                    this.scrollTimer = setTimeout(()=>{
                        this.logScrollStart = true;
                        this.logScrollBtn(elem.id, "scroll_end"); 
                    }, 250);

                    break;    
                }
                elem = elem.parentElement;
            }
        }, true);
    }

    logScrollBtn(element: string, event: string) {
        this.locationService.getLocation().then(
            user_loc => {
                let optionalArgs = {};
                //console.log(event);
                optionalArgs["event"] = event;
                this.buttonLogService.logButton(this.device_id, this.device_platform, user_loc.latitude,
                                                user_loc.longitude, this.pageName, "scroll_"+element, optionalArgs);
            });
    }


    loadMap() {
        console.log('loadMap in route page');
        let latLng = new google.maps.LatLng(this.lat, this.lon);

        let mapOptions = {
            center: latLng,
            // zoom: 15,
            // minZoom: 5,
            // maxZoom: 30,
            disableDefaultUI: true,
            mapTypeId: google.maps.MapTypeId.ROADMAP
        }

        this.map = new google.maps.Map(document.getElementById("route-map"), mapOptions);

        // this.map.addListener('bounds_changed', ()=>{
        //     console.log("bounds_changed fired");
        // });



        // this.map.addListener('center_changed', ()=>{
        //     console.log("event center_changed fired");
        // });

        this.currentLocationMarker = new google.maps.Marker({
            map: this.map,
            animation: google.maps.Animation.DROP,
            position: latLng,
            icon: this.current_location_icon,
        });

        if(this.routePath !== undefined && this.routePath !== null && !this.routePathDone){
            this.routePath.setMap(this.map);
            this.routePathDone = true;
        }

        if(this.stopsScheduleList !== undefined && this.stopsScheduleList !== null && !this.markersDone){
            this.addStopMarkers();
            this.markersDone = true;
        }

        this.mapLoading = false;

    }

    setLocationMarker() {
        let latLng = new google.maps.LatLng(this.lat, this.lon);
        this.currentLocationMarker.setPosition(latLng)
    }

    adjustMarkerVisibility(){
        var zoom = this.map.getZoom();
        console.log("in adjustMarkerVisibility(), zoom = ", zoom);
        if (zoom <= 12){
            this.hideMarker();
        }
        else{
            this.showMarker();
        }
    }

    showMarker(){
        for (var marker of this.zoomDependentMarkers){
            marker.setVisible(true);
        }
    }
    hideMarker(){
        for (var marker of this.zoomDependentMarkers){
            marker.setVisible(false);
        }
    }

    addStopMarker(stop: ScheduleEntry, icon, isCurrentStop) {


        let marker = new google.maps.Marker({
            map: this.map,
            //animation: google.maps.Animation.DROP,
            position: new google.maps.LatLng(stop.stopLat, stop.stopLon),
            icon: icon,
            zIndex: 10,
            visible: isCurrentStop
        });
        if (!isCurrentStop){
            this.zoomDependentMarkers.push(marker);
        }



    }

    addStopMarkers(){
        console.log("in add stopMarkers");
        // console.log("this.stopsScheduleList =", this.stopsScheduleList);
        console.log("this.schedule =", this.schedule);
        let bounds = new google.maps.LatLngBounds();
        for(let stop of this.stopsScheduleList){
            if (stop.stopId != this.schedule.stopId) {
                this.addStopMarker(stop, this.stopIcon, false);

            } else {

                this.addStopMarker(stop, this.stopIconCurrent, true);
                // distance is in meters
                let dist = google.maps.geometry.spherical.computeDistanceBetween
                (new google.maps.LatLng(this.lat, this.lon),
                 new google.maps.LatLng(stop.stopLat, stop.stopLon));

                console.log("cauculating distance: " + dist);
                if(dist < 1600) {
                    //set the bound to fit the closest stop and the user's location
                    console.log("set bound to fit user and stop.");
                    bounds.extend(new google.maps.LatLng(stop.stopLat, stop.stopLon));
                    bounds.extend(new google.maps.LatLng(this.lat, this.lon));
                    this.map.fitBounds(bounds);
                } else {
                    // only show the final stop and surroundings
                    this.map.setCenter(new google.maps.LatLng(stop.stopLat, stop.stopLon));
                    console.log("set center to stop and ignore current location");
                    this.map.setZoom(15);
                }

            }
        }
    }

    

    ionViewLoaded() {
        //this.MobileAccessibility = this.config.get('accessibility');
    }

    ionViewDidEnter() {
        //console.log("ionViewDidEnter");
        //if (this.firstLoaded) {
        //    this.getLocation();
        //    this.firstLoaded = false;
        //}
        this.enteredView = true;
        this.getLocation();
        this.mobileAccessibility.isScreenReaderRunning().then(result => {
            this.voiceOverEnabled = result;
            if(this.voiceOverEnabled) {
                this.mobileAccessibility.speak('Showing Route', 0);
                //race condition between the speaking. Can be fixed with a timedelay maybe?
                let headerElem = document.getElementById('header');
                this.voiceOverService.setVoiceOverFocus(headerElem);
            }
        });
        this.getStopsOnTrip();
        setInterval(() => {this.getStopsOnTrip(); }, 30 * 1000);
        this.alarmService.getAlarms(this.registration_id, this.device_id, this.device_platform, 'tiramisu');
    }

    speakStopName(stop: ScheduleEntry) {
        if (stop.predicted && !this.timeUtilityService.hasSwitched(stop)) {
            if(this.timeUtilityService.isInThePast(stop)) {
                return `Arrived at ${stop.stopName} `;
            } else {
                return `Arriving at ${stop.stopName} `;
            }
        } else {
            return `Scheduled at ${stop.stopName} `;
        }
    }

    speakArrivalMin(stop: ScheduleEntry) {
        if (this.timeUtilityService.isInThePast(stop)) {
            return `${this.timeUtilityService.showArrivalMin(stop)} minutes ago`;
        } else {
            return `in ${this.timeUtilityService.showArrivalMin(stop)} minutes`;
        }
    }

    showMinAgo(stop: ScheduleEntry) {
        if (this.timeUtilityService.isInThePast(stop)) {
            return 'min ago';
        } else {
            return 'min';
        }
    }

    getLocation() {
        this.locationService.getLocation().then(
            resp => {
                this.lat = resp.latitude;
                this.lon = resp.longitude;
                if(this.firstLoaded) {
                    this.loadMap();
                    this.map.addListener('idle', ()=>{
                        console.log("event idle fired");
                        this.adjustMarkerVisibility();
                    });
                    this.firstLoaded = false;
                }
                this.setLocationMarker();
                this.getStopsOnTrip();
            },
            error => {
                console.log('getLocation failed with code ' + error.code + ' and message ' + error.message);
            }
        );
    }

    getStopsOnTrip() {
        console.log("in getStopsOnTrip()");
        this.scheduleLoading = true;
        this.arrivalForTripService.getArrivalTimeForTrip(this.schedule).subscribe(
            result => {
                console.log("result: ", result);
                let shapeId = result.shapeId;
                this.stopsScheduleList = result.stopList;
                console.log("stopList = ", result.stopList);
                this.getShape(shapeId)
                console.log("shape id =, ", shapeId);
                // this.getArrivalTimesOnTrip();
                console.log('getStops success');
                console.log("stopsOnTripService result:", result);
                if(this.enteredView) {
                    this.scrollToCurrentStop();
                    this.enteredView = false;
                }
                this.scheduleLoading = false;

            },
            error => {
                this.errorMessage = <any>error;
                console.log(this.errorMessage);
            }
        );
    }

    scrollToCurrentStop() {
        console.log('scrolling');
        setTimeout(() => {
            console.log("this.schedule.stopId: ", this.schedule.stopId);
            let currentStopElement = document.getElementById(this.schedule.stopId);
            console.log("currentStopElement:", currentStopElement);
            currentStopElement.scrollIntoView(true);
        }, 0);
    }

    getShape(shapeId: string) {
        this.shapeService.getShape(shapeId).subscribe(
            result => {
                this.pointsForRoute = result;
                this.routePath = new google.maps.Polyline({
                    path: google.maps.geometry.encoding.decodePath(this.pointsForRoute),
                    strokeColor: '#1E99F7',
                    strokeOpacity: 1,
                    strokeWeight: 6,
                    geodesic: true
                });
                if(this.map !== undefined && this.map !== null && !this.routePathDone){
                    this.routePath.setMap(this.map);
                    this.routePathDone = true;
                }
                if(this.map !== undefined && this.map !== null && !this.markersDone){
                    this.addStopMarkers();
                    this.markersDone = true;

                }
                //this.scheduleLoading = false;
            },
            error => {
                this.errorMessage = <any>error;
                console.log(this.errorMessage);
            }
        );
    }

    getArrivalTimesOnTrip() {
        for (let index in this.stopsScheduleList) {
            let schedule = this.stopsScheduleList[index];
            schedule.tripId = this.schedule.tripId;
            schedule.serviceDate = this.schedule.serviceDate;
            schedule.stopSequence = this.schedule.stopSequence;
            this.getArrivalTimeForStop(index);
        }
    }

    getArrivalTimeForStop(index) {
        this.arrivalForStopService.getArrivalTimeForStop(this.stopsScheduleList[index]).subscribe(
            timeSchedule => {
                this.stopsScheduleList[index] = timeSchedule;
                if (timeSchedule.stopId == this.schedule.stopId) {
                    this.scrollToCurrentStop();
                    this.scheduleLoading = false;
                }
            },
            error => this.errorMessage = <any>error
        );
    }

    getFullAlarmKey(stopId: string) {
        return this.alarmKeyPrefix + stopId;
    }

    IsNotifyOn(stopId: string){
        return this.alarmService.alarmSet[this.getFullAlarmKey(stopId)];
    }

    setAlarm(stop: ScheduleEntry) {
        // temporary user_id
        this.alarmService.setAlarm(this.registration_id, this.device_id, this.device_platform, null,
                                   this.lat, this.lon, stop, this.schedule.routeShortName, this.schedule.tripHeadsign).subscribe(
                                       writeSuccess => {
                                           if (writeSuccess) {
                                               console.log("alarm write success");
                                           } else {
                                               console.log("alarm write failure")
                                           }
                                       },
                                       error => {
                                           console.log('setAlarm failed with code ' + error.code + ' and message ' + error.message);
                                       }
                                   );
    }

    cancelAlarm(stop: ScheduleEntry) {
        // temporary user_id
        this.alarmService.removeAlarm(this.registration_id, this.device_id, this.device_platform, null,
                                      this.lat, this.lon, stop, this.schedule.routeShortName, this.schedule.tripHeadsign).subscribe(
                                          removeSuccess => {
                                              if (removeSuccess) {
                                                  console.log("alarm remove success");
                                              } else {
                                                  console.log("alarm remove failure")
                                              }
                                          },
                                          error => {
                                              console.log('cancelAlarm failed with code ' + error.code + ' and message ' + error.message);
                                          }
                                      );
    }

    toggleNotification(stop: ScheduleEntry) {
        console.log('toggle alarm');
        let stopId = stop.stopId;
        if (this.IsNotifyOn(stopId)){
            this.cancelAlarm(stop);
            this.alarmService.alarmSet[this.getFullAlarmKey(stopId)] = false;
        } else {
            this.setAlarm(stop);
            this.alarmService.alarmSet[this.getFullAlarmKey(stopId)] = true;
        }
        // let picker = Picker.create();

        // picker.addButton({
        //     text: 'Cancel',
        //     role: 'cancel'
        // });
        // picker.addButton({
        //     text: 'Done',
        //     handler: (data) => {
        //         console.log(data);
        //     }
        // });

        // picker.addColumn({
        //     name: 'notify',
        //     columnWidth: '60%',
        //     options: [
        //         { text: '1 min', value: 1 },
        //         { text: '2 min', value: 2 },
        //         { text: '3 min', value: 3 },
        //         { text: '4 min', value: 4 },
        //         { text: '5 min', value: 5 },
        //         { text: '6 min', value: 6 },
        //         { text: '7 min', value: 7 },
        //         { text: '8 min', value: 8 },
        //         { text: '9 min', value: 9 },
        //         { text: '10 min', value: 10 },
        //         { text: '11 min', value: 11 },
        //         { text: '12 min', value: 12 },
        //         { text: '13 min', value: 13 },
        //         { text: '14 min', value: 14 },
        //         { text: '15 min', value: 15 },
        //     ]
        // });


        // let columns = picker.getColumns();

        // this.nav.present(picker);
    }
}
