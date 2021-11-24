/*import { LocationService } from './location.service';
import { Observable } from "rxjs/Rx";

describe('location service test', () => {
    var lat, lon;
    var locationService = new LocationService();
    var locationResult = locationService.getLocation();

    // can only test in browser
    
    //beforeAll(done => locationResult.subscribe(
    //    resp => {
    //        lat = resp.coords.latitude;
    //        lon = resp.coords.longitude;
    //        done();
    //    }
    //), 10000);
    

    it('getLocation() returns an Observable', () =>
        expect(locationResult).toEqual(jasmine.any(Observable)));

    xit('Observable gives two numbers', () => {
        expect(lat).toEqual(jasmine.any(Number));
        expect(lon).toEqual(jasmine.any(Number));
    });
});*/
