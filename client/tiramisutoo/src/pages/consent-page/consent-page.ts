import { ViewController, AlertController, Platform} from 'ionic-angular';
import { Storage } from '@ionic/storage';
import { Component } from '@angular/core';


@Component({
    templateUrl: 'consent-page.html',
    selector: 'page-consent',

})

export class ConsentPage {

    constructor(private viewCtrl : ViewController, private alertCtrl: AlertController, private platform: Platform, private database: Storage) {
    }

    ngOnInit() {
        /*this.platform.ready().then(() => {
            this.database = new Storage(SqlStorage);
        });*/
    }

    accept() {
        this.database.set("consent", true);
        this.viewCtrl.dismiss();
    }

    decline() {
        this.presentConfirm();
    }

    presentConfirm(){
        console.log("Presenting confirm alert...");
        let alert = this.alertCtrl.create({
            title: 'Confirm',
            message: "Are you sure you want to decline the terms? This will close Tiramisu.",
            buttons: [
                {
                    text: 'Cancel',
                    role : 'cancel',
                    handler: ()=>{
                        console.log("Decline cancelled");
                    }        
                },
                {
                    text: 'Yes',
                    handler: ()=>{
                        console.log("Decline confirmed");
                        this.platform.exitApp();
                    }
                }]
        });

        alert.present();
        
    }

}
