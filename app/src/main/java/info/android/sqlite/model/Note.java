package info.android.sqlite.model;

public class Note {
	
	private String Subject, Group_Name, Note_Text, Reminder_Date_Time, Updated_Date_Time;
	
	//constructors
	public Note(String Subject, String Group_Name, String Note_Text){
		this.Subject = Subject;
		this.Group_Name = Group_Name;
		this.Note_Text = Note_Text;
	}
	
	//setters
	public void setSubject(String Subject){
		this.Subject = Subject;
	}
	
	public void setGroup_Name(String Group_Name){
		this.Group_Name = Group_Name;
	}
	
	public void setNote_Text(String Note_Text){
		this.Note_Text = Note_Text;
	}
	
	public void setReminder_Date_Time(String Reminder_Date_Time){
		this.Reminder_Date_Time = Reminder_Date_Time;
	}
	
	public void setUpdated_Date_Time(String Updated_Date_Time){
		this.Updated_Date_Time = Updated_Date_Time;
	}
	
	//getters
	public String getSubject(){
		return this.Subject;
	}
	
	public String getGroup_Name(){
		return this.Group_Name;
	}
	
	public String getNote_Text(){
		return this.Note_Text;
	}
	
	public String getReminder_Date_Time(){
		return this.Reminder_Date_Time;
	}
	
	public String getUpdated_Date_Time(){
		return this.Updated_Date_Time;
	}
}
	
	

