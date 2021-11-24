import { Injectable } from '@angular/core';

@Injectable()
export class DataUtilityService {

    //Check whether an object(mapping) is empty
    isObjectEmpty(obj: { [key : string] : any;}){
        if(!obj){
            return true;
        }
        for(let key in obj){
            return false;
        }
        return true;
    }
}
