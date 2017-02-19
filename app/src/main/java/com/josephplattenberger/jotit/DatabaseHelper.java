package com.josephplattenberger.jotit;

import info.android.sqlite.model.Group;
import info.android.sqlite.model.Note;
import info.android.sqlite.model.Theme;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


@SuppressLint("SimpleDateFormat")

class DatabaseHelper extends SQLiteOpenHelper {
		
    // Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "JotIt_DB.db";
 
    // Table Names
	private static final String TABLE_NOTE = "Note";
	private static final String TABLE_NOTE_FTS = "NoteFTS";
	private static final String TABLE_GROUP = "GroupTable";
	private static final String TABLE_THEME = "Theme";
	private static final String TABLE_PALETTE = "Palette";
   
    // Common column names
	private static final String GROUP_NAME = "Group_Name";
	private static final String THEME_NAME = "Theme_Name";
 
    // Note Table - column names
	private static final String KEY_ROWID = "_id";
	private static final String NOTE_ID = "Note_id";
	private static final String SUBJECT = "Subject";
	private static final String NOTE_TEXT = "Note_Text";
	private static final String RDT = "Reminder_Date_Time";
	private static final String UDT = "Updated_Date_Time";
 
    // Theme Table - column names
	private static final String BACK_COLOR = "Background_Color";
	private static final String TEXT_COLOR = "Text_Color";
	private static final String HIGH_COLOR = "Highlight_Color";
 
    // Palette Table - column names
	private static final String COLOR_NAME = "Color_Name";
	private static final String HTML_CODE = "HTML_Code";
    
    //Trigger Names
	private static final String TRIGGER_DELETE = "DeleteFTS";
	private static final String TRIGGER_INSERT = "InsertFTS";
	private static final String TRIGGER_UPDT_SUB = "UpdateSubjectFTS";
	private static final String TRIGGER_UPDT_GROUP = "UpdateGroup";
	private static final String TRIGGER_UPDT_THEME = "UpdateTheme";
 
    // Table Create Statements
    // Note table create statement
    private static final String CREATE_TABLE_NOTE = "CREATE TABLE " + TABLE_NOTE 
            + "(" + NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
			+ SUBJECT + " CHAR (140) NOT NULL  DEFAULT (null), "
    		+ GROUP_NAME + " CHAR (140) NOT NULL  DEFAULT (null), "
            + NOTE_TEXT + " TEXT, " 
    		+ RDT + " DATETIME DEFAULT (null), "
            + UDT + " DATETIME DEFAULT (null), "
            + "FOREIGN KEY(" + GROUP_NAME +") REFERENCES " + TABLE_GROUP + "(" + GROUP_NAME + "))";
    
    // create FTS tables for quicker matches for search
    private static final String CREATE_VIRTUAL_TABLE_NOTE = "CREATE VIRTUAL TABLE " + TABLE_NOTE_FTS 
    		+ " USING fts3(" + NOTE_ID + " INTEGER PRIMARY KEY NOT NULL, "
    		+ SUBJECT + " CHAR (140) NOT NULL  DEFAULT (null))";
  
    // Group table create statement
    private static final String CREATE_TABLE_GROUP = "CREATE TABLE " + TABLE_GROUP
            + "(" + GROUP_NAME + " CHAR (140) PRIMARY KEY  NOT NULL  DEFAULT (null), "
    		+ THEME_NAME + " CHAR (140) NOT NULL  DEFAULT (null), "
    		+ "FOREIGN KEY(" + THEME_NAME +") REFERENCES " + TABLE_THEME + "(" + THEME_NAME + "))";
     
    
    // Theme table create statement
    private static final String CREATE_TABLE_THEME = "CREATE TABLE " + TABLE_THEME
            + "(" + THEME_NAME + " CHAR (140) PRIMARY KEY  NOT NULL  DEFAULT (null), "
            + BACK_COLOR + " CHAR (30) NOT NULL  DEFAULT (null), " 
            + TEXT_COLOR + " CHAR (30) NOT NULL  DEFAULT (null), "
            + HIGH_COLOR + " CHAR (30) NOT NULL  DEFAULT (null), "
            + "FOREIGN KEY(" + BACK_COLOR +") REFERENCES " + TABLE_PALETTE + "(" + COLOR_NAME + "), "
            + "FOREIGN KEY(" + TEXT_COLOR +") REFERENCES " + TABLE_PALETTE + "(" + COLOR_NAME + "), "
            + "FOREIGN KEY(" + HIGH_COLOR +") REFERENCES " + TABLE_PALETTE + "(" + COLOR_NAME + "))";

    // Palette table create statement
    private static final String CREATE_TABLE_PALETTE = "CREATE TABLE " + TABLE_PALETTE
            + "(" + COLOR_NAME + " CHAR (30) PRIMARY KEY  NOT NULL  DEFAULT (null),"
    		+ HTML_CODE + " INTEGER (10) NOT NULL  DEFAULT (null))";
   
    //Triggers
    //Delete NoteFTS
    private static final String CREATE_TRIGGER_DELETE = "CREATE TRIGGER " + TRIGGER_DELETE 
    		+ " AFTER DELETE ON " + TABLE_NOTE +" BEGIN delete from " + TABLE_NOTE_FTS
    		+ " Where " + NOTE_ID + " = old." + NOTE_ID + "; END";
    
    //Insert NoteFTS
    private static final String CREATE_TRIGGER_INSERT = "CREATE TRIGGER " + TRIGGER_INSERT 
    		+ " AFTER INSERT ON " + TABLE_NOTE +" BEGIN insert into " + TABLE_NOTE_FTS
    		+ "(" + NOTE_ID + ", " + SUBJECT + ") VALUES (new." + NOTE_ID + ", new."
    		+ SUBJECT + "); END";
    
	//Update NoteFTS Subject
    private static final String CREATE_TRIGGER_UPDT_SUBJ = "CREATE TRIGGER \"" + TRIGGER_UPDT_SUB
    		+ "\" UPDATE OF " + SUBJECT + " ON "+ TABLE_NOTE +" BEGIN update " + TABLE_NOTE_FTS
    		+ " SET " + SUBJECT + " = new." + SUBJECT + " Where " + NOTE_ID + " = old."
    		+ NOTE_ID + "; END";

	private static final String CREATE_TRIGGER_UPDT_GROUP = "CREATE TRIGGER " + TRIGGER_UPDT_GROUP
			+ " UPDATE OF " + GROUP_NAME + " ON " + TABLE_GROUP + " BEGIN update " + TABLE_NOTE
			+ " SET " + GROUP_NAME + " = new." + GROUP_NAME + " WHERE " + GROUP_NAME + " = old."
			+ GROUP_NAME + "; END";

	private static final String CREATE_TRIGGER_UPDT_THEME = "CREATE TRIGGER " + TRIGGER_UPDT_THEME
			+ " UPDATE OF " + THEME_NAME + " ON " + TABLE_THEME + " BEGIN update " + TABLE_GROUP
			+ " SET " + THEME_NAME + " = new." + THEME_NAME + " WHERE " + THEME_NAME + " = old."
			+ THEME_NAME + "; END";

