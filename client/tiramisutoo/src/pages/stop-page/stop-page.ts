import {Component, ViewChild} from '@angular/core';
import {NavParams, Content,  NavController, Config} from 'ionic-angular';
import {ScheduleEntry} from '../../model/schedule-entry';
import {Stop} from '../../model/stop';
import {StringUtilityService} from '../../providers/string-utility.service';
import {LocationService} from '../../providers/location.service';
import {FavStopsService} from '../../providers/fav-stops.service';
import {RouteSchedulePage} from '../route-schedule-page';
import {ButtonLogService} from "../../providers/button-logging.service";
import { VoiceOverService } from '../../providers/voice-over.service';



@Component({
    templateUrl: 'stop-page.html',
    selector: 'page-stop',
    providers: [LocationService, ButtonLogService, VoiceOverService]
})
export class StopPage {

    @ViewChild(Content) content: Content;

    public pageName: string = 'stop_page';
    private map: google.maps.Map;
    private currentLocationMarker: google.maps.Marker;
    private stopLocationMarker: google.maps.Marker;
    private lat: number;
    private lon: number;
    private stopLatLng: google.maps.LatLng;
    private userLat: number;
    private userLon: number;
    private userLatLng: google.maps.LatLng;
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

    loadMap() {
       console.log('loadMap in stop page');
        this.stopLatLng =new google.maps.LatLng(this.lat, this.lon);
        this.userLatLng = new google.maps.LatLng(this.userLat, this.userLon);

        let mapOptions = {
            center: this.stopLatLng,
            zoom: 15,
            disableDefaultUI: true,
            mapTypeId: google.maps.MapTypeId.ROADMAP
        };

        this.map = new google.maps.Map(document.getElementById("stop-map"), mapOptions);

        this.stopLocationMarker = new google.maps.Marker({
            map: this.map,
            animation: google.maps.Animation.DROP,
            position: this.stopLatLng,
            icon: this.blue_stop_icon,
        });

        this.currentLocationMarker = new google.maps.Marker({
            map: this.map,
            animation: google.maps.Animation.DROP,
            position: this.userLatLng,
            icon: this.current_location_icon,
        });


        // adjust bounds
        let bounds = new google.maps.LatLngBounds();
        let dist = google.maps.geometry.spherical.computeDistanceBetween(this.stopLatLng, this.userLatLng)
        console.log("cauculating distance: " + dist);
        if(dist < 1600) {
            //set the bound to fit the closest stop and the user's location
            console.log("set bound to fit user and stop.");
            bounds.extend(this.stopLatLng);
            bounds.extend(this.userLatLng);
            this.map.fitBounds(bounds);
            this.displayDirection();
        } else {
            // only show the final stop and surroundings
            this.map.setCenter(this.stopLatLng);
            console.log("set center to stop and ignore current location");
            this.map.setZoom(15);
        }
    }

    //calculate and display walking direction on the map
    displayDirection(){
        let directionsService = new google.maps.DirectionsService();
        let lineSymbol = {
            path: 'M 0,-1 0,1',
            strokeColor: '#1E99F7',
            strokeOpacity: 1,
            strokeWeight: 6,
            scale: 5
        };
        let lineSymbolGrey = {
            path: 'M 0,-1 0,1',
            strokeColor: '#959696',
            strokeOpacity: 1,
            strokeWeight: 6,
            scale: 5
        };
        directionsService.route({
            origin: this.userLatLng,
            destination: this.stopLatLng,
            travelMode: google.maps.TravelMode.WALKING,
            provideRouteAlternatives: true
        }, (response, status) => {
            if (status === google.maps.DirectionsStatus.OK) {
                console.log("direction request succeeded");
                console.log(response.routes.length);
                let bounds = new google.maps.LatLngBounds();
                for (var len = response.routes.length, i = len-1; i >= 0; i--) {
                    bounds.union(response.routes[i].bounds);
                    new google.maps.DirectionsRenderer({
                        hideRouteList: false,
                        suppressMarkers: true,
                        preserveViewport: true,
                        map: this.map,
                        directions: response,
                        routeIndex: i,
                        polylineOptions: {
                            icons: [{
                                icon: (i > 0) ? lineSymbolGrey : lineSymbol,
                                offset: '0',
                                repeat: (i > 0) ? '20px' : '22px'
                            }],
                            strokeOpacity: 0,
                        }
                    });
                }
                this.map.fitBounds(bounds);

                // For adding time marker on the map, but we currently don't have the chat box icon

                // let directionsRoute = response.routes[0];
                // let path = google.maps.geometry.encoding.decodePath(directionsRoute.overview_polyline);
                // let mid_point = path[path.length/2];
                // new google.maps.Marker({
                //     map: this.map,
                //     position: mid_point,
                //     icon: //insert chat box icon,
                // });
                this.loading = false;
                this.content.scrollToTop();
            } else {
                console.log('Directions request failed due to ' + status);
            }
        });
    }

