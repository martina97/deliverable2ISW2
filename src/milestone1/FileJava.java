package milestone1;

import java.util.HashMap;
import java.util.Map;

public class FileJava {

	
	private String name;
	private HashMap<Integer,String> fileMap = new HashMap<>();	//key = release, value = buggyness (yes/no)
	private String oldPath;
	
	
	public FileJava(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Map<Integer,String> getMap() {
		return fileMap;
	}
	
	
	public String getoldPath() {
		return oldPath;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setMap(Integer release, String bugg) {
		this.fileMap.put(release,bugg);
	}
	
	public void setOldPath(String oldPath) {
		this.oldPath = oldPath;
	}
	
}