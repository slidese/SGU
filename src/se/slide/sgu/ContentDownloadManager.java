package se.slide.sgu;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;

public enum ContentDownloadManager {
    INSTANCE;
    
    private DownloadManager manager;
    
    public void init(Context context) {
        if (manager == null)
            manager = (DownloadManager) context.getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
    }
    
    public long addToDownloadQueue(String url, String title, String description, String filename) throws Exception {
        
        if (manager == null) {
            throw new Exception("ContentDownloadManager never initialized");
        }
        
        Uri uri = Uri.parse(url);
        
        DownloadManager.Request req = new DownloadManager.Request(uri);

        req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
           .setAllowedOverRoaming(false)
           .setTitle(title)
           .setDescription(description)
           .setDestinationInExternalPublicDir(Utils.DIR_SGU, filename);
        
        long id = manager.enqueue(req);

        return id;
    }
}
