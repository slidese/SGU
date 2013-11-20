package se.slide.sgu.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Guest {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    public String guid;
    
    @DatabaseField
    public String mp3;
    
    @DatabaseField
    public String name;
    
    @DatabaseField
    public String url;
    
    public Guest() {
        
    }
}
