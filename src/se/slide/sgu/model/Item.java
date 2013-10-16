package se.slide.sgu.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Item {

    @DatabaseField(generatedId = true)
    private int id;
    
    @DatabaseField
    public String mp3;
    
    @DatabaseField
    public String title;
    
    @DatabaseField
    public String description;
    
    @DatabaseField
    public String link;
    
    @DatabaseField
    public boolean science;
    
    public Item() {
        
    }
}
