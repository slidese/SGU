package se.slide.sgu.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
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
    public String transcript;
    
    @DatabaseField 
    public String hosts;    // Semi-colon separated list of host numbers
    
    @DatabaseField 
    public String image;
        
    /**
     * We're saving these separately
     */
    
    public Quote            quote;
    public List<Section>    listOfSection   =   new ArrayList<Section>();   // Podcast sections
    public List<Item>       listOfItem      =   new ArrayList<Item>();      // Science or fiction items
    public List<Guest>      listOfGuests    =   new ArrayList<Guest>();     // Guests on the show
    
    public Episode() {
        
    }
}
