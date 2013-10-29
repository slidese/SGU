package se.slide.sgu;

import android.os.Environment;
import android.os.StrictMode;

import java.io.File;

public class Utils {

    public static final String DIR_SGU = Environment.DIRECTORY_DOWNLOADS + "/sgu/";
    
    public static String formatFilename(String title) {
        int dashIndex = title.indexOf(" -");
        String name = title.substring(0, dashIndex);
        name = name.replace("#", "");
        
        return name + ".mp3";
    }
    
    public static File getBaseStorageDirectory() {
        return Environment.getExternalStoragePublicDirectory(DIR_SGU);
    }
    
    public static File getFilepath(String filename) {
        return Environment.getExternalStoragePublicDirectory(DIR_SGU + filename);
    }
    
    public static void cleanDownloadDirectory() {
        File downloadDir = getBaseStorageDirectory();
        
        for (File file: downloadDir.listFiles())
            file.delete();
    }
    
    public static void setStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectAll()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }
    
    public static int[] convertToIntArray(String delimitedString) {
        if (delimitedString == null)
            return null;
        
        String[] strArray = delimitedString.split(";");
        int[] intArray = new int[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Integer.valueOf(strArray[i]);
        }
        
        return intArray;
    }
    
}
