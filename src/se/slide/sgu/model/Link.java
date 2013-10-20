package se.slide.sgu.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Link {
    
    public static final String BELONG_TO_SCIENCE_OR_FICTION       = "scienceorfiction";
    public static final String BELONG_TO_SECTION                  = "sections";

    @DatabaseField(generatedId = true)
    private int id;
    
    @DatabaseField
    public String mp3;
    
    @DatabaseField
    public String title;
    
    @DatabaseField
    public String description;
    
    @DatabaseField
    public String url;
    
    @DatabaseField
    public String belongsToSection;
    
    @DatabaseField
    public int number;
}
