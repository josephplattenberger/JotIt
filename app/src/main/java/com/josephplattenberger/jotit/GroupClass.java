package com.josephplattenberger.jotit;

class GroupClass {
	private int backgroundColor;
	private int backgroundColorCopy;
	private int textColor;
	private int highlightColor;
	private String name;
	private String theme;

	GroupClass(String name, int backgroundColor, int textColor, int highlightColor){
		this.name = name;
		this.backgroundColor = backgroundColor;
		this.backgroundColorCopy = backgroundColor;
		this.textColor = textColor;
		this.highlightColor = highlightColor;
	}
	public void setName(String name){
		this.name = name;
	}
	public String getName(){
		return name;
	}
	public void setTheme(String theme){
		this.theme = theme;
	}
	public String getTheme(){
		return theme;
	}
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
	
}
