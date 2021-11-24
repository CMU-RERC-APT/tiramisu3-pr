import { Injectable } from '@angular/core';
import { Storage } from '@ionic/storage';
//import { Observable } from 'rxjs/Observable';

@Injectable()
export class StoreLocationSQLService {

    constructor(private storage: Storage){
    }

    writeLocation(lat, lon){
        this.storage.set('last_location', JSON.stringify([{'lat': lat, 'lon': lon}]));
        //console.log("location writen");
        //console.log("last_location:", this.storage.get('last_location'));
    }

    readLastSavedLocation(){
        return this.storage.get('last_location').then(value =>{
            if (value){
                //console.log("Load last saved location:", value);
                return JSON.parse(value);
            }
            else{
                console.log("No last saved location found");
                return null;
            }
        });
    }



}
