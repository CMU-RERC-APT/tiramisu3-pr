/*import {
    beforeEach,
    beforeEachProviders,
    describe,
    expect,
    it,
    inject,
    injectAsync
} from '@angular/core/testing';
import {NearbyRoutesService} from './nearby-routes.service';
import {DisplayedRoute} from '../model/displayed-route';
import { Http, HTTP_PROVIDERS } from '@angular/http';
import {UrlGenerator} from './url-generator.service';
import {Observable} from "rxjs/Rx";

describe('nearby-routes service test', () => {
    var lat =  40.4432957;
    var lon = -79.9451408;
    //var http: Http;
    //var urlGenerator: UrlGenerator;
    var nearbyRoutesResult;
    var nearbyRoutesService: NearbyRoutesService;
    
    beforeEachProviders(() => [NearbyRoutesService, HTTP_PROVIDERS, UrlGenerator]);

    beforeEach(inject([NearbyRoutesService], (n: NearbyRoutesService) => {
        nearbyRoutesService = n;
        nearbyRoutesResult = nearbyRoutesService.getNearbyRoutes(lat, lon);
    }));

    it('getNearbyRoutes() returns an Observable', () => {
        expect(nearbyRoutesResult).toEqual(jasmine.any(Observable))
    });

    it('Observable gives an array of DisplayRoute', done => {
        nearbyRoutesResult.subscribe(
            resp => {
                expect(resp).toEqual(jasmine.any(Array));
            }
        )
    });
});*/
