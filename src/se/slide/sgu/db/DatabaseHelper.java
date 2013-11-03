
package se.slide.sgu.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import se.slide.sgu.GlobalContext;
import se.slide.sgu.model.Content;
import se.slide.sgu.model.Episode;
import se.slide.sgu.model.Guest;
import se.slide.sgu.model.Item;
import se.slide.sgu.model.Link;
import se.slide.sgu.model.Quote;
import se.slide.sgu.model.Section;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "SGU.sqlite";
    private static final int DATABASE_VERSION = 6;

    private Dao<Content, Integer> contentDao = null;
    private Dao<Section, Integer> sectionDao = null;
    private Dao<Guest, Integer> guestDao = null;
    private Dao<Item, Integer> itemDao = null;
    private Dao<Quote, Integer> quoteDao = null;
    private Dao<Episode, Integer> episodeDao = null;
    private Dao<Link, Integer> linkDao = null;
    private Context context = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase arg0, ConnectionSource arg1) {
        try {
            TableUtils.createTable(connectionSource, Content.class);
            TableUtils.createTable(connectionSource, Section.class);
            TableUtils.createTable(connectionSource, Guest.class);
            TableUtils.createTable(connectionSource, Item.class);
            TableUtils.createTable(connectionSource, Quote.class);
            TableUtils.createTable(connectionSource, Episode.class);
            TableUtils.createTable(connectionSource, Link.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {

        // http://www.sqlite.org/datatype3.html
        try {
            List<String> allSql = new ArrayList<String>();

            if (oldVersion == 1) {
                allSql.add("alter table Content add column `played` INTEGER");
            }
            else if (oldVersion == 2) {
                TableUtils.createTable(connectionSource, Guest.class);
                TableUtils.createTable(connectionSource, Item.class);
                TableUtils.createTable(connectionSource, Quote.class);
                TableUtils.createTable(connectionSource, Episode.class);
            }
            else if (oldVersion == 3) {
                allSql.add("alter table Episode add column `transcript` TEXT");
            }
            else if (oldVersion == 4) {
                TableUtils.createTable(connectionSource, Link.class);
            }
            else if (oldVersion == 5) {
                allSql.add("alter table Episode add column `image` TEXT");
            }

            // Execute all changes
            for (String sql : allSql) {
                db.execSQL(sql);
            }

        } catch (SQLException e) {
            Log.e(TAG, "Exception during onUpgrade", e);
            GlobalContext.INSTANCE.sendExceptionToGoogleAnalytics("While upgrading database, android SQLException", Thread.currentThread().getName(), e, false);
            throw new RuntimeException(e);
        } catch (java.sql.SQLException e) {
            Log.e(TAG, "Exception during onUpgrade", e);
            GlobalContext.INSTANCE.sendExceptionToGoogleAnalytics("While upgrading database, java sql SQLException", Thread.currentThread().getName(), e, false);
            throw new RuntimeException(e);
        }
    }

    public Dao<Content, Integer> getContentDao() {
        if (contentDao == null) {
            try {
                contentDao = getDao(Content.class);
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        return contentDao;
    }
    
    public Dao<Section, Integer> getSectionDao() {
        if (sectionDao == null) {
            try {
                sectionDao = getDao(Section.class);
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        return sectionDao;
    }
    
    public Dao<Guest, Integer> getGuestDao() {
        if (guestDao == null) {
            try {
                guestDao = getDao(Guest.class);
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        return guestDao;
    }
    
    public Dao<Item, Integer> getItemDao() {
        if (itemDao == null) {
            try {
                itemDao = getDao(Item.class);
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        return itemDao;
    }
    
    public Dao<Quote, Integer> getQuoteDao() {
        if (quoteDao == null) {
            try {
                quoteDao = getDao(Quote.class);
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        return quoteDao;
    }
    
    public Dao<Episode, Integer> getEpisodeDao() {
        if (episodeDao == null) {
            try {
                episodeDao = getDao(Episode.class);
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        return episodeDao;
    }
    
    public Dao<Link, Integer> getLinkDao() {
        if (linkDao == null) {
            try {
                linkDao = getDao(Link.class);
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        return linkDao;
    }
}