    //Default data population
    //Group insert into statement
    private static final String INSERT_INTO_TABLE_GROUP = "INSERT INTO " + TABLE_GROUP
    		+ "(" + GROUP_NAME + ", " + THEME_NAME + ") VALUES ('Other Notes', 'Default')";
    
    //Theme insert into statement
    private static final String INSERT_INTO_TABLE_THEME = "INSERT INTO " + TABLE_THEME
    		+ "(" + THEME_NAME + ", " + BACK_COLOR + ", " + TEXT_COLOR + ", " + HIGH_COLOR
    		+ ") VALUES ('Default', 'Aquamarine1', 'Black', 'Gold1')";
    
    //Palette insert into statements
    private static final String INSERT_INTO_TABLE_PALETTE1 = "INSERT INTO " + TABLE_PALETTE
    		+ "(" + COLOR_NAME + ", " + HTML_CODE + ") VALUES " 
    		+ "('Aliceblue', '0xFFF0F8FF'), ('Antiquewhite', '0xFFFAEBD7'), ('Antiquewhite1', '0xFFFFEFDB'), "
    		+ "('Antiquewhite2', '0xFFEEDFCC'), ('Antiquewhite3', '0xFFCDC0B0'), ('Antiquewhite4', '0xFF8B8378'), "
    		+ "('Aquamarine1', '0xFF7FFFD4'), ('Aquamarine2', '0xFF76EEC6'), ('Aquamarine4', '0xFF458B74'), "
    		+ "('Azure1', '0xFFF0FFFF'), ('Azure2', '0xFFE0EEEE'), ('Azure3', '0xFFC1CDCD'), "
    		+ "('Azure4', '0xFF838B8B'), ('Beige', '0xFFF5F5DC'), ('Bisque1', '0xFFFFE4C4'), "
    		+ "('Bisque2', '0xFFEED5B7'), ('Bisque3', '0xFFCDB79E'), ('Bisque4', '0xFF8B7D6B'), "
    		+ "('Black', '0xFF000000'), ('Blanchedalmond', '0xFFFFEBCD'), ('Blue1', '0xFF0000FF'), "
    		+ "('Blue2', '0xFF0000EE'), ('Blue4', '0xFF00008B'), ('Blueviolet', '0xFF8A2BE2'), "
    		+ "('Brown', '0xFFA52A2A'), ('Brown1', '0xFFFF4040'), ('Brown2', '0xFFEE3B3B'), "
    		+ "('Brown3', '0xFFCD3333'), ('Brown4', '0xFF8B2323'), ('Burlywood', '0xFFDEB887'), "
    		+ "('Burlywood1', '0xFFFFD39B'), ('Burlywood2', '0xFFEEC591'), ('Burlywood3', '0xFFCDAA7D'), "
    		+ "('Burlywood4', '0xFF8B7355'), ('Cadetblue', '0xFF5F9EA0'), ('Cadetblue1', '0xFF98F5FF'), "
    		+ "('Cadetblue2', '0xFF8EE5EE'), ('Cadetblue3', '0xFF7AC5CD'), ('Cadetblue4', '0xFF53868B'), "
    		+ "('Chartreuse1', '0xFF7FFF00'), ('Chartreuse2', '0xFF76EE00'), ('Chartreuse3', '0xFF66CD00'), "
    		+ "('Chartreuse4', '0xFF458B00'), ('Chocolate', '0xFFD2691E'), ('Chocolate1', '0xFFFF7F24'), "
    		+ "('Chocolate2', '0xFFEE7621'), ('Chocolate3', '0xFFCD661D'), ('Coral', '0xFFFF7F50'), "
    		+ "('Coral1', '0xFFFF7256'), ('Coral2', '0xFFEE6A50'), ('Coral3', '0xFFCD5B45'), "
    		+ "('Coral4', '0xFF8B3E2F'), ('Cornflowerblue', '0xFF6495ED'), ('Cornsilk1', '0xFFFFF8DC'), "
    		+ "('Cornsilk2', '0xFFEEE8CD'), ('Cornsilk3', '0xFFCDC8B1'), ('Cornsilk4', '0xFF8B8878'), "
    		+ "('Cyan1', '0xFF00FFFF'), ('Cyan2', '0xFF00EEEE'), ('Cyan3', '0xFF00CDCD'), "
    		+ "('Cyan4', '0xFF008B8B'), ('Darkgoldenrod', '0xFFB8860B'), ('Darkgoldenrod1', '0xFFFFB90F'), "
    		+ "('Darkgoldenrod2', '0xFFEEAD0E'), ('Darkgoldenrod3', '0xFFCD950C'), ('Darkgoldenrod4', '0xFF8B6508'), "
    		+ "('Darkgreen', '0xFF006400'), ('Darkkhaki', '0xFFBDB76B'), ('Darkolivegreen', '0xFF556B2F'), "
    		+ "('Darkolivegreen1', '0xFFCAFF70'), ('Darkolivegreen2', '0xFFBCEE68'), ('Darkolivegreen3', '0xFFA2CD5A'), "
    		+ "('Darkolivegreen4', '0xFF6E8B3D'), ('Darkorange', '0xFFFF8C00'), ('Darkorange1', '0xFFFF7F00'), "
    		+ "('Darkorange2', '0xFFEE7600'), ('Darkorange3', '0xFFCD6600'), ('Darkorange4', '0xFF8B4500'), "
    		+ "('Darkorchid', '0xFF9932CC'), ('Darkorchid1', '0xFFBF3EFF'), ('Darkorchid2', '0xFFB23AEE'), "
    		+ "('Darkorchid3', '0xFF9A32CD'), ('Darkorchid4', '0xFF68228B'), ('Darksalmon', '0xFFE9967A'), "
    		+ "('Darkseagreen', '0xFF8FBC8F'), ('Darkseagreen1', '0xFFC1FFC1'), ('Darkseagreen2', '0xFFB4EEB4'), "
    		+ "('Darkseagreen3', '0xFF9BCD9B'), ('Darkseagreen4', '0xFF698B69'), ('Darkslateblue', '0xFF483D8B'), "
    		+ "('Darkslategray', '0xFF2F4F4F'), ('Darkslategray1', '0xFF97FFFF'), ('Darkslategray2', '0xFF8DEEEE'), "
    		+ "('Darkslategray3', '0xFF79CDCD'), ('Darkslategray4', '0xFF528B8B'), ('Darkturquoise', '0xFF00CED1'), "
    		+ "('Darkviolet', '0xFF9400D3'), ('Deeppink1', '0xFFFF1493'), ('Deeppink2', '0xFFEE1289'), "
    		+ "('Deeppink3', '0xFFCD1076'), ('Deeppink4', '0xFF8B0A50'), ('Deepskyblue1', '0xFF00BFFF'), "
    		+ "('Deepskyblue2', '0xFF00B2EE'), ('Deepskyblue3', '0xFF009ACD'), ('Deepskyblue4', '0xFF00688B'), "
    		+ "('Dimgray', '0xFF696969'), ('Dodgerblue1', '0xFF1E90FF'), ('Dodgerblue2', '0xFF1C86EE'), "
    		+ "('Dodgerblue3', '0xFF1874CD'), ('Dodgerblue4', '0xFF104E8B'), ('Firebrick', '0xFFB22222'), "
    		+ "('Firebrick1', '0xFFFF3030'), ('Firebrick2', '0xFFEE2C2C'), ('Firebrick3', '0xFFCD2626'), "
    		+ "('Firebrick4', '0xFF8B1A1A'), ('Floralwhite', '0xFFFFFAF0'), ('Forestgreen', '0xFF228B22'), "
    		+ "('Gainsboro', '0xFFDCDCDC'), ('Ghostwhite', '0xFFF8F8FF'), ('Gold1', '0xFFFFD700')";
    		
