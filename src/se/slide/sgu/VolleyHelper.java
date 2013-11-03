package se.slide.sgu;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.io.File;

public class VolleyHelper {

    private static RequestQueue mRequestQueue;
    private static ImageLoader mImageLoader;
    
    private static int DISK_IMAGECACHE_SIZE = 1024*1024*10;
    
    private VolleyHelper() {
        // no instances
    }


    static void init(Context context) {
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
        //mImageLoader = new ImageLoader(mRequestQueue, new DiskBitmapCache(getDiskCacheDir(context.getApplicationContext())));
        mImageLoader = new ImageLoader(mRequestQueue, new BitmapLruImageCache(DISK_IMAGECACHE_SIZE));
    }


    public static RequestQueue getRequestQueue() {
        if (mRequestQueue != null) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }


    /**
     * Returns instance of ImageLoader initialized with {@see FakeImageCache} which effectively means
     * that no memory caching is used. This is useful for images that you know that will be show
     * only once.
     * 
     * @return
     */
    public static ImageLoader getImageLoader() {
        if (mImageLoader != null) {
            return mImageLoader;
        } else {
            throw new IllegalStateException("ImageLoader not initialized");
        }
    }
    
    private static File getDiskCacheDir(Context context) {
        final String cachePath = context.getCacheDir().getPath();
        return new File(cachePath);
    }
}
