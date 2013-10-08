package se.slide.sgu.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Section {

    @DatabaseField(generatedId = true)
    private int id;
    
    @DatabaseField
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
