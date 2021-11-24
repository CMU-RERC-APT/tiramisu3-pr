import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { UrlGenerator } from './url-generator.service';
import { StringUtilityService } from './string-utility.service';

import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators/map';

import { JsonResponse } from '../model/json-response';


import { ReadSettingsService } from './read-settings.service';
import { WriteSettingsService } from './write-settings.service';
import { RemoveSettingsService } from './remove-settings.service';

@Injectable()
export class HandleSettingsService {

    constructor(private readSettingsService: ReadSettingsService,
                private writeSettingsService: WriteSettingsService,
                private removeSettingsService: RemoveSettingsService) {}

    writeSettings(device_id: string, user_lat: number, user_lon: number, item: string) {
        return this.writeSettingsService.writeSettings(device_id, user_lat, user_lon, item);
    }

    removeSettings(device_id: string, user_lat: number, user_lon: number, item: string) {
        return this.removeSettingsService.removeSettings(device_id, user_lat, user_lon, item);
    }

    readSettings(device_id: string, user_lat: number, user_lon: number) {
        return this.readSettingsService.readSettings(device_id, user_lat, user_lon);  
    }
}