package se.slide.sgu.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Quote {

    @DatabaseField(generatedId = true)
    private int id;
    
    @DatabaseField
    public String mp3;
    
    @DatabaseField
    public String text;
    
    @DatabaseField
    public String by;
    
    public Quote() {
        
    }
}
