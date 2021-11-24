import { Injectable } from '@angular/core';
import { Platform, Config } from 'ionic-angular';

@Injectable()
export class UrlGenerator {

    private obaHost: string;
    private obaPath: string;
    private obaApiKey: string;

    private tiramisuHost: string;
    private tiramisuPath: string;

    constructor(private config: Config,
                private platform: Platform) {

        let useDevUrl: boolean = config.get("useDevUrl");
        let useLocalTiramisu: boolean = config.get("useLocalTiramisu");
        this.obaPath = config.get("obaPath");
        this.obaApiKey = config.get("obaApiKey");
        this.tiramisuPath = config.get("tiramisuPath");

        if(this.platform.is('core') || this.platform.is('android') || this.platform.is('ios')) {    
            this.obaHost = config.get("proxyUrlDev");

            if (useLocalTiramisu) {            
                this.tiramisuHost = config.get("localBackendUrl");
            } else {            
                this.tiramisuHost = config.get("proxyUrlDev");
            }

        } else {            
            if (useDevUrl) {            
                this.obaHost = config.get("backendUrlDev");
                this.tiramisuHost = config.get("backendUrlDev");
                
            } else {            
                this.obaHost = config.get("backendUrl");
                this.tiramisuHost = config.get("backendUrl");
            }
        }        
    }

    addParams(baseUrl: string, urlParams: { [key: string]: any; }): string {

        let url = `${baseUrl}?`

        for (let paramName in urlParams) {
            url += `${paramName}=${urlParams[paramName]}&`;
        }

        // slice is to remove the last '&'
        return url.slice(0, -1);

    }

    /*replaceUnsafeCharacters(url: string) {

	url.replace(" ", "%20");
	return url;
    }*/

    generateOBAUrl(servletPath: string, urlParams: { [key: string]: any; }): string {

        let baseUrl: string = `${this.obaHost}/${this.obaPath}/${servletPath}`;
        
        urlParams['key'] = this.obaApiKey;
        let url: string = this.addParams(baseUrl, urlParams);
	//url = replaceUnsafeCharacters(url);
        return url;
    }

    generateTiramisuUrl(servletPath: string, urlParams: { [key: string]: any; }): string {

        let baseUrl: string = `${this.tiramisuHost}/${this.tiramisuPath}/${servletPath}`;
        let url: string = this.addParams(baseUrl, urlParams);

	//url = replaceUnsafeCharacters(url);
        return url;
    }
}
