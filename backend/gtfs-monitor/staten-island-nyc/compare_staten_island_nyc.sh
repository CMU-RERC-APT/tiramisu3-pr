#!/bin/bash

#export PEOPLE="katadh@sbcglobal.net"
export GTFS2DIR=/home/ubuntu/tiramisu/backend/gtfs-monitor/staten-island-nyc

TO="oscarr@andrew.cmu.edu"
FROM="tiramisutransit@gmail.com"
SUBJECT="Staten Island GTFS changed"
MESSAGE="Remember to update the 'current' file in ${GTFS2DIR}"

curl http://web.mta.info/developers/developer-data-terms.html#data > ${GTFS2DIR}/page 2>/dev/null
grep staten_island ${GTFS2DIR}/page > ${GTFS2DIR}/new
diff --brief ${GTFS2DIR}/new ${GTFS2DIR}/current > /dev/null
comp_value=$?

if [ $comp_value -eq 1 ]
then
    #echo "Remember to update the 'current' file in ${GTFS2DIR}" | mail -s "PAAC GTFS changed" $PEOPLE
    # curl -v -X POST -H "Date: $date" -H "$auth_header" --data-urlencode "$message" --data-urlencode "$to" --data-urlencode "$source" --data-urlencode "$action" --data-urlencode "$subject"  "$endpoint"
    java -jar /home/ubuntu/tiramisu/backend/gtfs-monitor/send_email.jar "$FROM" "$TO" "$SUBJECT" "$MESSAGE"
fi

