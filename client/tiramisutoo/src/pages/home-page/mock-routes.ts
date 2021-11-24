
// export var nearbyRoutes : Route[] = [
//     new Route('71D', 'In', 'Trafford via Oakland', 'Fifth Ave/ MorewoodAve', '3'),
//     new Route('71C', 'Out', 'Schenley via Oakland', 'Fifth Ave Opp Morewood Ave', '6'),
//     new Route('28X', 'In', 'Downtown via Oakland', 'Fifth Ave/ MorewoodAve', '10'),
//     new Route('64', 'Out', 'Schenley via Oakland', 'Fifth Ave Opp Morewood Ave', '6'),
//     new Route('61D', 'In', 'Trafford via Oakland', 'Fifth Ave/ MorewoodAve', '2'),
//     new Route('61A', 'In', 'Trafford via Oakland', 'Fifth Ave/ MorewoodAve', '15'),
//     new Route('61B', 'In', 'Trafford via Oakland', 'Fifth Ave/ MorewoodAve', '3'),
//   ]

// classes for mock routes
export class Stop {
  name : string;
  time : string;
}

export class Route {
  public title : string;
  public direction : string;
  public dest : string;
  public stop: string;
  public time: string;
  public selected : boolean;
  public stopsList: Stop[];

  constructor(t, d, dest, s, time, stopsList){
    this.title = t;
    this.direction = d;
    this.dest = dest;
    this.stop = s;
    this.time = time;
    this.stopsList = stopsList;
    this.selected = true;
  }
}

var stops1 : Stop[] = [
      {"name" : "Forbes Ave opp Amberson Ave", "time" : "3 min"},
      {"name" : "Fifth Ave at Wilkins Ave", "time" : "5 min"}
    ];

var stops2 : Stop[] = [ 
      {"name" : "Fifth Ave opp Aiken Ave", "time" : "6 min"},
      {"name" : "Fifth Ave opp Bellefonte St", "time" : "9 min"},
      {"name" : "Fifth Ave at Negley Ave", "time" : "11 min"}
    ];

var stops3 : Stop[] = [
      {"name" : "Fifth Ave opp Maryland Ave", "time" : "14 min"},
      {"name" : "Fifth Ave opp College St", "time" : "15 min"},
      {"name" : "Highland Ave at Fifth Avenue", "time" : "18 min"}
    ];

var stops4 : Stop[] = [
      {"name" : "Highland Ave at Walnut St", "time" : "19 min"},
      {"name" : "Highland Ave opp Elwood St", "time" : "21 min"}
    ];

export var MOCK_ROUTES = [
  new Route('71D', 'In', 'Trafford via Oakland', 'Fifth Ave/ MorewoodAve', '3', 
    stops1),

  new Route('71C', 'Out', 'Schenley via Oakland', 'Fifth Ave Opp Morewood Ave', '6', 
    stops2),
  new Route('28X', 'In', 'Downtown via Oakland', 'Fifth Ave/ MorewoodAve', '10', 
    stops3),
  new Route('64', 'Out', 'Schenley via Oakland', 'Fifth Ave Opp Morewood Ave', '6',
    stops4),
  new Route('61D', 'In', 'Trafford via Oakland', 'Fifth Ave/ MorewoodAve', '2',
    stops1),
  new Route('61A', 'In', 'Trafford via Oakland', 'Fifth Ave/ MorewoodAve', '15',
    stops2),
  new Route('61B', 'In', 'Trafford via Oakland', 'Fifth Ave/ MorewoodAve', '3',
    stops3)
];
