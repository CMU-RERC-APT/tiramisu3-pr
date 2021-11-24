import { Component } from '@angular/core';
import { Platform, Config, AlertController, NavController, App, Events } from 'ionic-angular';

//import { BackgroundMode } from '@ionic-native/background-mode';
import { AppVersion } from '@ionic-native/app-version';
import { StatusBar } from '@ionic-native/status-bar';
import { Device } from '@ionic-native/device';
import { Diagnostic } from '@ionic-native/diagnostic';
import { Push, PushObject } from '@ionic-native/push';
import { LocalNotifications } from '@ionic-native/local-notifications';
import { MobileAccessibility } from '@ionic-native/mobile-accessibility';
import { Network } from '@ionic-native/network';
import { SplashScreen } from '@ionic-native/splash-screen';
import { ScreenOrientation } from '@ionic-native/screen-orientation';

import { RouteSchedulePage } from '../pages/route-schedule-page';
import { HomePage } from '../pages/home-page/home-page';
import { RoutePage } from '../pages/route-page/route-page';
import { SchedulePage } from '../pages/schedule-page/schedule-page';
import { StopPage } from '../pages/stop-page/stop-page';
import { ConsentPage } from '../pages/consent-page/consent-page';

import {AlarmService} from '../providers/alarm.service';
import {DistanceService} from '../providers/distance.service';
import {LocationService} from '../providers/location.service';
import {FavRoutesService} from '../providers/fav-routes.service';
import {InOutService} from '../providers/inout.service';
import {FocusLogService} from '../providers/focus-logging.service';
import {ButtonLogService} from '../providers/button-logging.service';
import { StatusLogService } from '../providers/status-log.service';

@Component({
    template: `<ion-nav [root]="rootPage"></ion-nav>`,
    providers: [AppVersion, Device, Diagnostic, StatusBar, Push, LocalNotifications, FavRoutesService, InOutService, DistanceService]
})
export class MyApp {
    rootPage = HomePage;

    private registration_id: string;
    private device_id: string;
    private device_platform: string;
    private connectionTimer;
    private lastSeenTime: number;
    private lastSeenLoc: {[key: string]: number};

    private navCtrl: NavController;

