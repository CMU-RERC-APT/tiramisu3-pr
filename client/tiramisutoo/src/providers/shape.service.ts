import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';

import {UrlGenerator} from './url-generator.service';

@Injectable()
export class ShapeService {
    private servletPath: string = "shape";

    constructor(private http: HttpClient, private urlGenerator: UrlGenerator) {
    }

    getShape(shapeId: string) {
        let fullServletPath: string = `${this.servletPath}/${shapeId}.json`;

        let shapeURL = this.urlGenerator.generateOBAUrl(fullServletPath, {});

        return this.http.get(shapeURL).pipe(map(res => this.extractShape(res)));
    }

    extractShape(res) {
        //let body = res.json();
        let points = res.data.entry.points;
        return points;
    }
}
