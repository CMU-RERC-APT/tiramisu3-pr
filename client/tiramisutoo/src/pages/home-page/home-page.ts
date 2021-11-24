import { ModalController, Platform, NavController, AlertController, Config, Content, Events } from 'ionic-angular';
import { Storage } from '@ionic/storage'
import { NgZone, Component } from '@angular/core';

import { Push } from '@ionic-native/push';
import { Keyboard } from '@ionic-native/keyboard';
import { Diagnostic } from '@ionic-native/diagnostic';
import { MobileAccessibility } from '@ionic-native/mobile-accessibility';

import { Subscription } from 'rxjs/Subscription';
//import './rxjs-operators';

//import {RoutePage} from '../route-page/route-page';
import {ConsentPage} from '../consent-page/consent-page';
import {RouteSchedulePage} from '../route-schedule-page';
import {StopPage} from '../stop-page/stop-page';
import {SettingsPage} from '../set-page/set-page';
import {DisplayedRoute} from '../../model/displayed-route';
import {ScheduleEntry} from '../../model/schedule-entry';
import {Stop} from '../../model/stop';
//import {MapSearchPlace} from '../../model/map-search-place';
import {ButtonLogService} from "../../providers/button-logging.service" ;
import {InOutService} from "../../providers/inout.service";
import {NearbyRoutesService} from '../../providers/nearby-routes.service';
//import {ScheduleService} from '../../providers/schedule.service';
import { SchedulesForLocationService } from '../../providers/schedules-for-location.service';
import {LocationService} from '../../providers/location.service';
import {DistanceService} from '../../providers/distance.service';
import {SchedulesFromStopService} from '../../providers/schedules-from-stop.service';
import {FavRoutesService} from '../../providers/fav-routes.service';
import {FavStopsService} from '../../providers/fav-stops.service';
import {AlarmService} from '../../providers/alarm.service';
import {StopsForLocationService} from '../../providers/stops-for-location.service';
import {StringUtilityService} from '../../providers/string-utility.service';
import {TimeUtilityService} from '../../providers/time-utility.service';
import {DataUtilityService} from '../../providers/data-utility.service';
import {SearchHistoryService} from '../../providers/search-history.service';
import { VoiceOverService } from '../../providers/voice-over.service';
import { NumScheduleRowsLogService } from '../../providers/num-schedule-rows-log.service';
import {HandleSettingsService} from '../../providers/handle-settings.service';

//import { Http, Response, RequestOptions } from '@angular/http';

@Component({
    templateUrl: 'home-page.html',
    selector: 'page-home',
    providers: [ButtonLogService, InOutService, NearbyRoutesService, SchedulesForLocationService, LocationService, StopsForLocationService,
                DistanceService, SchedulesFromStopService, TimeUtilityService, Keyboard,
                FavRoutesService, DataUtilityService, SearchHistoryService, Diagnostic, 
                VoiceOverService, NumScheduleRowsLogService, HandleSettingsService]
})

export class HomePage {

    public pageName : string = 'home_page';
    public mapExpanded: boolean = false;
    public map: google.maps.Map;
    private mapMoveTimer;
    private scrollTimer;
    private logScrollStart: boolean = true; 
    private shouldInitializeMap: boolean = true;
    private currentLocationMarker: google.maps.Marker;
    // maps stopId to marker on google map
    private markersMap: { [key: string]: google.maps.Marker; } = {};

    private nearbyRoutesSub: Subscription;
    private nearbyStopsSub: Subscription;
    private nearbySchedulesSub: Subscription;

    private currNearbyStops : Stop[] = [];
    private closestStop : Stop;
    private closestStopText : string;

    private settingsDic;
    
    public selectedRouteList: DisplayedRoute[] = [];
    public nearbyRoutes: DisplayedRoute[] = [];
    public nearbyStops: Stop[] = [];
    public nearbySchedules: ScheduleEntry[] = [];
    // schedules that go through favorite stops are shown at top
    public topSchedules: ScheduleEntry[] = [];
    // nearbySchedules indexed by each stopId
    public stopIdIndexedSchedules: { [key: string]: ScheduleEntry[] } = {};
    public lat: number;
    public lon: number;
    public centeredOnUserLocation: boolean = true;

    private paacBounds: {[key: string]: number[]} = {};

    public inFilter: boolean = false;
    public outFilter: boolean = false;

    public inFilterClass: string = 'inOutFilterOff';
    public outFilterClass: string = 'inOutFilterOff';

    public selectedStopSet: { [key: string]: Stop } = {};

    // the setInterval id of calling getNearbySchedules()
    private updateIntervalId;
    private checkLocationIntervalId;

    // html element holding autocomplete results
    public searchToggle: boolean = false;
    public searchSuggestionsHeight: number;

    public cancelButtonClass: string = "hideCancelButton"

    public mapClass: string = "schedule-no-map";

    public ariaHidden: boolean = false;

    private keyboardHeight: number = 0;
    private keyboardOpen: boolean = false;
    
    public listMargin: string = "16px";

    private autocompleteService: google.maps.places.AutocompleteService;
    private placesService: google.maps.places.PlacesService;
    public recentSearches: any[] = [];
    public searchQuery: string = "";
    public autocompleteResults: any[] = [];

    public loading = true;

    private registration_id: string;
    private device_id: string;
    private device_platform: string;

    public is_ios: boolean;
    private nav_opts = {animate: true};
    private schedulesRefreshInterval: number;
    public voiceOverEnabled: boolean = false;

    public origin: google.maps.Point = new google.maps.Point(0, 0);
    public spriteSize: google.maps.Size = new google.maps.Size(200,220);
    private anchor = null; // defaults to center of bottom

    public current_location_icon = {
        url: "assets/circle_marker.png", // url
        scaledSize: new google.maps.Size(25, 25), // scaled size
        origin: this.origin,
        anchor: this.anchor,
    };
    public blue_stop_icon = {
        url: "assets/icon_set_v1.png", // url
        scaledSize: this.spriteSize,
        size: new google.maps.Size(21,24),
        origin: new google.maps.Point(13,95),
        anchor: this.anchor,
    };
    public grey_stop_icon = {
        url: "assets/icon_set_v1.png", // url
        scaledSize: this.spriteSize,
        size: new google.maps.Size(21, 24), // scaled size
        origin: new google.maps.Point(64,95),
        anchor: this.anchor,
    };
    public blue_starred_stop_icon = {
        url: "assets/icon_set_v1.png", // url
        scaledSize: this.spriteSize,
        size: new google.maps.Size(21, 24), // scaled size
        origin: new google.maps.Point(113,95),
        anchor: this.anchor,
    };
    public grey_starred_stop_icon = {
        url: "assets/icon_set_v1.png", // url
        scaledSize: this.spriteSize,
        size: new google.maps.Size(21, 24), // scaled size
        origin: new google.maps.Point(163,95),
        anchor: this.anchor,
    };

