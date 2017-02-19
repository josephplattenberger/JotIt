package com.josephplattenberger.jotit;

class ThemeClass {
	private String name;
	private int backgroundColor;
	private int backgroundColorCopy;
	private int textColor;
	private int highlightColor;
	
	ThemeClass(String name, int backgroundColor, int textColor, int highlightColor){
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
