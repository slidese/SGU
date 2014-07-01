package se.slide.sgu;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public enum ContentDownloadManager {
    INSTANCE;
    
    private DownloadManager manager;
    
    public void init(Context context) {
        if (manager == null)
            manager = (DownloadManager) context.getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
    }
    
    public Cursor query(Query q) throws Exception {
        if (manager == null) {
            throw new Exception("ContentDownloadManager never initialized");
        }
        
        return manager.query(q);
    }
    
    public long addToDownloadQueue(String url, String title, String description, String filename) throws Exception {
        
        if (manager == null) {
            throw new Exception("ContentDownloadManager never initialized");
        }
        
        Uri uri = Uri.parse(url);
        
        DownloadManager.Request req = new DownloadManager.Request(uri);

        int flags = DownloadManager.Request.NETWORK_WIFI;
        String downloadOver = GlobalContext.INSTANCE.getPreferenceString("download", "1");
        if (downloadOver.equals("0"))
            flags = DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE;
        
        req.setAllowedNetworkTypes(flags)
           .setAllowedOverRoaming(false)
           .setTitle(title)
           .setDescription(description)
           .setDestinationInExternalPublicDir(Utils.DIR_SGU, filename);
        
        long id = manager.enqueue(req);

        return id;
    }
}
