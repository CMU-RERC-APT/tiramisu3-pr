import { Injectable } from '@angular/core';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';

import { DisplayedRoute } from '../model/displayed-route';

import { ReadFavRoutesService } from './read-fav-routes.service';
import { WriteFavRouteService } from './write-fav-route.service';
import { RemoveFavRouteService } from './remove-fav-route.service';

@Injectable()
export class FavRoutesService {

    public selectedRouteSet: { [key: string]: DisplayedRoute } = {};

    constructor(private readFavRoutesService: ReadFavRoutesService,
		private writeFavRouteService: WriteFavRouteService,
	        private removeFavRouteService: RemoveFavRouteService) {}

    getFavRoutes(device_id: string, user_lat: number, user_lon: number) {

	return this.readFavRoutesService.getFavRoutes(device_id, user_lat, user_lon).pipe(map(
            routes => {
                //console.log(routes);
                this.selectedRouteSet = routes;
                return;
            }
        ));
    }

    writeFavRoute(device_id: string, user_lat: number, user_lon: number, route: DisplayedRoute) {

        let routeId = route.routeId;
        this.selectedRouteSet[routeId] = route;

	return this.writeFavRouteService.writeFavRoute(device_id, user_lat, user_lon, route);
    }

    removeFavRoute(device_id: string, user_lat: number, user_lon: number, route: DisplayedRoute, event: string = 'remove') {

        let routeId = route.routeId;
        delete this.selectedRouteSet[routeId];

	return this.removeFavRouteService.removeFavRoute(device_id, user_lat, user_lon, route, event);
    }

    removeAllRoutes(device_id: string, user_lat: number, user_lon: number, event: string = 'autoremove') {

        this.selectedRouteSet = {};

        this.readFavRoutesService.getFavRoutes(device_id, user_lat, user_lon).subscribe(
            routes => {
                for(let key in routes) {
                    let route = routes[key];
                    this.removeFavRoute(device_id, user_lat, user_lon, route, event).subscribe(
                        removeSuccess => {
                            if(removeSuccess) {
                                console.log(route.routeId + " autoremove success");
                            } else {
                                console.log(route.routeId + " autoremove failure");
                            }
                        },
                        error => {
                            console.log('removeFavRoute failed with code ' + error.code + ' and message' + error.message);
                        }
                    );
                }

            },
            error => {
                console.log('getFavRoutes failed with code ' + error.code + ' and message ' + error.message);
            }
        );
    }
    
}
