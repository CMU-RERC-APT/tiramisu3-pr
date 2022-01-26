package gtfs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import org.apache.commons.cli.*;

public class CombineGTFS{
    public static String gt = "google_transit"; // Files are under "google_transit_bureauname"*/ 
    //public static String mtaPath = "YOUR_URL_HERE";
    public static String inputPath;
    public static String outputPath;
    public static String[] bureau = {"", "_bronx", "_brooklyn", "_staten_island", "_queens", "_manhattan"};

    public String[] fileNames = {"calendar", "calendar_dates", "routes", 
                                 "shapes", "stops", "trips", "stop_times"}; /*!!! left out "stop_times" and "agency"*/
    
    public static void generateAgencyFile(){
        try {
            BufferedReader agencyBufferedReader = new BufferedReader(new FileReader(inputPath+"/"+CombineGTFS.gt+bureau[1]+"/agency.txt"));
            String line =null;
            PrintWriter fout = new PrintWriter(new FileWriter(outputPath + "/agency.txt"));

            while ((line = agencyBufferedReader.readLine()) != null){
                fout.println(line);
            }
            fout.close();
        } catch (IOException e) {
            System.out.println("Failed generate agency.txt");
            e.printStackTrace();
            System.exit(0);
        }	
    }
    
    public static void main(String[] args){

        Options options = new Options();

        Option input = new Option("i", true, "input directory");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", true, "output directory");
        output.setRequired(true);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());

            return;
        }

        inputPath = cmd.getOptionValue("i");
        outputPath = cmd.getOptionValue("o");

        generateAgencyFile();
		
        Combine.run("stops.txt");
        Combine.run("calendar.txt");
        Combine.run("trips.txt");
        Combine.run("routes.txt");
        Combine.run("shapes.txt");
        Combine.run("calendar_dates.txt");
        Combine.run("stop_times.txt");
		
    }
}
