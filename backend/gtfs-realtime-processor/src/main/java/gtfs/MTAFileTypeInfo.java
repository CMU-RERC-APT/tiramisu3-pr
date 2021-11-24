package gtfs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

public class MTAFileTypeInfo {

	public LinkedHashMap<String, ArrayList<String>> contentMap = null;
	
	public int size;
	
	public ArrayList<String> fieldsList;
	
//	public String keyField;
	
	Set<String> keyFieldSet;
	
	protected MTAFileTypeInfo(Set<String> kSet) {
		size = 0;
		fieldsList = new ArrayList<String>();
		/*content map is a hashmap with its key being the unique identifier of this file type, for instance, stop id*/
		contentMap = new LinkedHashMap<String, ArrayList<String>>();
		keyFieldSet = kSet;
	}
	
	public void addFile(TextCSVReader file) {
//		System.out.println("Before adding file, contentMap = "+contentMap.toString());
		
		for (String keyField : keyFieldSet){
			int indexOfKeyField = file.headerList.indexOf(keyField);
			
			if (indexOfKeyField == -1){
				System.out.println("headerList = "+file.headerList.toString());
				System.out.println("keyField = "+keyField);
				System.out.println("Error: No such key field.");
				System.exit(0);
			}
			
		}
		
		for (String field : file.headerList){
			if (keyFieldSet.contains(field)){
				continue;
			}
			/* if new fields are not in content map yet*/
			if (!fieldsList.contains(field)){
				
				fieldsList.add(field);
				
				for (Iterator<String> it = contentMap.keySet().iterator(); it.hasNext();){
					String id = it.next();
					/* for previous entries add empty string under new field, which is the last field */
					contentMap.get(id).add("");
				}
				
//				Iterator<String> it = contentMap.keySet().iterator();
//				while (it.hasNext()){
//					String id = it.next();
//					/* for previous entries add empty string under new field, which is the last field */
//					contentMap.get(id).add("");
//				}
			}
		}
		
		/* as of now, info header index should be a superset of file header index*/
		/* establish a mapping form the current info header index to the file header index, 
		 * and -1 if not in file header*/
		
		ArrayList<Integer> headerMapping = new ArrayList<Integer>(); 
		
		for (int i = 0; i < fieldsList.size(); i++){
			headerMapping.add(file.headerList.indexOf(fieldsList.get(i)));
		}
		
		System.out.println("header mapping = "+headerMapping.toString());
		
		/*add entries*/
		for (int i = 0; i < file.getSize(); i++){
			
			/* construct row to add by mapping*/
			ArrayList<String> rowToAdd = new ArrayList<String>();
			for (int j = 0; j < headerMapping.size(); j ++){
				if (headerMapping.get(j)==-1){
					rowToAdd.add("");
				}
				else{
					rowToAdd.add(file.getRow(i).get(headerMapping.get(j)));
				}
			}
			
			String combinedKeyFields = getCombinedKeyField(keyFieldSet);
			
			String key = file.getByFieldIndex(keyFieldSet, i);
			/* if duplicate id*/
			ArrayList<String> existingRow = contentMap.get(key);
			if (existingRow != null){
//				System.out.println("duplicate keys! key = "+key);
				for (int j = 0; j < rowToAdd.size(); j++)
				{
					/* overwrite existing row*/
					if (!rowToAdd.get(j).equals("")){
						existingRow.set(j, rowToAdd.get(j));
					}
				}
//				System.out.println("existing updated:"+existingRow);
			}
			else{
//				System.out.println("rowToAdd = "+rowToAdd);
				contentMap.put(key, rowToAdd);
			}	
		}
		
		size += file.getSize();
	}

	private String getCombinedKeyField(Set<String> kSet) {
		String ret = "";
		Iterator<String> it = kSet.iterator();
		while (it.hasNext()){
			ret += it.next()+",";
		}
		ret = ret.replaceAll(",$", "");
		return ret;
	}

	public String headerToString() {
		String ret = "";
		Iterator<String> itKeyFields = keyFieldSet.iterator();
		while (itKeyFields.hasNext()){
			ret += itKeyFields.next()+",";
		}
		Iterator<String> itOtherFields = fieldsList.iterator();
		while (itOtherFields.hasNext()){
			ret += itOtherFields.next()+",";
		}
		ret = ret.replaceAll(",$", "");
		return ret;
	}

	public String entryToString(String key) {
		String ret = "";
		ret+=key+",";
		for (String element : contentMap.get(key)){
			ret+=element+",";
		}
		ret = ret.replaceAll(",$", "");
		return ret;
	}
}
