package milestone1;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Ticket {

	private String id;
	private List<Integer> aV;
	private LocalDateTime resolutionDate;
	private LocalDateTime creationDate;
	private Integer fV;
	private Integer oV;
	private Integer iV;
	private Integer index;
	private List<String> fileList;
	
	
	//costruttore
	public Ticket(String id, List<Integer> aV, LocalDateTime resolutionDate, LocalDateTime creationDate) {
		
	this.id = id;
	this.aV =aV;
	this.resolutionDate = resolutionDate;
	this.creationDate = creationDate;
	}
	
	//get 
	
	public String getID() {
		return id;
	}
	
	public List<Integer> getAV() {
		return aV;
	}
	
	public LocalDateTime getResolutionDate() {
		return resolutionDate;
	}
	
	public LocalDateTime getCreationDate() {
		return creationDate;
	}
	
	public Integer getFV() {
		return fV;
	}
	
	public Integer getOV() {
		return oV;
	}
	
	public Integer getIndex() {
		return index;
	}
	
	public List<String> getFileList() {
		return fileList;
	}
	
	
	
	public Integer getIV() {
		return iV;
	}
	
	//set
	public void setID(String id) {
		this.id = id;	
	}
	
	public void setAV(List<Integer> aV) {
		this.aV = aV;	
	}
	public void setResolutionDate(LocalDateTime resolutionDate) {
		this.resolutionDate = resolutionDate;	
	}
	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;	
	}
	
	public void setFV(Integer fV) {
		this.fV = fV;
	}	
	
	public void setOV(Integer oV) {
		this.oV = oV;
	}
	
	public void setIV(Integer iV) {
		this.iV = iV;
	}
	
	public void setIndex(Integer index) {
		this.index = index;
	}
	
	public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}
}