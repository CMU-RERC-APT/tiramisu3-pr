package gtfs;
/* Tool for combining GTFS data for different MTA bureaus. */

import java.util.*;

import org.apache.http.impl.conn.tsccm.RouteSpecificPool;

import java.io.*;

public class Combine{
    public static void run(String fileName){
		
        Set<String> keyFieldSet = new LinkedHashSet<String>();

        String keyField = "";
        switch (fileName){
        case "stops.txt":
            keyFieldSet.add("stop_id");
            break;
        case "trips.txt":
            keyFieldSet.add("trip_id");
            break;
        case "calendar.txt":
            keyFieldSet.add("service_id");
            break;
        case "routes.txt":
            keyFieldSet.add("route_id");
            break;
        case "shapes.txt":
            keyFieldSet.add("shape_id");
            keyFieldSet.add("shape_pt_sequence");
            break;
        case "calendar_dates.txt":
            keyFieldSet.add("date");
            keyFieldSet.add("service_id");
            break;
        case "stop_times.txt":
            keyFieldSet.add("stop_id");
            keyFieldSet.add("trip_id");
            break;
        default:
            System.out.print("wrong file type");
            System.exit(0);
        }
	   
        PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out));
        /* map fields to content*/
        MTAFileTypeInfo fileInfo = new MTAFileTypeInfo(keyFieldSet);
        int fileSizeCounter = 0;
        for (String b:CombineGTFS.bureau){
            System.out.println("Bureau: Adding " + fileName + " from bureau"+b);
            TextCSVReader csvReader = new TextCSVReader(CombineGTFS.inputPath + "/"+CombineGTFS.gt+b+"/"+fileName);
            /* For bus company ("google_transit/"), get agencyIdReader from other bureaus and replace in routes.txt*/
            if ((fileName.equals("routes.txt")) && b.equals("")){
                /* replace all agency id in routes.txt from bus company to the agency id from other bureaus*/
                replaceAgencyId(csvReader);
            }
            fileSizeCounter += csvReader.size;
            fileInfo.addFile(csvReader);
        }
        try{
            PrintWriter fout = new PrintWriter(new FileWriter(CombineGTFS.outputPath + "/"+fileName));
            fout.println(fileInfo.headerToString());
		  
            for (Iterator<String> i = fileInfo.contentMap.keySet().iterator(); i.hasNext();){
                String key = i.next();
                fout.println(fileInfo.entryToString(key));	  
            }
            fout.close();
        }catch(Exception e){
            System.out.println("ERORR: cannot write to file");
            e.printStackTrace();
            System.exit(0);
        }
        System.out.println("Finished combine for "+fileName+"\n");
   
    }
	
    /* replace all agency id in routes.txt from bus company to the agency id from other bureaus*/
    private static void replaceAgencyId(TextCSVReader csvReader) {
        /* get sample agency Id */
        TextCSVReader agencyIdReader = new TextCSVReader(CombineGTFS.inputPath+"/"+CombineGTFS.gt+CombineGTFS.bureau[1]+"/agency.txt");
        String sampleAgencyId = (agencyIdReader.getByFieldIndex(Collections.singleton("agency_id"), 0));
        csvReader.replaceColumn("agency_id", sampleAgencyId);	
    }
}
