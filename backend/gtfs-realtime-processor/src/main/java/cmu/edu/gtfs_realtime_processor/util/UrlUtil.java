package cmu.edu.gtfs_realtime_processor.util;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;



public class UrlUtil {
		
    public static String makeURL(String base, String func, Map<String, String> params) {
       StringBuffer sb = new StringBuffer(base);
       sb.append(func);
       String prefix = "?";
		for (String key : params.keySet()) {
			sb.append(prefix);
			prefix = "&";
			sb.append(key);
			sb.append("=");
			sb.append(params.get(key));
		}
		return sb.toString();
    }
    
    public static String combineArgs(String[] args) {
    	String res = "";
        for (int i = 0; i < args.length; ++i) {
           if (i > 0) res += ",";
           res += args[i];
        }
        return res;
    }
    
    public static String[][] fetchGroups(List<String> array, int MAX_COUNT) {
	      int num = (array.size() + MAX_COUNT - 1) / MAX_COUNT;
	      int rem = array.size() - MAX_COUNT * (num - 1);
	      String[][] groups = new String[num][];
	      for (int i = 0; i < num - 1; ++i) {
	         groups[i] = new String[MAX_COUNT];
	      }
	      groups[num - 1] = new String[rem];
	      for (int i = 0; i < array.size(); ++i) {
	         groups[i / MAX_COUNT][i % MAX_COUNT] = array.get(i);
	      }
	      return groups;
	   }
}
