export interface UpcomingArrivals {
    [index: number]: ScheduleEntry
}

export interface ScheduleEntry {
    routeId?: string;
    tripId?: string;
    serviceDate?: number;
    stopId?: string;
    stopName?: string;
    stopLat?: number;
    stopLon?: number;
    stopSequence?: number;
    //blockTripSequence: number;
    directionId?: number;
    routeShortName?: string;
    routeLongName?: string;
    tripHeadsign?: string;
    //arrivalEnabled: boolean;
    //departureEnable: boolean;
    scheduledArrivalTime?: number;
    scheduledDepartureTime?: number;
    //frequency?: string; //should be a new class
    predicted?: boolean;
    predictedArrivalTime?: number;
    predictedDepartureTime?: number;
    upcomingArrivals: UpcomingArrivals;
    //distanceFromStop: number;
    //numberOfStopsAway: number;
    //tripStatus?: string; //should be a new class
}
