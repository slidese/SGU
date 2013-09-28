
package se.slide.sgu.model;

import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.File;

import se.slide.sgu.Utils;

@DatabaseTable
public class Content {

    @DatabaseField 
    public String title;
    
    @DatabaseField
    public String description;
    
    @DatabaseField(id = true)
    public String mp3;
    
    public Content() {
        
    }
    
    public Content(String title, String description, String mp3) {
        this.title = title;
        this.description = description;
        this.mp3 = mp3;
    }
    
    public Uri asUri() {
        String filename = Utils.formatFilename(title);
        File file = Utils.getFilepath(filename);
        return Uri.parse(file.getAbsolutePath());
    }
}