    constructor(private platform: Platform,
                //private backgroundMode: BackgroundMode,
                private config: Config,
                private app: App,
                private appVersion: AppVersion,
                private device: Device,
                private diagnostic: Diagnostic,
                public events: Events,
                private alarmService: AlarmService,
                private distanceService: DistanceService,
                private alertCtrl: AlertController,
                private statusBar: StatusBar,
                private network: Network,
                private push: Push,
                private localNotification: LocalNotifications,
                private mobileAccessibility: MobileAccessibility,
                private splashScreen: SplashScreen,
                private locationService: LocationService,
                private favRoutesService: FavRoutesService,
                private inoutService: InOutService,
                private focusLogService: FocusLogService,
                private buttonLogService: ButtonLogService,
                public screenOrientation: ScreenOrientation,
                private statusLogService: StatusLogService) {

        this.platform.ready().then(() => {
            this.splashScreen.hide();
            this.navCtrl = this.app.getActiveNavs()[0];
            statusBar.styleDefault();
            this.config.set('ios', 'device_platform', 'ios');
            this.config.set('android', 'device_platform', 'android');
            this.screenOrientation.lock('portrait-primary');

            this.device_id = this.device.uuid;
            console.log("device_id " + this.device_id);
            this.config.set('device_id', this.device_id);
            this.config.set('is_ios', this.platform.is('ios') && this.platform.is('cordova'));

            /*this.backgroundMode.enable();
              this.backgroundMode.on('activate').subscribe( () => {
              console.log("background mode activated");
              });
              this.backgroundMode.on('deactivate').subscribe( () => {
              console.log("background mode deactivated");
              });*/

            if(!this.platform.is('core')) {
                setTimeout(() => {
                    this.mobileAccessibility.isScreenReaderRunning().then(
                        screenReaderOn => {
                            this.diagnostic.isLocationAvailable().then(
                                locationAvailable => {
                                    this.appVersion.getVersionNumber().then(
                                        version => {
                                            this.statusLogService.logStatus(this.device_id, this.device_platform, version, screenReaderOn, locationAvailable);
                                        });
                                }); 
                        });
                }, 5000);
            }

            this.locationService.getLocation().then(
                user_loc => {
                    this.lastSeenLoc = user_loc;
                    let optionalArgs = {};
                    this.focusLogService.logFocus(this.device_id,
                                                  this.device_platform, user_loc.latitude,
                                                  user_loc.longitude, "start", optionalArgs);
                }
            );

            this.platform.resume.subscribe(() => {
                console.log("app resumed");
                this.getAlarms();
                let optionalArgs = {};
                this.locationService.getLocation().then(
                    user_loc => {
                        if(((Date.now() - this.lastSeenTime) > (60 * 60 * 1000)) ||
                           (this.distanceService.distance(this.lastSeenLoc.latitude, user_loc.latitude, this.lastSeenLoc.longitude, user_loc.longitude) > 850)) {
                            //this.favRoutesService.removeAllRoutes(this.device_id, user_loc.latitude, user_loc.longitude);
                            this.favRoutesService.getFavRoutes(this.device_id, user_loc.latitude, user_loc.longitude).subscribe(() => {});
                            this.inoutService.getInOut(this.device_id, user_loc.latitude, user_loc.longitude).subscribe(() => {});
                            this.navCtrl.setRoot(this.navCtrl.getActive().component); //refresh page content
                            //for getting new inout from server
                            this.events.publish('user:data-refresh');
                        }
                        this.focusLogService.logFocus(this.device_id,
                                                      this.device_platform, user_loc.latitude,
                                                      user_loc.longitude, "resume", optionalArgs);
                    }
                );
            });

            //This is a hack for ios because it doesn't wait for the pause event code to finish, so some code from the pause event only gets run upon resume
            if(this.config.get('is_ios')) {
                setInterval(() => {
                    this.locationService.getLocation().then(
                        user_loc => {
                            this.lastSeenLoc = user_loc;
                        }
                    ); 
                }, 5000);
            }

            this.platform.pause.subscribe(() => {
                console.log("app paused");
                this.lastSeenTime = Date.now()
                let optionalArgs = {"stamp": this.lastSeenTime};
                this.locationService.getLocation().then(
                    user_loc => {
                        if(!this.config.get('is_ios')) {
                            this.lastSeenLoc = user_loc;
                        }
                        this.focusLogService.logFocus(this.device_id,
                                                      this.device_platform, this.lastSeenLoc.latitude,
                                                      this.lastSeenLoc.longitude, "pause", optionalArgs);
                    }
                );
            });

            // initialise the alarms
            this.device_platform = this.config.get('device_platform');
            this.registerPushNotification();

            this.network.onDisconnect().subscribe(() => {
                this.connectionTimer = setTimeout(() => {
                    let alert = this.alertCtrl.create({
                        title: "No internet connection",
                        subTitle: "Unable to get bus information",
                        buttons: ['Dismiss']
                    });
                    alert.present();
                }, 5000);
            });

            this.network.onConnect().subscribe(() => {
                clearTimeout(this.connectionTimer);
            });

            this.localNotification.on('trigger').subscribe( (notification) => {
                //console.log("local notification triggered")
                //console.log(notification);
                this.showAlertAndroid(notification);
            });
            this.localNotification.on('cancel').subscribe( () => {
                console.log("local notification canceled");
            });
        });
    }

    showAlertiOS(alarmInfo, alarmKey, text) {
        console.log(alarmKey)
        let alert = this.alertCtrl.create({
            title: "Bus Arriving Soon",
            subTitle: text,
            buttons: [
                {
                    text: 'Dismiss',
                    handler: () => {
                        this.locationService.getLocation().then(
                            user_loc => {
                                let optionalArgs = {};
                                optionalArgs["trip_id"] = alarmInfo.trip_id;
                                optionalArgs["stop_id"] = alarmInfo.stop_id;
                                this.buttonLogService.logButton(this.device_id, this.device_platform, user_loc.latitude,
                                                                user_loc.longitude, this.getCurrentPage(), "alert_dismiss", optionalArgs);
                            });
                    }
                }
            ]
        });
        alert.present();
        this.alarmService.alarmSet[alarmKey] = false;
        //this.getAlarms();
    }

