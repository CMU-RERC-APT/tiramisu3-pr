import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';

import { UrlGenerator } from './url-generator.service';

import { JsonResponse } from '../model/json-response';

@Injectable()
export class InOutService {
    
    public inout: string = '';
    private servletPath: string = "ReadInOutServlet";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator) {
    }

    getInOut(device_id: string, user_lat: number, user_lon: number) {
        let args = {'device_id': device_id,
                    'user_lat': user_lat,
                    'user_lon': user_lon};

        let readInOutUrl = this.urlGenerator.generateTiramisuUrl(this.servletPath, args);

        return this.http.get(readInOutUrl).pipe(map(
            (res: JsonResponse) => {
                this.inout = res.data[0];
            }));
    }
}
