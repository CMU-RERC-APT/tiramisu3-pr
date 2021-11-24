import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { BrowserModule } from '@angular/platform-browser';
import { IonicApp, IonicModule } from 'ionic-angular';
import { IonicStorageModule } from '@ionic/storage';
import { HttpModule } from '@angular/http' ;
import { Network } from '@ionic-native/network';
import { MobileAccessibility } from '@ionic-native/mobile-accessibility';
import { ScreenOrientation } from '@ionic-native/screen-orientation'
//import { BackgroundMode } from '@ionic-native/background-mode';
import { Geolocation } from '@ionic-native/geolocation';
import { SplashScreen } from '@ionic-native/splash-screen';

import { MyApp } from './app.component';
import { AlarmService } from '../providers/alarm.service';
import { FavStopsService } from '../providers/fav-stops.service';
import { HandleSettingsService } from '../providers/handle-settings.service';
import { GetAlarmsService } from '../providers/get-alarms.service';
import { InOutService } from '../providers/inout.service';
import { LocationService } from '../providers/location.service';
import { ReadFavRoutesService } from '../providers/read-fav-routes.service';
import { ReadFavStopsService } from '../providers/read-fav-stops.service';
import { ReadSearchHistoryService } from '../providers/read-search-history.service';
import { ReadSettingsService } from '../providers/read-settings.service';
import { RemoveAlarmService } from '../providers/remove-alarm.service';
import { RemoveFavRouteService } from '../providers/remove-fav-route.service';
import { RemoveFavStopService } from '../providers/remove-fav-stop.service';
import { RemoveSettingsService } from '../providers/remove-settings.service';
import { SetAlarmService } from '../providers/set-alarm.service';
import { StatusLogService } from '../providers/status-log.service';
import { StoreLocationSQLService } from '../providers/store-location-sql.service';
import { StringUtilityService } from '../providers/string-utility.service';
import { UrlGenerator } from '../providers/url-generator.service';
import { WriteFavRouteService } from '../providers/write-fav-route.service';
import { WriteFavStopService } from '../providers/write-fav-stop.service';
import { WriteSearchHistoryService } from '../providers/write-search-history.service';
import { WriteSettingsService } from '../providers/write-settings.service';
import { FocusLogService} from '../providers/focus-logging.service';
import { ButtonLogService} from '../providers/button-logging.service';

import { ConsentPage } from '../pages/consent-page/consent-page';
import { HomePage } from '../pages/home-page/home-page';
import { RoutePage } from '../pages/route-page/route-page';
import { RouteSchedulePage } from '../pages/route-schedule-page';
import { SchedulePage } from '../pages/schedule-page/schedule-page';
import { StopPage } from '../pages/stop-page/stop-page';
import { SettingsPage } from '../pages/set-page/set-page';

@NgModule({
    declarations: [
        MyApp,
        ConsentPage,
        HomePage,
        RoutePage,
        RouteSchedulePage,
        SchedulePage,
        StopPage,
        SettingsPage
    ],
    imports: [
        BrowserModule,
        HttpClientModule,
        IonicModule.forRoot(MyApp, {
            useDevUrl: true,
            useLocalTiramisu: false,
            backendUrl: 'https://www.riderinfo.org',
            //These urls are for development/testing
            backendUrlDev: 'http://onebusaway-test.us-east-1.elasticbeanstalk.com/',
            // for browser
            proxyUrlDev: '/backendDev',
            localBackendUrl: '/localbackend',
            obaPath: 'oba/api/where',
            tiramisuPath: 'tiramisu',
            obaApiKey: 'TEST',
            schedulesRefreshInterval: 30000, // 30 seconds
            backButtonText: '',
            prodMode: true, // for angular
            sender_id: "723232743147",
            animate: true,
            // need to change
            paacBounds: {'lowerLeft': [40.25, -80.30], 'upperRight': [40.62, -79.65]},
            defaultLat: 40.443460,
            defaultLon: -79.943451
        }),
        IonicStorageModule.forRoot()
    ],
    bootstrap: [IonicApp],
    entryComponents: [
        MyApp,
        ConsentPage,
        HomePage,
        RoutePage,
        RouteSchedulePage,
        SchedulePage,
        StopPage,
        SettingsPage
    ],
    providers: [AlarmService,
                //BackgroundMode,
                ButtonLogService,
                FavStopsService,
                HandleSettingsService,
                FocusLogService,
                Geolocation,
                GetAlarmsService,
                InOutService,
                Network,
                LocationService,
                ReadFavRoutesService,
                ReadFavStopsService,
                ReadSettingsService,
                ReadSearchHistoryService,
                RemoveAlarmService,
                RemoveFavRouteService,
                RemoveFavStopService,
                RemoveSettingsService,
                SetAlarmService,
                SplashScreen,
                StatusLogService,
                StoreLocationSQLService,
                StringUtilityService,
                UrlGenerator,
                WriteSettingsService,
                WriteFavRouteService,
                WriteFavStopService,
                WriteSearchHistoryService,
                MobileAccessibility,
                ScreenOrientation]
})
export class AppModule {}