    showAlertAndroid(notification) {
        //let alarmKey = notification.data;
        //let alarmInfo = JSON.parse(alarmKey);
        let alarmInfo = notification.data;
        console.log(alarmInfo);
        let alert = this.alertCtrl.create({
            title: "Bus Arriving Soon",
            subTitle: notification.text,
            buttons: [
                {
                    text: 'Dismiss',
                    handler: () => {
                        this.locationService.getLocation().then(
                            user_loc => {
                                let optionalArgs = {};
                                optionalArgs["trip_id"] = alarmInfo.trip_id;
                                optionalArgs["stop_id"] = alarmInfo.stop_id;
                                this.buttonLogService.logButton(this.device_id, this.device_platform, user_loc.latitude,
                                                                user_loc.longitude, this.getCurrentPage(), "alert_dismiss", optionalArgs);
                            });
                    }
                }
            ]
        });
        alert.present();
        this.alarmService.alarmSet[alarmInfo.alarmKey] = false;
    }

    registerPushNotification() {
        console.log("called registerPushNotification");
        if (!("PushNotification" in window)) {return;}
        const pushObject: PushObject = this.push.init({
            android: {
                senderID: this.config.get('sender_id')
            },
            ios: {
                alert: true,
                badge: true,
                sound: true
            },
            windows: {}
        });

        pushObject.on('registration').subscribe((data: any) => {
            console.log("registered push notifications");
            console.log(data);
            this.config.set('registration_id', data.registrationId);
            this.registration_id = data.registrationId;
            this.getAlarms();
            if(!this.localNotification.hasPermission()) {
                this.localNotification.requestPermission();
            }
        });

        pushObject.on('notification').subscribe((data: any) => {
            console.log("Notification received!");
            console.log(data);
            let alarm_info = null;
            try{
                let additional_data: any = data.additionalData
                alarm_info = JSON.parse(additional_data.alarmInfo);
            }catch(err) {
                let additional_data: any = data.additionalData
                alarm_info = additional_data.alarmInfo;
            };
            let route_name = alarm_info.route_name;
            let trip_headsign = alarm_info.trip_headsign;
            let stop_name = alarm_info.stop_name;
            let trip_id = alarm_info.trip_id;
            let service_date = alarm_info.service_date;
            let stop_id = alarm_info.stop_id;
            let alarmKey = trip_id + service_date + stop_id;
            let text = `${route_name} (${trip_headsign}) is now approaching ${stop_name}`;
            if (this.config.get('is_ios')) {
                this.showAlertiOS(alarm_info, alarmKey, text);
            } else {
                this.localNotification.schedule({
                    text: text,
                    data: {alarmKey: alarmKey,
                           trip_id: trip_id,
                           service_date: service_date,
                           stop_id: stop_id},
                });
            }
        });

        pushObject.on('error').subscribe( (e) => {
            console.log("error occured");
            console.log(e);
        });
    }

    getAlarms() {
        console.log("getting alarms");
        // temporary user_id
        this.alarmService.getAlarms(this.registration_id, this.device_id, this.device_platform, 'tiramisu');
    }

    getCurrentPage() {
        let view = this.navCtrl.getActive();
        // if (view.instance instanceof HomePage) {
        //    return "home_page";
        // }
        // if (view.instance instanceof RoutePage) {
        //    return "route_page";
        // }
        if (view.instance instanceof RouteSchedulePage) {
            return "route_schedule_page";
        }
        if (view.instance instanceof SchedulePage) {
            return "schedule_page";
        }
        if (view.instance instanceof StopPage) {
            return "stop_page";
        }
        if (view.instance instanceof ConsentPage) {
            return "consent_page";
        }
        // can't be here
        console.log("error occurred");
        return "";
    }


}
