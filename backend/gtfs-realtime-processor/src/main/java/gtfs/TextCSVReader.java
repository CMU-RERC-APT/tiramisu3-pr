package gtfs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

public class TextCSVReader {

    private BufferedReader br = null;
	
    int size = 0;
	
    private String header = null;
    
    /* content is a 2-d arraly list, 1d index = row, 2d index = column  (corresponding to headerlist*/  
    ArrayList<ArrayList<String>> content = new ArrayList<ArrayList<String>>();
    
    ArrayList<String> headerList = new ArrayList<String>();
	
    private ArrayList<String> parseLine(String line){
        line = line+",";
        ArrayList<String> result = new ArrayList<String>();
        while (line.indexOf(",")!=-1){
            result.add(line.substring(0, line.indexOf(",")));
            line = line.substring(line.indexOf(",")+1);
        }
        return result;
		
    }
	
    /* create a reader type for a text file*/
    protected TextCSVReader(String fileName) {
        try{
            br = new BufferedReader(new FileReader(fileName));
            header = br.readLine();
            String line;
            headerList = parseLine(header);
            /* initialize an empty arrayList for every field*/
            while ((line = br.readLine()) != null){
            	if (line.equals("")){
                    continue;
            	}
            	ArrayList<String> lineList = parseLine(line);
            	content.add(lineList);
            }
                        
            size = content.size();

    	} catch (Exception e){
            e.printStackTrace();
            System.exit(0);
    	}
    }
	
    /* replace an entire column with target string*/
    public void replaceColumn(String field, String str){
        int indexOfField = headerList.indexOf(field);
        if (indexOfField == -1){
            System.out.println("Error: replace column");
            System.exit(0);
        }
        for (ArrayList<String>lineList : content){
            lineList.set(indexOfField, str);
			
        }
    }
	
    public ArrayList<String> getRow(int index){
        return content.get(index);
    }
	
    public int getSize(){
        return size;
    }
	
    /* access the readerfile info by* field name and row index*/
    public String getByFieldIndex(Set<String> keyFieldSet, int index) {
        String ret = "";
        for (String keyField : keyFieldSet){
            if (headerList.indexOf(keyField) == -1){
                System.out.println("Error: getByFieldIndex: no such field");
                System.exit(0);
            }
            ret += content.get(index).get(headerList.indexOf(keyField)) + ",";
        }
        ret = ret.replaceAll(",$", "");

        return ret;
    }

	
}