    private static final String INSERT_INTO_TABLE_PALETTE2 = "INSERT INTO " + TABLE_PALETTE
    		+ "(" + COLOR_NAME + ", " + HTML_CODE + ") VALUES " 	
    		+ "('Gold2', '0xFFEEC900'), ('Gold3', '0xFFCDAD00'), ('Gold4', '0xFF8B7500'), "
    		+ "('Goldenrod', '0xFFDAA520'), ('Goldenrod1', '0xFFFFC125'), ('Goldenrod2', '0xFFEEB422'), "
    		+ "('Goldenrod3', '0xFFCD9B1D'), ('Goldenrod4', '0xFF8B6914'), ('Gray', '0xFFBEBEBE'), "
    		+ "('Gray1', '0xFF030303'), ('Gray10', '0xFF1A1A1A'), ('Gray11', '0xFF1C1C1C'), "
    		+ "('Gray12', '0xFF1F1F1F'), ('Gray13', '0xFF212121'), ('Gray14', '0xFF242424'), "
    		+ "('Gray15', '0xFF262626'), ('Gray16', '0xFF292929'), ('Gray17', '0xFF2B2B2B'), "
    		+ "('Gray18', '0xFF2E2E2E'), ('Gray19', '0xFF303030'), ('Gray2', '0xFF050505'), "
    		+ "('Gray20', '0xFF333333'), ('Gray21', '0xFF363636'), ('Gray22', '0xFF383838'), "
    		+ "('Gray23', '0xFF3B3B3B'), ('Gray24', '0xFF3D3D3D'), ('Gray25', '0xFF404040'), "
    		+ "('Gray26', '0xFF424242'), ('Gray27', '0xFF454545'), ('Gray28', '0xFF474747'), "
    		+ "('Gray29', '0xFF4A4A4A'), ('Gray3', '0xFF080808'), ('Gray30', '0xFF4D4D4D'), "
    		+ "('Gray31', '0xFF4F4F4F'), ('Gray32', '0xFF525252'), ('Gray33', '0xFF545454'), "
    		+ "('Gray34', '0xFF575757'), ('Gray35', '0xFF595959'), ('Gray36', '0xFF5C5C5C'), "
    		+ "('Gray37', '0xFF5E5E5E'), ('Gray38', '0xFF616161'), ('Gray39', '0xFF636363'), "
    		+ "('Gray4', '0xFF0A0A0A'), ('Gray40', '0xFF666666'), ('Gray42', '0xFF6B6B6B'), "
    		+ "('Gray43', '0xFF6E6E6E'), ('Gray44', '0xFF707070'), ('Gray45', '0xFF737373'), "
    		+ "('Gray46', '0xFF757575'), ('Gray47', '0xFF787878'), ('Gray48', '0xFF7A7A7A'), "
    		+ "('Gray49', '0xFF7D7D7D'), ('Gray5', '0xFF0D0D0D'), ('Gray50', '0xFF7F7F7F'), "
    		+ "('Gray51', '0xFF828282'), ('Gray52', '0xFF858585'), ('Gray53', '0xFF878787'), "
    		+ "('Gray54', '0xFF8A8A8A'), ('Gray55', '0xFF8C8C8C'), ('Gray56', '0xFF8F8F8F'), "
    		+ "('Gray57', '0xFF919191'), ('Gray58', '0xFF949494'), ('Gray59', '0xFF969696'), "
    		+ "('Gray6', '0xFF0F0F0F'), ('Gray60', '0xFF999999'), ('Gray61', '0xFF9C9C9C'), "
    		+ "('Gray62', '0xFF9E9E9E'), ('Gray63', '0xFFA1A1A1'), ('Gray64', '0xFFA3A3A3'), "
    		+ "('Gray65', '0xFFA6A6A6'), ('Gray66', '0xFFA8A8A8'), ('Gray67', '0xFFABABAB'), "
    		+ "('Gray68', '0xFFADADAD'), ('Gray69', '0xFFB0B0B0'), ('Gray7', '0xFF121212'), "
    		+ "('Gray70', '0xFFB3B3B3'), ('Gray71', '0xFFB5B5B5'), ('Gray72', '0xFFB8B8B8'), "
    		+ "('Gray73', '0xFFBABABA'), ('Gray74', '0xFFBDBDBD'), ('Gray75', '0xFFBFBFBF'), "
    		+ "('Gray76', '0xFFC2C2C2'), ('Gray77', '0xFFC4C4C4'), ('Gray78', '0xFFC7C7C7'), "
    		+ "('Gray79', '0xFFC9C9C9'), ('Gray8', '0xFF141414'), ('Gray80', '0xFFCCCCCC'), "
    		+ "('Gray81', '0xFFCFCFCF'), ('Gray82', '0xFFD1D1D1'), ('Gray83', '0xFFD4D4D4')";

