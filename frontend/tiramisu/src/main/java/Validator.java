package main.java;

import main.java.util.TimeUtil;
import java.sql.Timestamp;

public class Validator {
    public static void err(String errMsg){
	System.out.println("Invalid parameter: "+errMsg);
    }

    public static Object validateParam(String param, String paramType) throws Exception {
	switch (paramType) {
            case "string":
                return validateStringParam(param);
            case "integer":
                return Integer.valueOf(param);
            case "double":
                return Double.valueOf(param);
            case "unix_time":
                return validateUnixTimeParam(param);
            case "boolean":
                return validateBooleanParam(param);
            default:
                throw new Exception("Param type cannnot be validated");
	}
    }
    
    private static Timestamp validateUnixTimeParam(String param) throws Exception {
    	if (param == null) {
    		throw new Exception("Required unix_time param is null");
    	} else if (Long.valueOf(param) < 0 ) {
    		//TODO: check somewhere else: if the time were older than a day or if the time were in the future
    		//just ignore this request
    		throw new Exception("Required unix_time param is too small");
    	}
    	return new Timestamp(Long.valueOf(param));
    }

    public static String validateStringParam(String param) throws Exception {
    	if (param == null){
    		throw new Exception("Required String param is null");
    	} else if (param.equals("")){
    		throw new Exception("Required String param is empty");
    	} else {
		    return param;
		}
    }
    
    public static Boolean validateBooleanParam(String param) throws Exception {
    	if (param == null) {
    		throw new Exception("Required String param is null");
    	} else if (param.equals("true")) {
    		return true;
    	} else if (param.equals("false")) {
    		return false;
    	} else {
    		throw new Exception("Required String param is not boolean");
    	}
    }
}
