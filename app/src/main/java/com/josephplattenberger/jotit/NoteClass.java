package com.josephplattenberger.jotit;

class NoteClass {
	private int noteID;
	private int backgroundColor;
	private int backgroundColorCopy;
	private int textColor;
	private int highlightColor;
	private String subject;
	private String noteBody;
	private String group;
	private String editTime;
	private String reminderTime;

	NoteClass(int noteID, String subject, int backgroundColor, int textColor,
					  int highlightColor){
		this.noteID = noteID;
		this.subject = subject;
		this.backgroundColor = backgroundColor;
		this.backgroundColorCopy = backgroundColor;
		this.textColor = textColor;
		this.highlightColor = highlightColor;
	}

	public int getNoteID(){ return noteID; }
	public void setBackgroundColor(int backgroundColor){
		this.backgroundColor = backgroundColor;
	}
	public int getBackgroundColor(){
		return backgroundColor;
	}
	public int getBackgroundColorCopy(){
		return backgroundColorCopy;
	}
	public void setTextColor(int textColor){
		this.textColor = textColor;
	}
	public int getTextColor(){
		return this.textColor;
	}
	
	public int getHighlightColor(){
		return this.highlightColor;
	}
	
	public void setHighlightColor(int highlightColor){
		this.highlightColor = highlightColor;
	}
	public void setSubject(String subject){
		this.subject = subject;
	}
	public String getSubject(){
		return subject;
	}
	public void setNoteBody(String noteBody){
		this.noteBody = noteBody;
	}
	public String getNoteBody(){
		return noteBody;
	}
	public void setGroup(String group){
		this.group = group;
	}
	public String getGroup(){
		return group;
	}
	public void setEditTime(String editTime){
		this.editTime = editTime;
	}
	public String getEditTime(){
		return editTime;
	}
	public void setReminderTime(String reminderTime){
		this.reminderTime = reminderTime;
	}
	public String getReminderTime(){
		return reminderTime;
	}
}
