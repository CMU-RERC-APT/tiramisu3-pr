package cmu.edu.alarm_processor;


import java.util.logging.Logger;
import static java.util.logging.Level.*;

import java.io.File;
import java.lang.Thread;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URL;
import java.nio.file.Paths;

import org.json.JSONObject;

import com.amazonaws.services.sqs.model.Message;


public class TiramisuConfig {

   public static String credentialUrl = "YOUR_URL_HERE";
//   public static String credentialUrl = "YOUR_URL_HERE";
//
//   //public static String credentialUrl = "YOUR_URL_HERE";
   private static final String gtfsPAACDataPath = "gtfs/paac";
   private static final String gtfsMTADataPath = "gtfs/mta";
   private static Logger logger = Logger.getLogger(TiramisuConfig.class.getName());

   /* "/trips.txt" is irrelevant, just added to access
    * the directory one level up. */
   public static String gtfsAbsolutePath(String agency) {
      String result = null;
      try {
         URL resource = null;
         switch (agency){
         	case "PAAC":
//               resource = ClassLoader.getSystemClassLoader().getResource(gtfsPAACDataPath + "/trips.txt");
//               System.out.println("THE RESOURCE" + resource.toString());
//               break;
         		result = "YOUR_URL_HERE";
//         		result = "YOUR_URL_HERE";
         		break;
            case "MTA":
//               resource = ClassLoader.getSystemClassLoader().getResource(gtfsMTADataPath + "/trips.txt");
//               System.out.println("THE RESOURCE" + resource.toString());
//               break;
         		result = "YOUR_URL_HERE";
//         		result = "YOUR_URL_HERE";
         		break;
         }
//         result = Paths.get(resource.toURI()).toFile().getParentFile().getAbsolutePath();
//         System.out.println(result);
      }
      catch (Exception e) {
         logger.log(WARNING, "Invalid path");
      }
      return result;
   }
}