    private static final String INSERT_INTO_TABLE_PALETTE3 = "INSERT INTO " + TABLE_PALETTE
    		+ "(" + COLOR_NAME + ", " + HTML_CODE + ") VALUES " 	
    		+ "('Gray84', '0xFFD6D6D6'), ('Gray85', '0xFFD9D9D9'), ('Gray86', '0xFFDBDBDB'), "
    		+ "('Gray87', '0xFFDEDEDE'), ('Gray88', '0xFFE0E0E0'), ('Gray89', '0xFFE3E3E3'), "
    		+ "('Gray9', '0xFF171717'), ('Gray90', '0xFFE5E5E5'), ('Gray91', '0xFFE8E8E8'), "
    		+ "('Gray92', '0xFFEBEBEB'), ('Gray93', '0xFFEDEDED'), ('Gray94', '0xFFF0F0F0'), "
    		+ "('Gray95', '0xFFF2F2F2'), ('Gray97', '0xFFF7F7F7'), ('Gray98', '0xFFFAFAFA'), "
    		+ "('Gray99', '0xFFFCFCFC'), ('Green1', '0xFF00FF00'), ('Green2', '0xFF00EE00'), "
    		+ "('Green3', '0xFF00CD00'), ('Green4', '0xFF008B00'), ('Greenyellow', '0xFFADFF2F'), "
    		+ "('Honeydew1', '0xFFF0FFF0'), ('Honeydew2', '0xFFE0EEE0'), ('Honeydew3', '0xFFC1CDC1'), "
    		+ "('Honeydew4', '0xFF838B83'), ('Hotpink', '0xFFFF69B4'), ('Hotpink1', '0xFFFF6EB4'), "
    		+ "('Hotpink2', '0xFFEE6AA7'), ('Hotpink3', '0xFFCD6090'), ('Hotpink4', '0xFF8B3A62'), "
    		+ "('Indianred', '0xFFCD5C5C'), ('Indianred1', '0xFFFF6A6A'), ('Indianred2', '0xFFEE6363'), "
    		+ "('Indianred3', '0xFFCD5555'), ('Indianred4', '0xFF8B3A3A'), ('Ivory1', '0xFFFFFFF0'), "
    		+ "('Ivory2', '0xFFEEEEE0'), ('Ivory3', '0xFFCDCDC1'), ('Ivory4', '0xFF8B8B83'), "
    		+ "('Khaki', '0xFFF0E68C'), ('Khaki1', '0xFFFFF68F'), ('Khaki2', '0xFFEEE685'), "
    		+ "('Khaki3', '0xFFCDC673'), ('Khaki4', '0xFF8B864E'), ('Lavender', '0xFFE6E6FA'), "
    		+ "('Lavenderblush1', '0xFFFFF0F5'), ('Lavenderblush2', '0xFFEEE0E5'), ('Lavenderblush3', '0xFFCDC1C5'), "
    		+ "('Lavenderblush4', '0xFF8B8386'), ('Lawngreen', '0xFF7CFC00'), ('Lemonchiffon1', '0xFFFFFACD'), "
    		+ "('Lemonchiffon2', '0xFFEEE9BF'), ('Lemonchiffon3', '0xFFCDC9A5'), ('Lemonchiffon4', '0xFF8B8970'), "
    		+ "('Light', '0xFFEEDD82'), ('Lightblue', '0xFFADD8E6'), ('Lightblue1', '0xFFBFEFFF'), "
    		+ "('Lightblue2', '0xFFB2DFEE'), ('Lightblue3', '0xFF9AC0CD'), ('Lightblue4', '0xFF68838B'), "
    		+ "('Lightcoral', '0xFFF08080'), ('Lightcyan1', '0xFFE0FFFF'), ('Lightcyan2', '0xFFD1EEEE'), "
    		+ "('Lightcyan3', '0xFFB4CDCD'), ('Lightcyan4', '0xFF7A8B8B'), ('Lightgoldenrod1', '0xFFFFEC8B'), "
    		+ "('Lightgoldenrod2', '0xFFEEDC82'), ('Lightgoldenrod3', '0xFFCDBE70'), ('Lightgoldenrod4', '0xFF8B814C'), "
    		+ "('Lightgoldenrodyellow', '0xFFFAFAD2'), ('Lightgray', '0xFFD3D3D3'), ('Lightpink', '0xFFFFB6C1'), "
    		+ "('Lightpink1', '0xFFFFAEB9'), ('Lightpink2', '0xFFEEA2AD'), ('Lightpink3', '0xFFCD8C95'), "
    		+ "('Lightpink4', '0xFF8B5F65'), ('Lightsalmon1', '0xFFFFA07A'), ('Lightsalmon2', '0xFFEE9572'), "
    		+ "('Lightsalmon3', '0xFFCD8162'), ('Lightsalmon4', '0xFF8B5742'), ('Lightseagreen', '0xFF20B2AA'), "
    		+ "('Lightskyblue', '0xFF87CEFA'), ('Lightskyblue1', '0xFFB0E2FF'), ('Lightskyblue2', '0xFFA4D3EE'), "
    		+ "('Lightskyblue3', '0xFF8DB6CD'), ('Lightskyblue4', '0xFF607B8B'), ('Lightslateblue', '0xFF8470FF'), "
    		+ "('Lightslategray', '0xFF778899'), ('Lightsteelblue', '0xFFB0C4DE'), ('Lightsteelblue1', '0xFFCAE1FF'), "
    		+ "('Lightsteelblue2', '0xFFBCD2EE'), ('Lightsteelblue3', '0xFFA2B5CD'), ('Lightsteelblue4', '0xFF6E7B8B'), "
    		+ "('Lightyellow1', '0xFFFFFFE0'), ('Lightyellow2', '0xFFEEEED1'), ('Lightyellow3', '0xFFCDCDB4'), "
    		+ "('Lightyellow4', '0xFF8B8B7A'), ('Limegreen', '0xFF32CD32'), ('Linen', '0xFFFAF0E6'), "
    		+ "('Magenta', '0xFFFF00FF'), ('Magenta2', '0xFFEE00EE'), ('Magenta3', '0xFFCD00CD'), "
    		+ "('Magenta4', '0xFF8B008B'), ('Maroon', '0xFFB03060'), ('Maroon1', '0xFFFF34B3'), "
    		+ "('Maroon2', '0xFFEE30A7'), ('Maroon3', '0xFFCD2990'), ('Maroon4', '0xFF8B1C62'), "
    		+ "('Mediumaquamarine', '0xFF66CDAA'), ('Mediumblue', '0xFF0000CD'), ('Mediumorchid', '0xFFBA55D3'), "
    		+ "('Mediumorchid1', '0xFFE066FF'), ('Mediumorchid2', '0xFFD15FEE'), ('Mediumorchid3', '0xFFB452CD'), "
    		+ "('Mediumorchid4', '0xFF7A378B'), ('Mediumpurple', '0xFF9370DB'), ('Mediumpurple1', '0xFFAB82FF'), "
    		+ "('Mediumpurple2', '0xFF9F79EE'), ('Mediumpurple3', '0xFF8968CD'), ('Mediumpurple4', '0xFF5D478B')";