    toggleMap() {

        this.mapExpanded = !this.mapExpanded;
        if (this.mapExpanded) {
            this.mapClass = 'schedule-map';
            setTimeout(() => {
                this.triggerResizeMap();
            }, 5);
        } else {
            this.mapClass = 'schedule-no-map';
        }

        if (this.mapExpanded && this.shouldInitializeMap) {
            //After going from route page back to home page and clicking on the map toggle,
            //somehow the map container isn't expanded yet when this line is reached,
            //which will result in a blank map.
            //Using setTimeout is a temporary workaround that allows the map to expand to full height first.
            setTimeout(() => {
                this.shouldInitializeMap = false;
                this.loadMap();
            }, 0);
        }
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
    getMapHeight() {
        if (this.mapExpanded) {
            return '220px';
        } else {
            return '0px';
        }
    }

    // Currently required for Android
    triggerResizeMap() {
        if (this.map) {
            google.maps.event.trigger(this.map, "resize");
            let center = new google.maps.LatLng(this.lat, this.lon);
            this.map.setCenter(center);
        }
    }

    getMapArrow() {
        return this.mapExpanded ? "arrow-up" : "arrow-down";
    }

    // a stop is filtered out iff all its associated route/direction pairs are filtered out
    isStopFilteredOut(stopId: string) {
        let schedules = this.stopIdIndexedSchedules[stopId];
        if (!schedules) { // no schedules associated with this stop
            return true;
        }
        for (let schedule of schedules) {
            if (this.shouldDisplay(schedule.routeId, schedule.directionId)) {
                return false;
            }
        }
        return true;
    }

    getIconForStop(stopId: string) {
        if (this.isStopFilteredOut(stopId)) {
            if (this.selectedStopSet[stopId]) {
                return this.grey_starred_stop_icon;
            } else {
                return this.grey_stop_icon;
            }
        } else {
            if (this.selectedStopSet[stopId]) {
                return this.blue_starred_stop_icon;
            } else {
                return this.blue_stop_icon;
            }
        }
    }

    getZIndexForStop(stopId: string) {
        if (this.isStopFilteredOut(stopId)) {
            if (this.selectedStopSet[stopId]) {
                return 2;
            } else {
                return 1;
            }
        } else {
            if (this.selectedStopSet[stopId]) {
                return 3;
            } else {
                return 4;
            }
        }
    }

    clearMarkers() {
        console.log("clearing markers");
        if (this.map) {
            let mapBounds = this.map.getBounds();
            for (let stopId in this.markersMap) {
                let marker = this.markersMap[stopId];
                if (!mapBounds.contains(marker.getPosition())) {
                    marker.setMap(null);
                    delete this.markersMap[stopId];
                }
                else {
                    let icon = this.getIconForStop(stopId);
                    this.markersMap[stopId].setIcon(icon);

                    let zIndex = this.getZIndexForStop(stopId);
                    this.markersMap[stopId].setZIndex(zIndex);
                }
            }
        } else {
            this.markersMap = {};
        }
    }

    addMarkersForStops() {
        console.log("adding markers for stops");
        if (this.map) {
            let mapBounds: google.maps.LatLngBounds = this.map.getBounds();
            console.log(mapBounds);
            for (let stop of this.nearbyStops) {
                let stopLocation = new google.maps.LatLng(stop.lat, stop.lon);
                if (!(stop.id in this.markersMap) && mapBounds.contains(stopLocation)) {
                    let marker = new google.maps.Marker({
                        map: this.map,
                        //animation: google.maps.Animation.DROP,
                        position: stopLocation,
                        optimized: false,
                        icon: this.getIconForStop(stop.id),
                        zIndex: this.getZIndexForStop(stop.id)
                    });
                    marker.addListener('click', this.centerOnStop(marker, stop));
                    this.markersMap[stop.id] = marker;
                }
            }
            //console.log(this.markersMap)
        }
    }

    resetStopIcons() {
        if (this.map) {
            console.log('reset icons');
            for (let stopId in this.markersMap) {
                let icon = this.getIconForStop(stopId);
                this.markersMap[stopId].setIcon(icon);

                let zIndex = this.getZIndexForStop(stopId);
                this.markersMap[stopId].setZIndex(zIndex);
            }
        }
    }

    centerOnStop(marker, stop) {
        return () => {
            this.cancelUpdate();
            this.centeredOnUserLocation = false;
            this.map.panTo(marker.getPosition());
            this.logMapStopBtn(stop);
            //this.onCenterChanged(marker.getPosition().lat(), marker.getPosition().lng());
        };
    }

    createCenterControl(controlDiv) {
        let yourLocationButton = document.createElement('button');
        yourLocationButton.id = 'your-location-icon';
        yourLocationButton.title = 'Go to my location';
        controlDiv.appendChild(yourLocationButton);

        yourLocationButton.addEventListener('click', this.mapUserLocation);
    }

    // we are using this type of function definition so it can be called in the event listener
    mapUserLocation = () => {
        this.centerOnUserLocation();
        this.logMapUserLocationBtn();
    }

    centerOnUserLocation() {
        this.cancelUpdate();
        this.turnOffSearch();
        this.locationService.getLocation().then(
            user_loc => {
                this.centeredOnUserLocation = true;
                if (this.map) {
                    let center = new google.maps.LatLng(user_loc.latitude, user_loc.longitude);
                    this.map.panTo(center);
                    this.currentLocationMarker.setPosition(center);
                } else {
                    this.onCenterChanged(user_loc.latitude, user_loc.longitude);
                }
            });
    }

    // called when the center of map changes
    onCenterChanged(lat: number, lon: number) {
        //console.log("center changed");
        //this.centeredOnUserLocation = false;
        this.lat = lat
        this.lon = lon;
        this.loading = true;
        this.getNearbyRoutes(lat, lon);
        this.getNearbyStops(lat, lon);
        this.getNearbySchedules(lat, lon);
    }

    cancelUpdate() {
        this.nearbyRoutesSub.unsubscribe();
        this.nearbyStopsSub.unsubscribe();
        this.nearbySchedulesSub.unsubscribe();
    }

    loadMap() {
        console.log('loadMap()');

        // set up current location marker
        this.locationService.getLocation().then(
            user_loc => {
                let userCenter = new google.maps.LatLng(user_loc.latitude, user_loc.longitude);
                let lastSearchCenter = new google.maps.LatLng(this.lat, this.lon);
                let mapOptions = {
                    center: (this.centeredOnUserLocation) ? userCenter : lastSearchCenter,
                    zoom: 15,
                    disableDefaultUI: true,
                    mapTypeId: google.maps.MapTypeId.ROADMAP
                };

                this.map = new google.maps.Map(document.getElementById("map"), mapOptions);
                this.placesService = new google.maps.places.PlacesService(this.map);

                this.currentLocationMarker = new google.maps.Marker({
                    map: this.map,
                    animation: google.maps.Animation.DROP,
                    position: userCenter,
                    icon: this.current_location_icon,
                    optimized: false,
                    zIndex: 5
                });

                // set up the 'go to your location' button
                let centerControlDiv = document.createElement('div');
                this.createCenterControl(centerControlDiv);
                this.map.controls[google.maps.ControlPosition.RIGHT_BOTTOM].push(centerControlDiv);

                //Need to wait for map to open first time before loading stops
                google.maps.event.addListenerOnce(this.map, 'bounds_changed', () => {
                    this.addMarkersForStops();
                });

                this.map.addListener('idle', () => {
                    this.zone.run(() => {
                        this.mapMoveTimer = setTimeout(() => {
                            console.log("move finished");
                            let center = this.map.getCenter();
                            //this.currentLocationMarker.setPosition(center);
                            this.onCenterChanged(center.lat(), center.lng());
                        },750);
                    });
                });

                this.map.addListener('dragstart', () => {
                    this.zone.run(() => {
                        clearTimeout(this.mapMoveTimer);
                        this.cancelUpdate();
                        this.centeredOnUserLocation = false;
                    });
                });
            }
        );

    };



    constructor(private buttonLogService : ButtonLogService,
                private nearbyRoutesService: NearbyRoutesService,
                private scheduleService: SchedulesForLocationService,
                private locationService: LocationService,
                private stopsService: StopsForLocationService,
                private favStopsService: FavStopsService,
                private favRoutesService: FavRoutesService,
                private inoutService: InOutService,
                private alarmService: AlarmService,
                private stringUtilityService: StringUtilityService,
                private timeUtilityService: TimeUtilityService,
                private dataUtilityService: DataUtilityService,
                private searchHistoryService: SearchHistoryService,
                public nav: NavController,
                public events: Events,
                private alertCtrl: AlertController,
                private modalCtrl: ModalController,
                private platform: Platform,
                private config: Config,
                private zone: NgZone,
                private keyboard: Keyboard,
                private diagnostic: Diagnostic,
                public database: Storage,
                private distanceService : DistanceService,
                public mobileAccessibility: MobileAccessibility,
                private numScheduleRowsLogService: NumScheduleRowsLogService,
                public voiceOverService: VoiceOverService,
                public handleSettingsService: HandleSettingsService) {
    }
    
    ngOnInit() {
        //Using window event listener because (onScroll) is not detected on Ionic some elements
        window.addEventListener('scroll', (event: any)=>{
            //console.log(event);
            let elem: any = event.target;
            //console.log(elem);
            while(elem !== null) {
                if (elem.id == "schedule_list" || elem.id == "route_filter" || elem.id == "trip_headsign" || elem.id == "stop_name")
                {
                    //console.log(elem.id);
                    //console.log(elem);
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

        this.platform.ready().then((readySource) => {
            console.log('platform ready');
            this.paacBounds = this.config.get('paacBounds');
            console.log(this.paacBounds);
            this.is_ios = this.config.get('is_ios');
            this.device_id = this.config.get('device_id');
            this.device_platform = this.config.get('device_platform');
            // this.registerPushNotification();
            this.schedulesRefreshInterval = this.config.get('schedulesRefreshInterval');
            this.loading = true;
            this.autocompleteService = new google.maps.places.AutocompleteService();

            /* DeadCode needs to be replaced.

               let accessibility = this.config.get('accessibility');
               if (accessibility) {
               accessibility.isScreenReaderRunning((result) => {
               if (result) {
               // disable page transition animation
               this.config.set('animate', '');
               this.nav_opts = {animate: false};
               console.log('screenreader is on');
               } else {
               console.log('screenreader is off');
               }
               });
               }
            */

            this.mobileAccessibility.isScreenReaderRunning().then(result => {
                this.voiceOverEnabled = result;
                let headerElem = document.getElementById('search-input-container');
                this.voiceOverService.setVoiceOverFocus(headerElem);
            });

            this.database.get('consent').then((value)=>{
                console.log("consent: value = ", value);
                if (!value){
                    console.log("presenting consent page");
                    this.presentModal();
                }
            });

            this.getFavStops();
            this.read_Settings();
            

            this.diagnostic.isLocationAvailable().then(
                available => {
                    if (available) {
                        this.updateWithLocation();
                    } else {
                        console.log("pausing for location");
                        setTimeout(() => {
                            this.diagnostic.isLocationAuthorized().then(
                                authorized => {
                                    if (authorized) {
                                        this.diagnostic.isLocationAvailable().then(
                                            available => {
                                                if (available) {
                                                    this.updateWithLocation();
                                                } else {
                                                    let alert = this.alertCtrl.create({
                                                        title: "Location Unavailable",
                                                        subTitle: "Defaulting to last known location",
                                                        buttons: ['Dismiss']
                                                    });
                                                    alert.present(alert);
                                                    this.watchLocationStatus();
                                                    this.setFilters();
                                                    this.update();
                                                }
                                            });
                                    } else {
                                        this.watchLocationStatus();
                                        this.setFilters();
                                        this.update();
                                    }
                                });
                        }, 5000);
                    }
                },
                error => {
                    console.log(error);
                    this.updateWithLocation();
                });

            //this.removeFavRoutes();
            this.getFavStops();

            // call getNearbySchedules() every 30s
            this.updateIntervalId = setInterval(() => {
                this.update();
            }, this.schedulesRefreshInterval);

            this.searchSuggestionsHeight = window.innerHeight - 70;
            if(this.is_ios) {
                this.listMargin = "13px";
            }

            this.keyboard.disableScroll(true);
            this.keyboard.onKeyboardShow().subscribe(event => {
                if (!this.keyboardOpen) {
                    this.keyboardHeight = event.keyboardHeight;
                    console.log("keyboard height: " + this.keyboardHeight);
                    this.searchSuggestionsHeight -= this.keyboardHeight;
                    this.keyboardOpen = true;
                }
            });
            this.keyboard.onKeyboardHide().subscribe(event => {
                if (this.keyboardOpen) {
                    console.log("keyboard hidden");
                    this.searchSuggestionsHeight += this.keyboardHeight;
                    this.keyboardOpen = false;
                }
            });

            this.events.subscribe('user:data-refresh', () => {
                this.setInOut()
            });

        }, (error) => {
            console.log(error);
        });
    }

    updateWithLocation() {
        console.log("updating with location");
        this.locationService.updateCache().then(
            resp => {
                this.setFilters();
                this.update();
                this.locationService.startWatch();
            });
    }

    watchLocationStatus() {
        this.checkLocationIntervalId = setInterval(() => {
            this.diagnostic.isLocationAvailable().then(
                available => {
                    if (available) {
                        clearInterval(this.checkLocationIntervalId);
                        this.updateWithLocation();
                    }
                }
            )
        }, 5000);
    }

    presentModal(){
        this.ariaHidden = true;
        let consentPageModal = this.modalCtrl.create(ConsentPage, {database: this.database});
        consentPageModal.onDidDismiss( () => {
            this.ariaHidden = false;
        });

        /*consentPageModal.onDismiss(consent =>{
          console.log("consent page returns:", consent);
          if (consent){
          this.database.set("consent", true);
          }
          else{
          this.platform.exitApp();
          }
          });*/
        consentPageModal.present();
        //consentPageModal.present(consentPageModal);
    }

    /* logging functions */

    /*
     * HomePage: Log when user pushes the location bar
     */

    logNumScheduleRows(user_lat: number, user_lon: number) {
        let totalRows: number = 0;
        let numFavorite: number = 0;

        for (let schedule of this.nearbySchedules) {
            if (this.shouldDisplay(schedule.routeId, schedule.directionId)) {
                totalRows += 1;
                if(this.selectedStopSet[schedule.stopId]) {
                    numFavorite += 1;
                }
            }
        }

        this.numScheduleRowsLogService.logNumRow(this.device_id, user_lat, user_lon, totalRows, numFavorite);
    }

    logLocationBar() {
        this.locationService.getLocation().then(
            user_loc => {
                let optionalArgs = {};

                optionalArgs["user_id"] = null;
                optionalArgs["stop_id"] = (this.closestStop == null) ? null : this.closestStop.id;

                console.log("Logging Location Bar");
                this.buttonLogService.logButton(this.device_id, this.device_platform, user_loc.latitude,
                                                user_loc.longitude, this.pageName, "location_bar", optionalArgs);
            });

    }

    logFilterBtn(direction: string, event: string) {
        this.locationService.getLocation().then(
            user_loc => {
                let optionalArgs = {};
                optionalArgs["event"] = event;
                //optionalArgs["user_id"] = null;
                this.buttonLogService.logButton(this.device_id, this.device_platform, user_loc.latitude,
                                                user_loc.longitude, this.pageName, direction + "_filter", optionalArgs);
            });
    }

    logMapStopBtn(stop) {
        this.locationService.getLocation().then(
            user_loc => {
                let optionalArgs = {};
                optionalArgs["stop_id"] = stop.id
                //optionalArgs["user_id"] = null;
                this.buttonLogService.logButton(this.device_id, this.device_platform, user_loc.latitude,
                                                user_loc.longitude, this.pageName, "map_stop", optionalArgs);
            });
    }

    logMapUserLocationBtn() {
        this.locationService.getLocation().then(
            user_loc => {
                let optionalArgs = {};
                //optionalArgs["user_id"] = null;
                this.buttonLogService.logButton(this.device_id, this.device_platform, user_loc.latitude,
                                                user_loc.longitude, this.pageName, "map_user_loc", optionalArgs);
            });
    }

    /*
     * HomePage: Log when user pushes the stop name button on the top each route row
     */

    logStopPageBtn(schedule){
        this.locationService.getLocation().then(
            user_loc => {
                let optionalArgs = {};
                optionalArgs["route_id"] = schedule.routeShortName;
                optionalArgs["stop_id"] = schedule.stopId;
                optionalArgs["trip_id"] = schedule.tripId;
                //optionalArgs["user_id"] = null;
                optionalArgs["arrival_time"] = this.timeUtilityService.getArrivalTime(schedule);

                console.log("Logging stop_page");
                this.buttonLogService.logButton(this.device_id, this.device_platform, user_loc.latitude,
                                                user_loc.longitude, this.pageName, "stop_page",optionalArgs);
            });
    }

    /*
     * HomePage: Log when user pushes the orange route page button on the left
     */

    /*logShortNameRoutePageBtn(schedule) {
      this.locationService.getLocation().then(
      user_loc => {
      console.log("Logging SHORT_NAME_ROUTE_PAGE");
      console.log(schedule);
      let optionalArgs = {};
      optionalArgs["route_id"] = schedule.routeShortName;
      optionalArgs["stop_id"] = schedule.stopId;
      optionalArgs["trip_id"] = schedule.tripId;
      optionalArgs["user_id"] = null;
      optionalArgs["arrival_time"] = this.timeUtilityService.getArrivalTime(schedule);

      this.buttonLogService.logButton(this.device_id, this.device_platform, user_loc.latitude,
      user_loc.longitude, this.pageName, "SHORT_NAME_ROUTE_PAGE", optionalArgs);
      });
      }*/

    /*
     * HomePage: Log when user pushes the blue route page button on the bottom of each route row
     */

    logRoutePageBtn(schedule){
        this.locationService.getLocation().then(
            user_loc => {
                let optionalArgs = {};
                optionalArgs["route_id"] = schedule.routeShortName;
                optionalArgs["stop_id"] = schedule.stopId;
                optionalArgs["trip_id"] = schedule.tripId;
                //optionalArgs["user_id"] = null;
                optionalArgs["arrival_time"] = this.timeUtilityService.getArrivalTime(schedule);

                console.log("Logging route_page");
                this.buttonLogService.logButton(this.device_id, this.device_platform, user_loc.latitude,
                                                user_loc.longitude, this.pageName, "route_page", optionalArgs);
            });
    }

    /*
     * HomePage: Log when user toggles the map button
     */

    logToggleMapBtn(){
        this.locationService.getLocation().then(
            user_loc => {
                let optionalArgs = {};
                //optionalArgs["user_id"] = null;
                console.log("Logging toggle_map");
                this.buttonLogService.logButton(this.device_id, this.device_platform, user_loc.latitude,
                                                user_loc.longitude, this.pageName, "toggle_map",optionalArgs);
            });
    }


    ionViewDidEnter() {
        this.update();
        //this.setFilters();
        if (this.favStopsService.toggledInStopPage) {
            let toggledStopId = this.favStopsService.toggledStopId;
            if (toggledStopId in this.selectedStopSet) {
                console.log('unfav');
                this.unselectStop(toggledStopId);
            } else {
                console.log('fav');
                this.selectStop(toggledStopId);
            }
        };
    }

    autocompleteSearch() {
        if(this.searchQuery.trim() != "") {
            //console.log("autocomplete triggered");
            this.autocompleteService.getPlacePredictions({input: this.searchQuery,
                                                          location: new google.maps.LatLng(this.lat, this.lon),
                                                          radius: 20000},
                                                         (predictions, status) => {
                                                             //console.log("got autocomplete result");
                                                             if(status == google.maps.places.PlacesServiceStatus.OK) {
                                                                 this.zone.run(() => {
                                                                     this.autocompleteResults = predictions;
                                                                 });
                                                             }
                                                         });
        } else {
            this.autocompleteResults = [];
        }
    }

    submitSearch(event) {
        console.log("search submitted");
        let searchQuery = event.target.value;
        if (searchQuery.trim() != "") {
            this.selectAutocompleteSearch(this.autocompleteResults[0]);
        }
        this.keyboard.close();
        this.turnOffSearch();
    }

    selectAutocompleteSearch(autocompleteResult) {
        this.cancelUpdate();
        this.turnOffSearch();
        this.placesService.getDetails({placeId: autocompleteResult.place_id},
                                      (place, status) => {
                                          if(status == google.maps.places.PlacesServiceStatus.OK) {
                                              let lat = place.geometry.location.lat();
                                              let lon = place.geometry.location.lng();
                                              let center = new google.maps.LatLng(lat, lon);
                                              if (this.map) {
                                                  this.map.panTo(center);
                                              } else {
                                                  this.onCenterChanged(center.lat(), center.lng());
                                              }
                                              this.centeredOnUserLocation = false;

                                              let newSearch = {name: autocompleteResult.description, place_lat: lat, place_lon: lon };
                                              this.recentSearches.unshift(newSearch);

                                              this.locationService.getLocation().then(
                                                  user_loc => {
                                                      this.searchHistoryService.writeSearchHistory(this.device_id, user_loc.latitude, user_loc.longitude, newSearch).subscribe(res=>{});
                                                  });
                                          }
                                      });
    }

    selectStopSearch(stopId: string) {
        this.turnOffSearch();
        let stop = this.selectedStopSet[stopId];
        let center = new google.maps.LatLng(stop.lat, stop.lon);
        if (this.map) {
            this.map.panTo(center);
        } else {
            this.onCenterChanged(center.lat(), center.lng());
        }
        this.centeredOnUserLocation = false;
        this.locationService.getLocation().then(
            user_loc => {
                var optionalArgs = {};
                optionalArgs['stop_id'] = stopId;
                //optionalArgs["user_id"] = null;
                console.log("Logging select_stop_search")
                this.buttonLogService.logButton(this.device_id, this.device_platform, user_loc.latitude,
                                                user_loc.longitude, this.pageName, "select_stop_search", optionalArgs);

            });
    }

    selectRecentSearch(search) {
        // create new entry in the user_data.search table, but mark as recent search.
        search.is_recent = true;
        this.locationService.getLocation().then(
            user_loc => {
                this.searchHistoryService.writeSearchHistory(this.device_id, user_loc.latitude, user_loc.longitude, search).subscribe(res=>{});
            });
        //this.searchHistoryService.writeSearchHistory(this.device_id, search).subscribe(res=>{});

        this.turnOffSearch();
        let center = new google.maps.LatLng(search.place_lat, search.place_lon);
        if (this.map) {
            this.map.panTo(center);
        } else {
            this.onCenterChanged(center.lat(), center.lng());
        }
        this.centeredOnUserLocation = false;
    }

    // called when user clicks on search input field
    startSearch() {
        //Forces the cancel button to always show for voice over users
        if (this.is_ios) {
            this.cancelButtonClass = "showCancelButton";
        }
        if (!this.searchToggle) {
            this.searchToggle = true;

            // We need to load the map for the places service
            // but we don't want to expand it
            if (!this.map) {
                this.loadMap();
            }
            /*if (!this.mapExpanded) {
              this.toggleMap();
              }*/

            this.searchHistoryService.getSearchHistory(this.device_id).subscribe(
                list => {
                    this.recentSearches = list;
                });
        }
    }

    turnOffSearch() {
        if (this.is_ios) {
            this.cancelButtonClass = "hideCancelButton";
        }
        console.log('turnoff search');
        this.searchToggle = false;
        this.searchQuery = "";
        this.autocompleteResults = [];
        if(this.voiceOverEnabled) {
            let headerElem = document.getElementById('search-input-container');
            this.voiceOverService.setVoiceOverFocus(headerElem);
        }
    }

    /*getLocation() {
      this.locationService.getLocation().then(
      user_loc => {
      this.lat = user_loc.latitude;
      this.lon = user_loc.longitude;
      //console.log(this.lat, this.lon);
      this.clearScheduleData();
      this.getNearbyRoutes();
      this.getNearbyStops();
      },
      error => {
      console.error("getLocation error");
      });
      }*/

    getAlarms() {
        // temporary user_id
        this.alarmService.getAlarms(this.registration_id, this.device_id, this.device_platform, 'tiramisu');
    }

    update() {
        this.locationService.getLocation().then(
            user_loc => {
                let center = new google.maps.LatLng(user_loc.latitude, user_loc.longitude);
                if (this.currentLocationMarker) {
                    this.currentLocationMarker.setPosition(center);
                }

                if(this.centeredOnUserLocation){
                    if (this.map) {
                        this.map.panTo(center);
                    }

                    this.lat = user_loc.latitude;
                    this.lon = user_loc.longitude;
                    this.getNearbyRoutes(user_loc.latitude, user_loc.longitude);
                    this.getNearbyStops(user_loc.latitude, user_loc.longitude);
                    this.getNearbySchedules(user_loc.latitude, user_loc.longitude);
                } else {
                    this.getNearbySchedules(this.lat, this.lon)
                }
                this.getClosestStop(user_loc.latitude, user_loc.longitude);
            });
    }

    getFavRoutes() {
        this.locationService.getLocation().then(
            user_loc => {
                this.favRoutesService.getFavRoutes(this.device_id, user_loc.latitude, user_loc.longitude).subscribe(
                    () => {
                        //console.log(routes);
                        this.getNearbyRoutes(user_loc.latitude, user_loc.longitude);
                    },
                    error => {
                        console.log('getFavRoutes failed with code ' + error.code + ' and message ' + error.message);
                    }
                );

            });
    }

    removeFavRoutes() {
        this.locationService.getLocation().then(
            user_loc => {
                this.favRoutesService.removeAllRoutes(this.device_id, user_loc.latitude, user_loc.longitude);
            });
    }

    hasFilterRoutes(){
        if(this.selectedRouteList.length > 0) {
            return true;
        }
        return false;
    }

    getNearbyRoutes(lat: number, lon: number) {
        this.nearbyRoutesSub = this.nearbyRoutesService.getNearbyRoutes(lat, lon).subscribe(
            routes => {
                //console.log(routes);
                this.selectedRouteList = []
                this.nearbyRoutes = []
                for (let route of routes) {
                    if(this.favRoutesService.selectedRouteSet[route.routeId]) {
                        route.selector = this.favRoutesService.selectedRouteSet[route.routeId].selector;
                        this.selectedRouteList.push(route);
                    } else {
                        this.nearbyRoutes.push(route);
                    }
                }
                this.selectedRouteList.sort(this.orderRoutes);
                this.nearbyRoutes.sort(this.orderRoutes);
            },
            error => {
                console.log('getNearbyRoutes failed with code ' + error.code + ' and message ' + error.message);
            }
        );
    }

    getFavStops() {
        this.locationService.getLocation().then(
            user_loc => {
                this.favStopsService.getFavStops(this.device_id, user_loc.latitude, user_loc.longitude).subscribe(
                    stops => {
                        this.selectedStopSet = stops;
                    },
                    error => {
                        console.log('getFavStops failed with code ' + error.code + ' and message ' + error.message);
                    }
                );
            });
    }

    getNearbyStops(lat: number, lon: number) {
        this.nearbyStopsSub = this.stopsService.getStopsForLocation(lat, lon).subscribe(
            stops => {
                this.nearbyStops = stops;
                this.clearMarkers();
                this.addMarkersForStops();
            },
            error => {
                console.log('getNearbyStops failed with code ' + error.code + ' and message ' + error.message);
            }
        );
    }

    getClosestStop(lat : number, lon : number) {
        this.stopsService.getStopsForLocation(lat, lon).subscribe(
            stops => {
                this.currNearbyStops = stops;
                if(this.currNearbyStops.length <= 0) {
                    this.closestStop = null;
                }
                else {
                    this.closestStop = this.currNearbyStops[0];
                    let dist : number = this.distanceService.distance(lat, this.closestStop.lat, lon, this.closestStop.lon);
                    for (let stop of this.currNearbyStops) {
                        let tempDist : number = this.distanceService.distance(lat, stop.lat, lon, stop.lon);
                        if (tempDist < dist) {
                            dist = tempDist;
                            this.closestStop = stop;
                        }
                    }
                }
                if(this.closestStop == null) {
                    this.closestStopText = "No Stops Found";
                }
                else {
                    this.closestStopText = this.closestStop.name;
                }
            },
            error => {
                console.log('getClosestStop failed with code ' + error.code + ' and message ' + error.message);
            }
        );
    }

    getNearbySchedules(lat: number, lon: number) {
        console.log('calling schedule service');
        this.nearbySchedulesSub = this.scheduleService.getSchedules(lat, lon).subscribe(
            schedules => {
                this.sortSchedules(schedules);
                console.log("finished getting schedules");
                this.clearMarkers();
                //this.addMarkersForStops();
                this.resetStopIcons();
                /*if (this.shouldInitializeMap) {
                  if (this.mapExpanded) {
                  this.loadMap();
                  this.shouldInitializeMap = false;
                  }
                  }*/
                this.loading = false;
                this.logNumScheduleRows(lat, lon);
            },
            error => {
                console.log('getNearbySchedules failed with code ' + error.code + ' and message ' + error.message);
            }
        );
    }

    sortSchedules(schedules: ScheduleEntry[]) {
        this.indexSchedulesByStopId(schedules);
        this.nearbySchedules = schedules.sort(this.orderSchedule);
        let topSchedules: ScheduleEntry[] = [];
        for (let stopId in this.selectedStopSet) {
            let schedules = this.stopIdIndexedSchedules[stopId];
            if (schedules) {
                for (let schedule of schedules) {
                    topSchedules.push(schedule);
                }
            }
        }
        this.topSchedules = topSchedules.sort(this.orderTopSchedule);
    }

    // build a mapping from each stopid to its list of schedules
    indexSchedulesByStopId(schedules: ScheduleEntry[]) {
        this.stopIdIndexedSchedules = {};
        for (let schedule of schedules) {
            let stopId = schedule.stopId;
            if (!this.stopIdIndexedSchedules[stopId]) { // newly encountered stop
                this.stopIdIndexedSchedules[stopId] = [];
            }
            this.stopIdIndexedSchedules[stopId].push(schedule);
        }
        // filter nearbyStops to include only nearest stops
        //this.nearbyStops = this.nearbyStops.filter(stop => stop.id in this.stopIdIndexedSchedules);
    }

    // top schedules are ordered by arrival time first
    orderTopSchedule(s1: ScheduleEntry, s2: ScheduleEntry) {
        if (s1.scheduledArrivalTime < s2.scheduledArrivalTime) {
            return -1;
        }
        if (s1.scheduledArrivalTime > s2.scheduledArrivalTime) {
            return 1;
        }

        if (s1.routeShortName < s2.routeShortName) {
            return -1;
        }
        if (s1.routeShortName > s2.routeShortName) {
            return 1;
        }

        if (s1.directionId > s2.directionId) {
            return -1;
        } else {
            return 1;
        }
    }

    // bottom schedules are ordered by route name first
    orderSchedule(s1: ScheduleEntry, s2: ScheduleEntry) {
        if (s1.routeShortName < s2.routeShortName) {
            return -1;
        }
        if (s1.routeShortName > s2.routeShortName) {
            return 1;
        }

        if (s1.directionId > s2.directionId) {
            return -1;
        }
        if (s1.directionId < s2.directionId) {
            return 1;
        }

        if (s1.scheduledArrivalTime < s2.scheduledArrivalTime) {
            return -1;
        } else {
            return 1;
        }
    }

    orderRoutes = (route1: DisplayedRoute, route2: DisplayedRoute) => {

        /*if (route1.routeShortName < route2.routeShortName) {
          return -1;
          }
          if (route1.routeShortName > route2.routeShortName) {
          return 1;
          }
          return 0;*/
        let rt1Name = route1.routeShortName;
        let rt2Name = route2.routeShortName;

        let res = this.stringUtilityService.orderRouteNames(rt1Name, rt2Name);
        //console.log(res);
        return res;
    }

    notFavStop(stopId: string) {
        return (!this.selectedStopSet[stopId]);
    }

    speakMapButton() {
        return this.mapExpanded ? "Hide map" : "Show map";
    }

    speakRouteFilterSummary() {
        let text = '';
        if (this.inPaac()) {
            if (this.inFilter) {
                text = text + 'Inbound, ';
            }
            if (this.outFilter) {
                text = text + 'Outbound, ';
            }
        }
        for (let routeId of this.selectedRouteList) {
            text = text + routeId.routeShortName + ', ';
        }
        if (text === '') {
            return 'No active route filters'
        } else {
            return 'Active route filters: ' + text
        }
    }

    speakScheduleSummary(schedule: ScheduleEntry) {
        return `${this.speakRouteInfo(schedule)} ${this.speakArrivalInfo(schedule)} ${this.speakStopInfo(schedule)} ${this.speakHeadsignInfo(schedule)}`;
    }

    speakRouteInfo(schedule: ScheduleEntry) {
        return `${schedule.routeShortName}, ${this.showDirection(schedule.directionId)}BOUND`;
    }

    speakStopInfo(schedule: ScheduleEntry) {
        return `at ${schedule.stopName}`;
    }

    speakHeadsignInfo(schedule: ScheduleEntry) {
        return `going to ${this.stringUtilityService.directionStringTrim(schedule.tripHeadsign)}`;
    }

    speakClosestStop() {
        return `closest stop is ${this.closestStopText}`;
    }

    speakArrivalInfo(schedule: ScheduleEntry) {
        let text: string = "";
        if (this.timeUtilityService.isInThePast(schedule)) {
            text += "Arrived"
            text += `${this.timeUtilityService.speakArrivalHourMin(schedule)} ago, `;
            text += "Upcoming arrivals"
            let arrivals: any = schedule.upcomingArrivals;
            for (let arrival of arrivals) {
                if (arrival.predicted && !this.timeUtilityService.hasSwitched(arrival)) {
                    text += `${this.timeUtilityService.speakArrivalHourMin(arrival)}, real time, `;
                }
                else {
                    text += `${this.timeUtilityService.speakArrivalHourMin(arrival)}, schedule, `;
                }
            }
        } else {
            text += "Upcoming arrivals";
            if (schedule.predicted && !this.timeUtilityService.hasSwitched(schedule)) {
                text += `${this.timeUtilityService.speakArrivalHourMin(schedule)}, real time, `;
            } else {
                text += `${this.timeUtilityService.speakArrivalHourMin(schedule)}, schedule, `;
            }
            let arrivals: any = schedule.upcomingArrivals;
            for (let arrival of arrivals) {
		if (arrival.predicted && !this.timeUtilityService.hasSwitched(arrival)) {
                    text += `${this.timeUtilityService.speakArrivalHourMin(arrival)}, real time, `;
                }
                else {
                    text += `${this.timeUtilityService.speakArrivalHourMin(arrival)}, schedule, `;
                }
            }
        }
        return text;
    }

    inPaac() {
        if (this.keys(this.paacBounds).length == 2) {
            if (this.lat > this.paacBounds['lowerLeft'][0] &&
                this.lat < this.paacBounds['upperRight'][0] &&
                this.lon > this.paacBounds['lowerLeft'][1] &&
                this.lon < this.paacBounds['upperRight'][1]) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    setFilters() {
        this.getFavRoutes();
        this.setInOut();
    }

    setInOut() {
        this.locationService.getLocation().then(
            user_loc  => {
                this.inoutService.getInOut(this.device_id, user_loc.latitude, user_loc.longitude).subscribe(
                    () => {
                        console.log("Setting inout: " + this.inoutService.inout);
                        if (this.inoutService.inout == 'in_filter') {
                            this.inFilter = true;
                            this.inFilterClass = 'inOutFilterOn-System';
                            this.outFilter = false;
                            this.outFilterClass = 'inOutFilterOff';
                        } else if (this.inoutService.inout == 'out_filter') {
                            this.outFilter = true;
                            this.outFilterClass = 'inOutFilterOn-System';
                            this.inFilter = false;
                            this.inFilterClass = 'inOutFilterOff';
                        } else {
                            this.inFilter = false;
                            this.inFilterClass = 'inOutFilterOff';
                            this.outFilter = false;
                            this.outFilterClass = 'inOutFilterOff';
                        }
                    });
            });
    }

    toggleInFilter() {
        this.inFilter = !this.inFilter;

        if(this.inFilter){
            this.inFilterClass = 'inOutFilterOn';
            this.logFilterBtn("in", "write");
        } else {
            this.inFilterClass = 'inOutFilterOff';
            this.logFilterBtn("in", "remove");
        }

        this.resetStopIcons();

        this.locationService.getLocation().then(
            user_loc => {
                this.logNumScheduleRows(user_loc.latitude, user_loc.longitude);
            }
        );
    }

    toggleOutFilter() {
        this.outFilter = !this.outFilter;

        if(this.outFilter){
            this.outFilterClass = 'inOutFilterOn';
            this.logFilterBtn("out", "write");
        } else {
            this.outFilterClass = 'inOutFilterOff';
            this.logFilterBtn("out", "remove");
        }

        this.resetStopIcons();

        this.locationService.getLocation().then(
            user_loc => {
                this.logNumScheduleRows(user_loc.latitude, user_loc.longitude);
            }
        );
    }

    jumpOverFilter() {
        console.log("jump over pressed");
        let firstSchedule = document.getElementsByClassName('title');
        this.voiceOverService.setVoiceOverFocus(firstSchedule[0]);
    }

    jumpBackFilter() {
        console.log("jump back pressed");
        let firstFilter = document.getElementsByClassName('inOutFilterOff');
        this.voiceOverService.setVoiceOverFocus(firstFilter[0]);
    }

    routeFilterClass(route: DisplayedRoute) {
        //console.log(route);
        if(route.selector == "system"){
            return "system-selected";
        } else {
            return "user-selected";
        }
    }

    selectRoute(route: DisplayedRoute) {
        //route.selected = !route.selected;

        //console.log(route)
        route.selector = "user";
        this.selectedRouteList.push(route);

        let index = this.nearbyRoutes.indexOf(route, 0);
        if (index > -1) {
            this.nearbyRoutes.splice(index, 1);
        }

        this.selectedRouteList.sort(this.orderRoutes);
        this.nearbyRoutes.sort(this.orderRoutes);

        this.locationService.getLocation().then(
            user_loc => {
                this.favRoutesService.writeFavRoute(this.device_id, user_loc.latitude, user_loc.longitude, route).subscribe(
                    writeSuccess => {
                        if(writeSuccess) {
                            console.log(route.routeId + " write success");
                        } else {
                            console.log(route.routeId + " write failure")
                        }
                    },
                    error => {
                        console.log('writeFavRoute failed with code ' + error.code + ' and message ' + error.message);
                    }
                );
                this.logNumScheduleRows(user_loc.latitude, user_loc.longitude);
            });

        this.resetStopIcons();
    }

    unSelectRoute(route: DisplayedRoute) {

        this.nearbyRoutes.push(route);
        let index = this.selectedRouteList.indexOf(route, 0);
        if (index > -1) {
            this.selectedRouteList.splice(index, 1)
        }

        this.selectedRouteList.sort(this.orderRoutes);
        this.nearbyRoutes.sort(this.orderRoutes);

        this.locationService.getLocation().then(
            user_loc => {
                this.favRoutesService.removeFavRoute(this.device_id, user_loc.latitude, user_loc.longitude, route).subscribe(
                    removeSuccess => {
                        if(removeSuccess) {
                            console.log(route.routeId + " remove success");
                        } else {
                            console.log(route.routeId + " remove failure")
                        }
                    },
                    error => {
                        console.log('removeFavRoute failed with code ' + error.code + ' and message ' + error.message);
                    }
                );
                this.logNumScheduleRows(user_loc.latitude, user_loc.longitude);
            });

        this.resetStopIcons();

    }

    selectStop(stopId: string) {
        console.log('select ' + stopId);
        if (this.map && this.markersMap[stopId]) {
            this.markersMap[stopId].setIcon(this.blue_starred_stop_icon);
        }

        let stop = this.nearbyStops.find(stop => stop.id == stopId);
        this.selectedStopSet[stopId] = stop;

        let schedules = this.stopIdIndexedSchedules[stopId];
        for (let schedule of schedules) {
            let insertIndex = this.topSchedules.findIndex(
                sch => this.orderTopSchedule(schedule, sch) < 0
            );
            // insert at correct position into top schedule list
            if (insertIndex >= 0) { // found a larger schedule
                this.topSchedules.splice(insertIndex, 0, schedule);
            } else { // this schedule is the largest
                this.topSchedules.push(schedule);
            }
        }

        this.locationService.getLocation().then(
            user_loc => {
                this.favStopsService.writeFavStop(this.device_id, user_loc.latitude, user_loc.longitude, stop).subscribe(
                    writeSuccess => {
                        if(writeSuccess) {
                            console.log(stopId + " write success");
                        } else {
                            console.log(stopId + " write failure")
                        }
                    },
                    error => {
                        console.log('writeFavStop failed with code ' + error.code + ' and message ' + error.message);
                    }
                );
                this.logNumScheduleRows(user_loc.latitude, user_loc.longitude);

            });
    }

    unselectStop(stopId: string) {
        console.log('unselect ' + stopId);
        if (this.map && this.markersMap[stopId]) {
            this.markersMap[stopId].setIcon(this.blue_stop_icon);
        }

        let stop = this.selectedStopSet[stopId];
        delete this.selectedStopSet[stopId];

        // remove schedules with stopId from top schedule list
        this.topSchedules = this.topSchedules.filter(
            schedule => (schedule.stopId != stopId)
        );

        this.locationService.getLocation().then(
            user_loc => {
                this.favStopsService.removeFavStop(this.device_id, user_loc.latitude, user_loc.longitude, stop).subscribe(
                    removeSuccess => {
                        if(removeSuccess) {
                            console.log(stopId + " remove success");
                        } else {
                            console.log(stopId + " remove failure")
                        }
                    },
                    error => {
                        console.log('removeFavStop failed with code ' + error.code + ' and message ' + error.message);
                    }
                );
                this.logNumScheduleRows(user_loc.latitude, user_loc.longitude);

            });
    }

    read_Settings(){
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
        this.checkMap();
    }

    checkMap() {
        if (this.settingsDic["map_on_start"]) {
            this.toggleMap();
            this.logToggleMapBtn();
        }
    }
    
    shouldDisplay(routeId: string, directionId: number) {
        return (
            //show all routes if none is selected
            ((this.favRoutesService.selectedRouteSet[routeId] ||  (this.dataUtilityService.isObjectEmpty(this.favRoutesService.selectedRouteSet)) || !this.hasFilterRoutes())
             && this.directionMatchFilter(directionId)))
    }

    directionMatchFilter(directionId: number) {
        if (this.inPaac()) {
            //show both directions if neither is selected
            return (!this.outFilter && !this.inFilter) ||
                (directionId == 0 && this.outFilter) || (directionId == 1 && this.inFilter);
        } else {
            return true;
        }
    }

    showDirection(directionId: number) {
        if (!this.inPaac()) {
            return '';
        } else {
            if (directionId == 0) {
                return 'OUT: ';
            } else {
                return 'IN: ';
            }
        }
    }

    // pushes a navigator page (destinations)
    showDests(schedule: ScheduleEntry) {
        this.nav.push(RouteSchedulePage, {schedule: schedule,
                                          config: this.config},
                      this.nav_opts);
        //this.nav.push(RoutePage, schedule, this.nav_opts);
    }

    showSettings()
    {
        this.nav.push(SettingsPage,{},this.nav_opts);
    }

    showStopPage(schedule: ScheduleEntry){
        let stopId = schedule.stopId;
        let selectedstop = null;
        for (let stop of this.nearbyStops) {
            if (stop.id == stopId){
                selectedstop = stop;
            }
        }
        this.nav.push(StopPage, {stop: selectedstop, schedules : this.stopIdIndexedSchedules[stopId],
                                 isFav : stopId in this.selectedStopSet}, this.nav_opts);
    }

    keys(object) {
        return Object.keys(object);
    }
}
