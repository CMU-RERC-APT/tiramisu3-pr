import { Injectable } from '@angular/core';

import { Observable } from 'rxjs/Observable';

import { ReadFavStopsService } from './read-fav-stops.service';
import { WriteFavStopService } from './write-fav-stop.service';
import { RemoveFavStopService } from './remove-fav-stop.service';

import { Stop } from '../model/stop';

@Injectable()
export class FavStopsService {

    public toggledInStopPage: boolean = false;
    public toggledStopId: string = null;

    constructor(private readFavStopsService: ReadFavStopsService,
                private writeFavStopService: WriteFavStopService,
                private removeFavStopService: RemoveFavStopService) {}

    getFavStops(device_id: string, user_lat: number, user_lon: number) {

        return this.readFavStopsService.getFavStops(device_id, user_lat, user_lon);
    }

    writeFavStop(device_id: string, user_lat: number, user_lon: number, stop: Stop) {

        return this.writeFavStopService.writeFavStop(device_id, user_lat, user_lon, stop);
    }

    removeFavStop(device_id: string, user_lat: number, user_lon: number, stop: Stop) {

        return this.removeFavStopService.removeFavStop(device_id, user_lat, user_lon, stop);
    }

    enterStopPage(stopId: string) {
        this.toggledInStopPage = false;
        this.toggledStopId = stopId;
    }

    toggleInStopPage() {
        this.toggledInStopPage = !this.toggledInStopPage;
    }
    
}