    private static final String INSERT_INTO_TABLE_PALETTE4 = "INSERT INTO " + TABLE_PALETTE
    		+ "(" + COLOR_NAME + ", " + HTML_CODE + ") VALUES " 	
    		+ "('Mediumseagreen', '0xFF3CB371'), ('Mediumslateblue', '0xFF7B68EE'), ('Mediumspringgreen', '0xFF00FA9A'), "
    		+ "('Mediumturquoise', '0xFF48D1CC'), ('Mediumvioletred', '0xFFC71585'), ('Midnightblue', '0xFF191970'), "
    		+ "('Mintcream', '0xFFF5FFFA'), ('Mistyrose1', '0xFFFFE4E1'), ('Mistyrose2', '0xFFEED5D2'), "
    		+ "('Mistyrose3', '0xFFCDB7B5'), ('Mistyrose4', '0xFF8B7D7B'), ('Moccasin', '0xFFFFE4B5'), "
    		+ "('Navajowhite1', '0xFFFFDEAD'), ('Navajowhite2', '0xFFEECFA1'), ('Navajowhite3', '0xFFCDB38B'), "
    		+ "('Navajowhite4', '0xFF8B795E'), ('Navyblue', '0xFF000080'), ('Oldlace', '0xFFFDF5E6'), "
    		+ "('Olivedrab', '0xFF6B8E23'), ('Olivedrab1', '0xFFC0FF3E'), ('Olivedrab2', '0xFFB3EE3A'), "
    		+ "('Olivedrab4', '0xFF698B22'), ('Orange1', '0xFFFFA500'), ('Orange2', '0xFFEE9A00'), "
    		+ "('Orange3', '0xFFCD8500'), ('Orange4', '0xFF8B5A00'), ('Orangered1', '0xFFFF4500'), "
    		+ "('Orangered2', '0xFFEE4000'), ('Orangered3', '0xFFCD3700'), ('Orangered4', '0xFF8B2500'), "
    		+ "('Orchid', '0xFFDA70D6'), ('Orchid1', '0xFFFF83FA'), ('Orchid2', '0xFFEE7AE9'), "
    		+ "('Orchid3', '0xFFCD69C9'), ('Orchid4', '0xFF8B4789'), ('Pale', '0xFFDB7093'), "
    		+ "('Palegoldenrod', '0xFFEEE8AA'), ('Palegreen', '0xFF98FB98'), ('Palegreen1', '0xFF9AFF9A'), "
    		+ "('Palegreen2', '0xFF90EE90'), ('Palegreen3', '0xFF7CCD7C'), ('Palegreen4', '0xFF548B54'), "
    		+ "('Paleturquoise', '0xFFAFEEEE'), ('Paleturquoise1', '0xFFBBFFFF'), ('Paleturquoise2', '0xFFAEEEEE'), "
    		+ "('Paleturquoise3', '0xFF96CDCD'), ('Paleturquoise4', '0xFF668B8B'), ('Palevioletred1', '0xFFFF82AB'), "
    		+ "('Palevioletred2', '0xFFEE799F'), ('Palevioletred3', '0xFFCD6889'), ('Palevioletred4', '0xFF8B475D'), "
    		+ "('Papayawhip', '0xFFFFEFD5'), ('Peachpuff1', '0xFFFFDAB9'), ('Peachpuff2', '0xFFEECBAD'), "
    		+ "('Peachpuff3', '0xFFCDAF95'), ('Peachpuff4', '0xFF8B7765'), ('Pink', '0xFFFFC0CB'), "
    		+ "('Pink1', '0xFFFFB5C5'), ('Pink2', '0xFFEEA9B8'), ('Pink3', '0xFFCD919E'), "
    		+ "('Pink4', '0xFF8B636C'), ('Plum', '0xFFDDA0DD'), ('Plum1', '0xFFFFBBFF'), "
    		+ "('Plum2', '0xFFEEAEEE'), ('Plum3', '0xFFCD96CD'), ('Plum4', '0xFF8B668B'), "
    		+ "('Powderblue', '0xFFB0E0E6'), ('Purple', '0xFFA020F0'), ('Purple1', '0xFF9B30FF'), "
    		+ "('Purple2', '0xFF912CEE'), ('Purple3', '0xFF7D26CD'), ('Purple4', '0xFF551A8B'), "
    		+ "('Rebeccapurple', '0xFF663399'), ('Red1', '0xFFFF0000'), ('Red2', '0xFFEE0000'), "
    		+ "('Red3', '0xFFCD0000'), ('Red4', '0xFF8B0000'), ('Rosybrown', '0xFFBC8F8F'), "
    		+ "('Rosybrown1', '0xFFFFC1C1'), ('Rosybrown2', '0xFFEEB4B4'), ('Rosybrown3', '0xFFCD9B9B'), "
    		+ "('Rosybrown4', '0xFF8B6969'), ('Royalblue', '0xFF4169E1'), ('Royalblue1', '0xFF4876FF'), "
    		+ "('Royalblue2', '0xFF436EEE'), ('Royalblue3', '0xFF3A5FCD'), ('Royalblue4', '0xFF27408B'), "
    		+ "('Saddlebrown', '0xFF8B4513'), ('Salmon', '0xFFFA8072'), ('Salmon1', '0xFFFF8C69'), "
    		+ "('Salmon2', '0xFFEE8262'), ('Salmon3', '0xFFCD7054'), ('Salmon4', '0xFF8B4C39'), "
    		+ "('Sandybrown', '0xFFF4A460'), ('Seagreen1', '0xFF54FF9F'), ('Seagreen2', '0xFF4EEE94'), "
    		+ "('Seagreen3', '0xFF43CD80'), ('Seagreen4', '0xFF2E8B57'), ('Seashell1', '0xFFFFF5EE'), "
    		+ "('Seashell2', '0xFFEEE5DE'), ('Seashell3', '0xFFCDC5BF'), ('Seashell4', '0xFF8B8682'), "
    		+ "('Sienna', '0xFFA0522D'), ('Sienna1', '0xFFFF8247'), ('Sienna2', '0xFFEE7942'), "
    		+ "('Sienna3', '0xFFCD6839'), ('Sienna4', '0xFF8B4726'), ('Skyblue', '0xFF87CEEB'), "
    		+ "('Skyblue1', '0xFF87CEFF'), ('Skyblue2', '0xFF7EC0EE'), ('Skyblue3', '0xFF6CA6CD'), "
    		+ "('Skyblue4', '0xFF4A708B'), ('Slateblue', '0xFF6A5ACD'), ('Slateblue1', '0xFF836FFF'), "
    		+ "('Slateblue2', '0xFF7A67EE'), ('Slateblue3', '0xFF6959CD'), ('Slateblue4', '0xFF473C8B'), "
    		+ "('Slategray', '0xFF708090'), ('Slategray1', '0xFFC6E2FF'), ('Slategray2', '0xFFB9D3EE')";

    private static final String INSERT_INTO_TABLE_PALETTE5 = "INSERT INTO " + TABLE_PALETTE
    		+ "(" + COLOR_NAME + ", " + HTML_CODE + ") VALUES " 	
    		+ "('Slategray3', '0xFF9FB6CD'), ('Slategray4', '0xFF6C7B8B'), ('Snow1', '0xFFFFFAFA'), "
    		+ "('Snow2', '0xFFEEE9E9'), ('Snow3', '0xFFCDC9C9'), ('Snow4', '0xFF8B8989'), "
    		+ "('Springgreen1', '0xFF00FF7F'), ('Springgreen2', '0xFF00EE76'), ('Springgreen3', '0xFF00CD66'), "
    		+ "('Springgreen4', '0xFF008B45'), ('Steelblue', '0xFF4682B4'), ('Steelblue1', '0xFF63B8FF'), "
    		+ "('Steelblue2', '0xFF5CACEE'), ('Steelblue3', '0xFF4F94CD'), ('Steelblue4', '0xFF36648B'), "
    		+ "('Tan', '0xFFD2B48C'), ('Tan1', '0xFFFFA54F'), ('Tan2', '0xFFEE9A49'), "
    		+ "('Tan3', '0xFFCD853F'), ('Tan4', '0xFF8B5A2B'), ('Thistle', '0xFFD8BFD8'), "
    		+ "('Thistle1', '0xFFFFE1FF'), ('Thistle2', '0xFFEED2EE'), ('Thistle3', '0xFFCDB5CD'), "
    		+ "('Thistle4', '0xFF8B7B8B'), ('Tomato1', '0xFFFF6347'), ('Tomato2', '0xFFEE5C42'), "
    		+ "('Tomato3', '0xFFCD4F39'), ('Tomato4', '0xFF8B3626'), ('Turquoise', '0xFF40E0D0'), "
    		+ "('Turquoise1', '0xFF00F5FF'), ('Turquoise2', '0xFF00E5EE'), ('Turquoise3', '0xFF00C5CD'), "
    		+ "('Turquoise4', '0xFF00868B'), ('Violet', '0xFFEE82EE'), ('Violetred', '0xFFD02090'), "
    		+ "('Violetred1', '0xFFFF3E96'), ('Violetred2', '0xFFEE3A8C'), ('Violetred3', '0xFFCD3278'), "
    		+ "('Violetred4', '0xFF8B2252'), ('Wheat', '0xFFF5DEB3'), ('Wheat1', '0xFFFFE7BA'), "
    		+ "('Wheat2', '0xFFEED8AE'), ('Wheat3', '0xFFCDBA96'), ('Wheat4', '0xFF8B7E66'), "
    		+ "('White', '0xFFFFFFFF'), ('Whitesmoke', '0xFFF5F5F5'), ('Yellow1', '0xFFFFFF00'), "
    		+ "('Yellow2', '0xFFEEEE00'), ('Yellow3', '0xFFCDCD00'), ('Yellow4', '0xFF8B8B00'), "
    		+ "('Yellowgreen', '0xFF9ACD32')";
    		
    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        
        //This line is for debugging only REMOVE OR COMMENT OUT for production
        //ctx.deleteDatabase(DATABASE_NAME);
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
    	    	  	
