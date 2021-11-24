import { Injectable } from '@angular/core';

import {Stop} from '../model/stop';

@Injectable()

export class DistanceService {

    //calculates the distance between 2 locations

    distance(lat1: number, lat2: number, lon1: number, lon2: number) {
        function toRadians(x) {
            return x * Math.PI / 180;
        }
        let R = 6371e3; // metres
        let phi1 = toRadians(lat1);
        let phi2 = toRadians(lat2);
        let delta_phi = toRadians(lat2-lat1);
        let delta_lambda = toRadians(lon2-lon1);

        let a = Math.sin(delta_phi/2) * Math.sin(delta_phi/2) +
                Math.cos(phi1) * Math.cos(phi2) *
                Math.sin(delta_lambda/2) * Math.sin(delta_lambda/2);
        let c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return R * c;
    }

    //compares which stop is closer to the current location

    compare(s1: Stop, s2: Stop, lat: number, lon: number) {
        return this.distance(s1.lat,lat,s1.lon,lon)-this.distance(s2.lat,lat,s2.lon,lon);
    }
}