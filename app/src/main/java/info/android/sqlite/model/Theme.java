package info.android.sqlite.model;

public class Theme {
	
	private String Theme_Name, Background_Color, Text_Color, Highlight_Color;
	
	//constructors
	public Theme(String Theme_Name){
		this.Theme_Name = Theme_Name;
	}
	
	public Theme(String Theme_Name, String Background_Color, String Text_Color, String Highlight_Color){
		this.Theme_Name = Theme_Name;
		this.Background_Color = Background_Color;
		this.Text_Color = Text_Color;
		this.Highlight_Color = Highlight_Color;
	}

	//setters
	public void setTheme_Name(String Theme_Name){
		this.Theme_Name = Theme_Name;
	}	
		
	public void setBackground_Color(String Background_Color){	
		this.Background_Color = Background_Color;
	}	
		
	public void setText_ColorTheme(String Text_Color){	
		this.Text_Color = Text_Color;
	}
	
	public void setHighlight_Color(String Highlight_Color){
		this.Highlight_Color = Highlight_Color;
	}
	
	//getters
	public String getTheme_Name(){
		return this.Theme_Name;
	}	
		
	public String getBackground_Color(){	
		return this.Background_Color;
	}	
		
	public String getText_ColorTheme(){	
		return this.Text_Color;
	}
	
	public String getHighlight_Color(){
		return this.Highlight_Color;
	}
}