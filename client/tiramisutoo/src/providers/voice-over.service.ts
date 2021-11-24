import { Injectable } from '@angular/core';

@Injectable()
export class VoiceOverService {

    // Hack, does not work for all elements
    setVoiceOverFocus(element) {
        let focusInterval = 20; // ms, time between function calls
        let focusTotalRepetitions = 20; // number of repetitions

        element.blur();

        let focusRepetitions = 0;
        let interval = window.setInterval(function() {
            element.focus();
            focusRepetitions++;
            if (focusRepetitions >= focusTotalRepetitions) {
                window.clearInterval(interval);
            }
        }, focusInterval);
    }

}
