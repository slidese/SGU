package se.slide.sgu.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Section {

    @DatabaseField(id = true)
    public String mp3;
    
    @DatabaseField 
    public String title;
    
    @DatabaseField
    public int start;
    
    @DatabaseField
    public int number;
    
    public Section() {
        
    }
}
