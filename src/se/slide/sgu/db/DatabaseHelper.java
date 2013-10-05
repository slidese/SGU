
package se.slide.sgu.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import se.slide.sgu.Utils;
import se.slide.sgu.model.Content;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "SGU.sqlite";
    private static final int DATABASE_VERSION = 2;

    private Dao<Content, Integer> contentDao = null;
    private Context context = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase arg0, ConnectionSource arg1) {
        try {
            TableUtils.createTable(connectionSource, Content.class);
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

            // Execute all changes
            for (String sql : allSql) {
                db.execSQL(sql);
            }

        } catch (SQLException e) {
            Log.e(TAG, "Exception during onUpgrade", e);
            Utils.sendExceptionToGoogleAnalytics(context, Thread.currentThread().getName(), e, false);
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
}