    private schedules : ScheduleEntry[];
    private stop: Stop;
    public stopName: string;
    //A two by two array of route direction and name strings for displaying on the page
    public routesTable: any[][] = [];
    private numOfColumns: number = 5;
    public loading: boolean = true;
    public isFav: boolean;
    public is_ios: boolean;
    private nav_opts = {animate: true};
    private device_id;
    private device_platform;

    constructor(params: NavParams,
                private buttonLogService: ButtonLogService,
                private locationService: LocationService,
                private utilityService: StringUtilityService,
                private favStopsService: FavStopsService,
                public voiceOverService: VoiceOverService,
                public nav: NavController,
                private config: Config) {
        this.schedules = params.data.schedules;
        this.stop = params.data.stop;
        this.isFav = params.data.isFav;
        this.lat = this.stop.lat;
        this.lon = this.stop.lon;
        this.stopName = this.utilityService.toTitleCase(this.stop.name);
        this.favStopsService.enterStopPage(this.stop.id);
        this.is_ios = this.config.get('is_ios');
        this.device_id = config.get('device_id');
        this.device_platform = config.get('device_platform');
        if (!this.config.get('animate')) {
            this.nav_opts = {animate: false};
        }
    }

    ionViewDidLeave(){
        this.logBackBtn();
    }


    logBackBtn(){
        this.locationService.getLocation().then(
            user_loc => {
                let optionalArgs = {};
                console.log("logging back button");
                optionalArgs["user_id"] = null;
                this.buttonLogService.logButton(this.device_id, this.device_platform, user_loc.latitude,
                                                user_loc.longitude, this.pageName, "back", optionalArgs);
                
            });
    }

    ionViewDidLoad() {
        this.loadRouteNames();
    }

    ionViewDidEnter() {
        this.getLocation();

        let headerElem = document.getElementById('header');
        this.voiceOverService.setVoiceOverFocus(headerElem);
    }

    //load the route direction and route name strings into a two by two array for displaying on the page
    loadRouteNames(){
        let numOfRows = (this.schedules.length / this.numOfColumns) + 1;
        let index = 0;
        for (let i = 0; i < numOfRows; i++){
            this.routesTable.push([]);
            for (let j = 0; index < this.schedules.length && j < this.numOfColumns; j++){
                let schedule = this.schedules[index];
                let obj = {str: '', routeId: schedule.routeId};
                if(schedule.directionId == 1){
                    obj.str = 'IN: ' + schedule.routeShortName;
                } else {
                    obj.str = 'OUT: ' + schedule.routeShortName;
                }
                this.routesTable[i].push(obj);
                index++;
            }
        }
    }

    getLocation() {
        this.locationService.getLocation().then(
            user_loc => {
                this.userLat = user_loc.latitude;
                this.userLon = user_loc.longitude;
                this.loadMap();
            },
            error => {
                console.log('getLocation failed with code ' + error.code + ' and message ' + error.message);
            }
        );
    }

    speakStarButton() {
        return this.isFav ? 'Marked as favorite. Unmark' : 'Mark as favorite';
    }

    toggleFavorite(){
        this.isFav = !this.isFav;
        this.favStopsService.toggleInStopPage();
    }

    showRoutePage(routeId : string){
        let schedule = null;
        for(let scheduleEntry of this.schedules){
            if(scheduleEntry.routeId == routeId){
                schedule = scheduleEntry;
            }
        }


        /* logging show route page button*/
        this.locationService.getLocation().then(
            user_loc => {
                console.log("Logging route_page");
                let optionalArgs = {};
                optionalArgs["route_id"] = schedule.routeShortName;
                optionalArgs["stop_id"] = schedule.stopId;
                optionalArgs["trip_id"] = schedule.tripId;
                optionalArgs["user_id"] = null;
                
                
                this.buttonLogService.logButton(this.device_id, this.device_platform, user_loc.latitude,
                                                user_loc.longitude, this.pageName, "route_page", optionalArgs);
            });


        console.log(schedule, this.nav_opts);
        this.nav.push(RouteSchedulePage, {schedule: schedule,
                                          config: this.config}, this.nav_opts);

    }

    speakRoutesSummary() {
        let text = 'See details for: ';
        for (let schedule of this.schedules) {
            if (schedule.directionId == 1) {
                text = text + 'Inbound ';
            } else {
                text = text + 'Outbound ';
            }
            text = text + schedule.routeShortName + ', ';
        }
        return text;
    }

    logShowRoutePage(){
        console.log("logging SHOW_ROUTE_PAGE");
    }
}




