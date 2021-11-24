import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable'
import { filter } from 'rxjs/operators/filter';

import { Geolocation, Geoposition, PositionError } from '@ionic-native/geolocation';

import { Config, Platform } from 'ionic-angular';
import { StoreLocationSQLService } from './store-location-sql.service';


@Injectable()
export class LocationService {

    constructor(private storeLocationSQLService: StoreLocationSQLService,
                private config: Config,
                private geolocation: Geolocation,
                private platform: Platform) {

        //This is here to trigger the request for location permissions
        this.platform.ready().then(() => {
            this.updateCache();
        });
    }

    startWatch() {
        this.geolocation.watchPosition({enableHighAccuracy: true}).pipe(filter((p) => p.coords !== undefined))
            .subscribe(
                position => {
                    console.log("watch position found: " + position.coords.longitude + ' ' + position.coords.latitude); 
                    this.storeLocationSQLService.writeLocation(position.coords.latitude,
                                                               position.coords.longitude);
                    //}
                },
                error => {
                    console.log("error getting position from watchPosition()");
                    this.updateCache()
                });
    }
    

    updateCache() {
        //console.log('getting last saved location');
        return this.geolocation.getCurrentPosition({enableHighAccuracy: true, timeout: 2000, maximumAge: 120000}).then(
            position => {
                this.storeLocationSQLService.writeLocation(position.coords.latitude,
                                                           position.coords.longitude);
                let result = {"latitude": position.coords.latitude,
                              "longitude": position.coords.longitude};
                return result;
                
            }).catch((error) => {
                console.log("Error getting non-cached location", error);
            });
    }

    getLocation() {
        return this.storeLocationSQLService.readLastSavedLocation().then(lastLocation =>{
            if (lastLocation){
                //console.log("lastLocation =", lastLocation);
                let result = {"latitude" : lastLocation[0].lat,
                              "longitude" : lastLocation[0].lon};
                return result;
            }
            else{
                //console.log("this.config.get(defaultLat) =", this.config.get("defaultLat"));
                let result = {"latitude" : this.config.get("defaultLat"),
                              "longitude" : this.config.get("defaultLon")};
                return result;
            }
        });
        
    }

        /* The followingis the old way of calling getCurrentPosition everytime getLocation is called*/
        // console.log("getting location");
        // let options = {timeout : 10000, enableHighAccuracy: true};
        //   return Geolocation.getCurrentPosition(options).then(data => {
        //     console.log("recevied locaiton from Geolocation");
        //     return {"latitude" : data.coords.latitude, "longtitude": data.coords.longitude};
        //   }, (error)=>{
        //     console.log('getting last saved location');
        //     return this.storeLocationSQLService.readLastSavedLocation().then(lastLocation =>{
        //       if (lastLocation){
        //           console.log("lastLocation =", lastLocation);
        //           let result = {"latitude" : lastLocation[0].lat,
        //                 "longtitude" : lastLocation[0].lon};
        //           return result;
        //         }
        //         else{
        //           console.log("this.config.get(defaultLat) =", this.config.get("defaultLat"));
        //           let result = {
        //           "latitude" : this.config.get("defaultLat"),
        //           "longtitude" : this.config.get("defaultLon")
        //           };
        //         return result;
        //       }
        //   });
        // });


}