        // Create required tables
        db.execSQL(CREATE_TABLE_PALETTE);
        db.execSQL(CREATE_TABLE_THEME);
        db.execSQL(CREATE_TABLE_GROUP);
        db.execSQL(CREATE_TABLE_NOTE);
        db.execSQL(CREATE_VIRTUAL_TABLE_NOTE);
        
        //Create required triggers
        db.execSQL(CREATE_TRIGGER_DELETE);
        db.execSQL(CREATE_TRIGGER_INSERT);
        db.execSQL(CREATE_TRIGGER_UPDT_SUBJ);
		db.execSQL(CREATE_TRIGGER_UPDT_GROUP);
		db.execSQL(CREATE_TRIGGER_UPDT_THEME);
        
        //Populate with default tuples
        db.execSQL(INSERT_INTO_TABLE_PALETTE1);
        db.execSQL(INSERT_INTO_TABLE_PALETTE2);
        db.execSQL(INSERT_INTO_TABLE_PALETTE3);
        db.execSQL(INSERT_INTO_TABLE_PALETTE4);
        db.execSQL(INSERT_INTO_TABLE_PALETTE5);
        db.execSQL(INSERT_INTO_TABLE_THEME);
        db.execSQL(INSERT_INTO_TABLE_GROUP);
        
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       
    	// on upgrade drop older tables
    	db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE_FTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_THEME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PALETTE);
        
        //on upgrade drop older triggers
        db.execSQL("DROP TRIGGER IF EXISTS " + TRIGGER_DELETE);
        db.execSQL("DROP TRIGGER IF EXISTS " + TRIGGER_INSERT);
        db.execSQL("DROP TRIGGER IF EXISTS " + TRIGGER_UPDT_SUB);
		db.execSQL("DROP TRIGGER IF EXISTS " + TRIGGER_UPDT_GROUP);
		db.execSQL("DROP TRIGGER IF EXISTS " + TRIGGER_UPDT_THEME);
 
        // create new tables
        onCreate(db);
    }
    
	//get current time from device
    private static String getCDT(){
    	DateFormat dateFormat = new SimpleDateFormat("MM:dd:yyyy HH:mm:ss");
    	Date date = new Date();
		return dateFormat.format(date);
    }
    
    //-----------------------------------Create Tuple-----------------------------------------------
    
    //Create Note
  	void insertNote(Note Note) {
  		SQLiteDatabase db = this.getWritableDatabase();  		
  		
  		ContentValues initialValues = new ContentValues();
  		initialValues.put(SUBJECT, Note.getSubject());
  		initialValues.put(GROUP_NAME, Note.getGroup_Name());
  		initialValues.put(NOTE_TEXT, Note.getNote_Text());
  		initialValues.put(RDT, Note.getReminder_Date_Time());
  		initialValues.put(UDT, getCDT());
  			
  		db.insert(TABLE_NOTE, null, initialValues);
  		db.close();
  	}
  	
  	//Create Theme
  	void insertTheme(Theme Theme) {
  		SQLiteDatabase db = this.getWritableDatabase();  		
  		
  		ContentValues initialValues = new ContentValues();
  		initialValues.put(THEME_NAME, Theme.getTheme_Name());
  		initialValues.put(BACK_COLOR, Theme.getBackground_Color());
  		initialValues.put(TEXT_COLOR, Theme.getText_ColorTheme());
  		initialValues.put(HIGH_COLOR, Theme.getHighlight_Color());

  		db.insert(TABLE_THEME, null, initialValues);
  		db.close();
  	}
  	    
  	//Create Group
    void insertGroup(Group Group) {
    	SQLiteDatabase db = this.getWritableDatabase();
    	
		ContentValues initialValues = new ContentValues();
		initialValues.put(GROUP_NAME, Group.getGroup_Name());
		initialValues.put(THEME_NAME, Group.getTheme_Name());
		
		db.insert(TABLE_GROUP, null, initialValues);
		db.close();
    }

  //-------------------------------------Read Tuple-------------------------------------------------
    
    //Reads all Theme names from Theme_Table
  	Cursor fetchAllThemeNames(){
  		SQLiteDatabase db = this.getWritableDatabase();
  		
  		return db.query(TABLE_THEME, 
  				new String[] {THEME_NAME}, null, null, null, null, null);
  	}
  	
  	//Reads Scheme belonging to a Theme
  	Cursor fetchThemeScheme(String ThemeName){
  		SQLiteDatabase db = this.getWritableDatabase();
  		
  		return db.query(TABLE_THEME, 
  				new String[] {BACK_COLOR, TEXT_COLOR, 
  				HIGH_COLOR},
  				THEME_NAME + " = '" + ThemeName + "'", null, null, null, null);
  	}
  	
  	//Reads all Group names from Group_Table
  	Cursor fetchAllGroupNames(){
  		SQLiteDatabase db = this.getWritableDatabase();
  		
  		return db.query(TABLE_GROUP, 
  				new String[] {GROUP_NAME}, null, null, null, null, null);
  	}
  	
  	//Reads theme of a Group names from Group_Table
  	Cursor fetchAGroupThemeName(String GroupName){
  		SQLiteDatabase db = this.getWritableDatabase();
  		
  		return db.query(TABLE_GROUP, 
  				new String [] {THEME_NAME}, GROUP_NAME
  				+ " = '" + GroupName + "'", null, null, null, null);
  	}
  	
  	//Reads all note previews for all preview layouts and sorts
  	Cursor fetchNotePreview(String GroupName, String Sort){
  		SQLiteDatabase db = this.getWritableDatabase();

  	    String query = "SELECT " + NOTE_ID + ", " + SUBJECT + ", B." + HTML_CODE
  	    				+ " as Bcode, T." + HTML_CODE + " as Tcode, HI." + HTML_CODE
  	    				+ " as Hcode FROM " + TABLE_NOTE
  	    				+ ", " + TABLE_GROUP + ", " + TABLE_THEME 
  	    				+ ", (SELECT "  + THEME_NAME	+ ", " + HTML_CODE 
  	    				+ " FROM " + TABLE_THEME + ", " + TABLE_PALETTE
  	    				+ " Where " + BACK_COLOR + " = " + COLOR_NAME 
  						+ ") B, (SELECT " + THEME_NAME + ", " + HTML_CODE
  						+ " FROM " + TABLE_THEME + ", " + TABLE_PALETTE
  						+" Where " + TEXT_COLOR + " = " + COLOR_NAME
  						+ ") T, (SELECT " + THEME_NAME + ", " + HTML_CODE
  						+ " FROM " + TABLE_THEME + ", " + TABLE_PALETTE
  						+ " Where " + HIGH_COLOR + " = " + COLOR_NAME
  						+ ") HI WHERE " + TABLE_NOTE + "." + GROUP_NAME
  						+ " = " + TABLE_GROUP + "." + GROUP_NAME	
  						+ " and " + TABLE_GROUP + "." + THEME_NAME
  						+ " = " + TABLE_THEME  + "." + THEME_NAME
  						+ " and B."  + THEME_NAME + " = " + TABLE_THEME
  						+ "." + THEME_NAME + " and T." + THEME_NAME 
  						+ " = " + TABLE_THEME + "." + THEME_NAME
  						+ " and HI." + THEME_NAME + " = " + TABLE_THEME
  						+ "." + THEME_NAME;
  	    
  	    //Adds Group name to WHERE Clause for Group layouts
  	    if (!GroupName.equals("All Notes")){
  	    	query = query + " and " + TABLE_NOTE + "." 
  	    			+ GROUP_NAME + " = '" + GroupName + "'";
  	    }
  	    
  	    //Adds ORDER BY when sorting
		switch (Sort) {
			case "AZ":
				//A-Z
				query = query + " ORDER BY " + SUBJECT;
				break;
			case "ZA":
				//Z-A
				query = query + " ORDER BY " + SUBJECT + " DESC";
				break;
			case "GAZ":
				// Group A-Z
				query = query + " ORDER BY " + TABLE_GROUP + "." + GROUP_NAME;
				break;
			case "GZA":
				// Group Z-A
				query = query + " ORDER BY " + TABLE_GROUP + "." + GROUP_NAME + " DESC";
				break;
			case "UAZ":
				// Updated Date-Time A-Z
				query = query + " ORDER BY " + UDT;
				break;
			case "UZA":
				// Updated Date-Time Z-A
				query = query + " ORDER BY " + UDT + " DESC";
				break;
		}

  		return db.rawQuery(query, null);
  	}
  	
  //Reads all group previews for all preview layouts and sorts
  	Cursor fetchGroupPreview(){
  		SQLiteDatabase db = this.getWritableDatabase();

  	    String query = "SELECT " + GROUP_NAME + ", B." + HTML_CODE
  	    				+ ", T." + HTML_CODE + ", HI." + HTML_CODE
  	    				+ " FROM " + TABLE_GROUP + ", " + TABLE_THEME 
  	    				+ ", (SELECT "  + THEME_NAME	+ ", " + HTML_CODE 
  	    				+ " FROM " + TABLE_THEME + ", " + TABLE_PALETTE
  	    				+ " Where " + BACK_COLOR + " = " + COLOR_NAME 
  						+ ") B, (SELECT " + THEME_NAME + ", " + HTML_CODE
  						+ " FROM " + TABLE_THEME + ", " + TABLE_PALETTE
  						+" Where " + TEXT_COLOR + " = " + COLOR_NAME
  						+ ") T, (SELECT " + THEME_NAME + ", " + HTML_CODE
  						+ " FROM " + TABLE_THEME + ", " + TABLE_PALETTE
  						+ " Where " + HIGH_COLOR + " = " + COLOR_NAME
  						+ ") HI WHERE " + TABLE_GROUP + "." + THEME_NAME
  						+ " = " + TABLE_THEME  + "." + THEME_NAME
  						+ " and B."  + THEME_NAME + " = " + TABLE_THEME
  						+ "." + THEME_NAME + " and T." + THEME_NAME 
  						+ " = " + TABLE_THEME + "." + THEME_NAME
  						+ " and HI." + THEME_NAME + " = " + TABLE_THEME
  						+ "." + THEME_NAME  + " ORDER BY " + TABLE_GROUP 
  						+ "." + GROUP_NAME;

  		return db.rawQuery(query, null);
  	}
  	
  //Reads all theme previews for all preview layouts and sorts
  	Cursor fetchThemePreview(){
  		SQLiteDatabase db = this.getWritableDatabase();

  	    String query = "SELECT " + TABLE_THEME + "." + THEME_NAME 
  	    				+ ", B." + HTML_CODE + ", T." + HTML_CODE 
  	    				+ ", HI." + HTML_CODE + " FROM " + TABLE_THEME 
  	    				+ ", (SELECT "  + THEME_NAME + ", " + HTML_CODE 
  	    				+ " FROM " + TABLE_THEME + ", " + TABLE_PALETTE
  	    				+ " Where " + BACK_COLOR + " = " + COLOR_NAME 
  						+ ") B, (SELECT " + THEME_NAME + ", " + HTML_CODE
  						+ " FROM " + TABLE_THEME + ", " + TABLE_PALETTE
  						+" Where " + TEXT_COLOR + " = " + COLOR_NAME
  						+ ") T, (SELECT " + THEME_NAME + ", " + HTML_CODE
  						+ " FROM " + TABLE_THEME + ", " + TABLE_PALETTE
  						+ " Where " + HIGH_COLOR + " = " + COLOR_NAME
  						+ ") HI WHERE B."  + THEME_NAME + " = " + TABLE_THEME
  						+ "." + THEME_NAME + " and T." + THEME_NAME 
  						+ " = " + TABLE_THEME + "." + THEME_NAME
  						+ " and HI." + THEME_NAME + " = " + TABLE_THEME
  						+ "." + THEME_NAME  + " ORDER BY " + TABLE_THEME 
  						+ "." + THEME_NAME;

  		return db.rawQuery(query, null);
  	}
  	
   //Reads a selected theme 
  	Cursor fetchTheme(String Theme){
  		SQLiteDatabase db = this.getWritableDatabase();

  	    String query = "SELECT B." + HTML_CODE
  	    				+ ", T." + HTML_CODE + ", HI." + HTML_CODE
  	    				+ " FROM " + TABLE_THEME 
  	    				+ ", (SELECT "  + THEME_NAME + ", " + HTML_CODE 
  	    				+ " FROM " + TABLE_THEME + ", " + TABLE_PALETTE
  	    				+ " Where " + BACK_COLOR + " = " + COLOR_NAME
  						+ ") B, (SELECT " + THEME_NAME + ", " + HTML_CODE
  						+ " FROM " + TABLE_THEME + ", " + TABLE_PALETTE
  						+" Where " + TEXT_COLOR + " = " + COLOR_NAME
  						+ ") T, (SELECT " + THEME_NAME + ", " + HTML_CODE
  						+ " FROM " + TABLE_THEME + ", " + TABLE_PALETTE
  						+ " Where " + HIGH_COLOR + " = " + COLOR_NAME
  						+ ") HI WHERE B." + THEME_NAME + " = '" + Theme + "'"
						+ " and T." + THEME_NAME + " = '" + Theme + "'"
  						+ " and HI." + THEME_NAME + " = '" + Theme + "'";

  		return db.rawQuery(query, null);
  	}
  	
  	//Reads a note that matches the requested Subject
  	Cursor fetchNote(int noteID){
  		SQLiteDatabase db = this.getWritableDatabase();

  	    String query = "SELECT " + TABLE_NOTE + ".*, B." + HTML_CODE
  	    				+ " as Bcode, T." + HTML_CODE + " as Tcode, HI." + HTML_CODE + " as Hcode"
  	    				+ " FROM " + TABLE_NOTE 
  	    				+ ", " + TABLE_GROUP	+ ", " + TABLE_THEME 
  	    				+ ", (SELECT "  + THEME_NAME	+ ", " + HTML_CODE 
  	    				+ " FROM " + TABLE_THEME + ", " + TABLE_PALETTE
  	    				+ " Where " + BACK_COLOR + " = " + COLOR_NAME 
  						+ ") B, (SELECT " + THEME_NAME + ", " + HTML_CODE
  						+ " FROM " + TABLE_THEME + ", " + TABLE_PALETTE
  						+" Where " + TEXT_COLOR + " = " + COLOR_NAME
  						+ ") T, (SELECT " + THEME_NAME + ", " + HTML_CODE
  						+ " FROM " + TABLE_THEME + ", " + TABLE_PALETTE
  						+ " Where " + HIGH_COLOR + " = " + COLOR_NAME
  						+ ") HI WHERE " + TABLE_NOTE + "." + GROUP_NAME
  						+ " = " + TABLE_GROUP + "." + GROUP_NAME	
  						+ " and " + TABLE_GROUP + "." + THEME_NAME
  						+ " = " + TABLE_THEME  + "." + THEME_NAME
  						+ " and B."  + THEME_NAME + " = " + TABLE_THEME
  						+ "." + THEME_NAME + " and T." + THEME_NAME 
  						+ " = " + TABLE_THEME + "." + THEME_NAME
  						+ " and HI." + THEME_NAME + " = " + TABLE_THEME
  						+ "." + THEME_NAME + " and " + TABLE_NOTE 
  						+ "." + NOTE_ID	+ " = " + noteID;
  	    
  			return db.rawQuery(query, null);
  	}
  	
  	//Reads all Text for search
  	Cursor fetchSearch(String Search){
  		SQLiteDatabase db = this.getWritableDatabase();

  	    String query = "SELECT rowid as " + KEY_ROWID + ", "  + NOTE_ID + " FROM "
				+ TABLE_NOTE_FTS + " WHERE " + SUBJECT + " LIKE '%" + Search + "%'";
  		
  	return db.rawQuery(query, null);
  	}
  	
  	//Reads all Colors for selection
  	Cursor fetchColor(){
  		SQLiteDatabase db = this.getWritableDatabase();

  		String query = "SELECT * FROM " + TABLE_PALETTE + " ORDER BY " + COLOR_NAME;
  			
  		return db.rawQuery(query, null);
	}
  		
    //---------------------------------------Update Tuple-------------------------------------------
  		
    //Update Note
  	void updateNote(Note Note, int noteID) {
  		SQLiteDatabase db = this.getWritableDatabase();
  		
      	ContentValues updateValues = new ContentValues();
  		updateValues.put(SUBJECT, Note.getSubject());
  		updateValues.put(GROUP_NAME, Note.getGroup_Name());
  		updateValues.put(NOTE_TEXT, Note.getNote_Text());
  		updateValues.put(RDT, Note.getReminder_Date_Time());
  		updateValues.put(UDT, getCDT());

  		db.update(TABLE_NOTE, updateValues, NOTE_ID + " = " + noteID, null);
  		db.close();
  	}
      
  	//Update Group
  	void updateGroup(Group Group, String oldGroup) {
  		SQLiteDatabase db = this.getWritableDatabase();
  		
  	    ContentValues updateValues = new ContentValues();
  	    updateValues.put(GROUP_NAME, Group.getGroup_Name());
  		updateValues.put(THEME_NAME, Group.getTheme_Name());

  		db.update(TABLE_GROUP, updateValues, GROUP_NAME + " = '" + oldGroup + "'",null);
  		db.close();
  	}
      
  	//Update Theme
  	void updateTheme(Theme Theme, String oldTheme) {
  		SQLiteDatabase db = this.getWritableDatabase();
  		
  	    ContentValues updateValues = new ContentValues();
  	    updateValues.put(THEME_NAME, Theme.getTheme_Name());
  	    updateValues.put(BACK_COLOR, Theme.getBackground_Color());
  		updateValues.put(TEXT_COLOR, Theme.getText_ColorTheme());
  		updateValues.put(HIGH_COLOR, Theme.getHighlight_Color());

  		db.update(TABLE_THEME, updateValues, THEME_NAME + " = '" + oldTheme + "'",null);
  		db.close();
  	}
      
  	//Update Note Group_Name to default if Group is deleted
  	private void updateNoteDefault(String OldGroup) {
  		SQLiteDatabase db = this.getWritableDatabase();
  		
  	    ContentValues updateValues = new ContentValues();
  		updateValues.put(GROUP_NAME, "Other Notes");
  		
  		db.update(TABLE_NOTE, updateValues, GROUP_NAME + " = '" + OldGroup + "'", null);
  		db.close();
  	}
  	    
  	//Update Group Theme_Name to default if Theme is deleted
 	private void updateGroupDefault(String OldTheme) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		
  		ContentValues updateValues = new ContentValues();
  		updateValues.put(THEME_NAME, "Default");

  		db.update(TABLE_GROUP, updateValues,	THEME_NAME + " = '" + OldTheme + "'", null);
  		db.close();
 	}
  	
  	//Update Note Group_Name via multiple select
  	void updateNoteMultiple(int noteID, String Group) {
  		SQLiteDatabase db = this.getWritableDatabase();
  		
  	    ContentValues updateValues = new ContentValues();
  		updateValues.put(GROUP_NAME, Group);
  		
  		db.update(TABLE_NOTE, updateValues, NOTE_ID + " = " + noteID, null);
  		db.close();		
  	}
  	    
  	//Update Group Theme_Name via multiple select
  	public void updateGroupMultiple(Group Group) {
  		SQLiteDatabase db = this.getWritableDatabase();
  		
  		ContentValues updateValues = new ContentValues();
  		updateValues.put(THEME_NAME, Group.getTheme_Name());

  		db.update(TABLE_GROUP, updateValues, GROUP_NAME + " = '" + Group.getGroup_Name() + "'", null);
  		db.close();
  	}
  	
  //--------------------------------------Delete Tuple----------------------------------------------
  	
    //Delete Note
  	void deleteNote(int noteID){
  		SQLiteDatabase db = this.getWritableDatabase();
  		
  		db.delete(TABLE_NOTE, NOTE_ID + " = " + noteID, null);
  		db.close();
  	}
  	
  	//Delete Group
  	void deleteGroup(String group){
  		if(!group.equals("Other Notes")){

  			SQLiteDatabase db = this.getWritableDatabase();
  			
  			db.delete(TABLE_GROUP, GROUP_NAME + " = '" + group + "'", null);
  			updateNoteDefault(group);
  			db.close();
  		}
  		
  	}
  	
  	//Delete Theme
  	void deleteTheme(String theme){
 		if(!theme.equals("Default")){
 			
 			SQLiteDatabase db = this.getWritableDatabase();
 			
  			db.delete(TABLE_THEME, THEME_NAME + " = '" + theme + "'", null);
  			updateGroupDefault(theme);
  			db.close();
  		}
  	}
}