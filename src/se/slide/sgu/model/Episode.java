package se.slide.sgu.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;

@DatabaseTable
public class Episode {

    @DatabaseField(id = true)
    public String mp3;
    
    @DatabaseField 
    public String title;
    
    @DatabaseField 
    public String description;
    
    @DatabaseField 
    public List<Section> listOfSection;
    
    public Episode() {
        
    }
}
