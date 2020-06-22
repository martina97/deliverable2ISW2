package milestone1;

import java.util.ArrayList;
import java.util.List;

public class FileAlias {

	
	private String lastFileName;
	private ArrayList<String> alias = new ArrayList<>();
	
	
		
	
	public List<String> getAlias() {
		return alias;
	}
	
	public String getLastFileName() {
		return lastFileName;
	}
	
	
	
	public void setAlias(String oldName) {
		this.alias.add(oldName);
	}
	
	public void setLastFileName(String lastFileName) {
		this.lastFileName = lastFileName;
	}
	
	
	
	public boolean checkAlias(String fileName) {
		
		for(String a : alias) {
			
			if(a.equals(fileName)) {
				return false;
			}
		}
		
		return true;
	}
	
}