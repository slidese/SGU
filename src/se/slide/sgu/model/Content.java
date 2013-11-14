
package se.slide.sgu.model;

import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import se.slide.sgu.Utils;

import java.io.File;
import java.util.Date;

@DatabaseTable
public class Content {

    @DatabaseField 
    public String title;
    
    @DatabaseField
    public String description;
    
    @DatabaseField(id = true)
    public String mp3;
    
    @DatabaseField
    public Date published;
    
    @DatabaseField
    public int length;
    
    @DatabaseField
    public String guid;
    
    @DatabaseField
    public int duration;
    
    @DatabaseField
    public boolean played;
    
    @DatabaseField
    public int elapsed;
    
    /**
     * These are not stored
     */
    public long downloadId = -1L;
    public float downloadProgress = 0f;
    public float downloadProgressOld = 0f;
    public boolean exists = false;
    public int downloadStatus = -1;
    public String image;
    public String friendlyTitle;
    public boolean isPlaying = false;
    public boolean isPaused = false;
    public boolean dirty = false;
    
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
    
    public String getFilename() {
        int dashIndex = title.indexOf(" -");
        String name = title.substring(0, dashIndex);
        name = name.replace("#", "");
        
        return name + ".mp3";
    }
}
