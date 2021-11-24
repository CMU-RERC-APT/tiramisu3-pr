import { Injectable } from '@angular/core';

@Injectable()
export class StringUtilityService {

    toTitleCase(str: string) {
        return str.replace(/\b\w+/g, txt =>
			   txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase()
		          );
    }
    
    directionStringTrim(s: string) {
        if (s.startsWith('Inbound-') || s.startsWith('Outbound-')) {
	    return s.substring(s.indexOf('-')+1);
        } else {
            return s;
        }
    }

    replacePoundSign(s: string) {
        return s.replace('#', '%23');
    }

    orderRouteNames(rt1Name: string, rt2Name: string) {
        
        let index = 0;
        while (index < rt1Name.length && index < rt2Name.length) {
            let int1 = parseInt(rt1Name.substring(index));
            let int2 = parseInt(rt2Name.substring(index));
            
            if(isNaN(int1) && isNaN(int2)) {
                let char1 = rt1Name.charAt(index);
                let char2 = rt2Name.charAt(index);

                if(char1 < char2) {
                    return -1;
                } else if(char1 > char2) {
                    return 1;
                } else {
                    index++;
                    continue;
                }
            } else if(isNaN(int2)) {
                return -1;
            } else if(isNaN(int1)) {
                return 1;

            } else {
                if(int1 < int2) {
                    return -1;
                } else if (int1 > int2) {
                    return 1;

                } else {
                    let intLen = int1.toString().length;
                    index += intLen;
                    continue;
                }
            }
        }

        // Both strings have been the same up to the end of one of the strings
        if(rt1Name.length < rt2Name.length) {
            return -1;
        } else if (rt1Name.length > rt2Name.length) {
            return 1;
        } else {
            return 0;
        }
    }
}
