package info.android.sqlite.model;

public class Group {
	
	private String Group_Name, Theme_Name;
	
	//constructors
	public Group(String Group_Name, String Theme_Name){
		this.Group_Name = Group_Name;
		this.Theme_Name = Theme_Name;
	}
	
	//setters
	public void setGroup_Name(String Group_Name){
		this.Group_Name = Group_Name;
	}
	
	public void setTheme_Name(String Theme_Name){
		this.Theme_Name = Theme_Name;
	}

	//getters
	public String getGroup_Name(){
		return this.Group_Name;
	}
	
	public String getTheme_Name(){
		return this.Theme_Name;
	}

}
