package milestone1;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

public class Release {

	private Integer index;
	private LocalDateTime date;
	private String rel;
	private List<RevCommit> listCommit;
	private List<FileJava2> listFile;

	public Release(Integer index, LocalDateTime date, String release) {

		this.index = index;
		this.date = date;
		this.rel = release;
		this.listCommit = new ArrayList<>();
		this.listFile = new ArrayList<>();

	}

	public String getRelease() {
		return rel;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public Integer getIndex() {
		return index;
	}

	public List<RevCommit> getListCommit() {
		return listCommit;
	}

	public List<FileJava2> getListFile() {
		return listFile;
	}

	public void setRelease(String release) {
		this.rel = release;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public void setIndec(Integer index) {
		this.index = index;
	}

	public void setListCommit(List<RevCommit> listCommit) {
		this.listCommit = listCommit;
	}

	public void setListFile(List<FileJava2> listFile) {
		this.listFile = listFile;
	}

}