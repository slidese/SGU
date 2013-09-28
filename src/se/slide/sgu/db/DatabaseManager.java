
package se.slide.sgu.db;

import android.content.Context;

import se.slide.sgu.model.Content;

import java.sql.SQLException;
import java.util.List;

public class DatabaseManager {
    static private DatabaseManager instance;
    private DatabaseHelper helper;

    static public void init(Context ctx) {
        if (instance == null) {
            instance = new DatabaseManager(ctx);
        }
    }

    static public DatabaseManager getInstance() {
        return instance;
    }

    private DatabaseManager(Context ctx) {
        helper = new DatabaseHelper(ctx);
    }

    private DatabaseHelper getHelper() {
        return helper;
    }

    public List<Content> getAllContents() {
        List<Content> listOfContent = null;
        try {
            listOfContent = getHelper().getContentDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listOfContent;
    }

    public void addContent(Content content) {
        try {
            getHelper().getContentDao().createOrUpdate(content);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void addContent(List<Content> listOfContent) {
        try {
            for (Content content : listOfContent) {
                getHelper().getContentDao().createOrUpdate(content);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
