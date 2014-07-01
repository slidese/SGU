package se.slide.sgu.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.List;

@DatabaseTable
public class Section {

    @DatabaseField(generatedId = true)
    private int id;
    
    @DatabaseField
    public String guid;
    
    @DatabaseField
    public String mp3;
    
    @DatabaseField
    public String title;
    
    @DatabaseField
    public int start;
    
    @DatabaseField
    public int number;
    
    /**
     * We're saving these separately
     */
    
    public List<Link>       listOfLinks      =   new ArrayList<Link>();      // Links
    
    public Section() {
        
    }
}
