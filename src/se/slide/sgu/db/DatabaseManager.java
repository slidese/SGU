
package se.slide.sgu.db;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.table.TableUtils;

import se.slide.sgu.model.Content;
import se.slide.sgu.model.Episode;
import se.slide.sgu.model.Guest;
import se.slide.sgu.model.Item;
import se.slide.sgu.model.Link;
import se.slide.sgu.model.Quote;
import se.slide.sgu.model.Section;

import java.sql.SQLException;
import java.util.List;

public class DatabaseManager {
    
    private final String TAG = "DatabaseManager";
    
    static private DatabaseManager instance;
    private DatabaseHelper helper;

    static public void init(Context ctx) {
        if (instance == null) {
            instance = new DatabaseManager(ctx.getApplicationContext());
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
    
    public List<Content> getContent(String mp3) {
        List<Content> listOfContent = null;
        try {
            listOfContent = getHelper().getContentDao().query(getHelper().getContentDao().queryBuilder().where().like("mp3", mp3).prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listOfContent;
    }

    public List<Content> getPremiumContents() {
        List<Content> listOfContent = null;
        try {
            listOfContent = getHelper().getContentDao().query(getHelper().getContentDao().queryBuilder().orderBy("published", false).where().like("title", "%SGU Premium%").prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listOfContent;
    }
    
    public List<Content> getAdFreeContents() {
        List<Content> listOfContent = null;
        try {
            listOfContent = getHelper().getContentDao().query(getHelper().getContentDao().queryBuilder().orderBy("published", false).where().like("title", "%The Skeptics Guide%").prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listOfContent;
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

    public void createOrUpdateContent(Content content) {
        try {
            getHelper().getContentDao().createOrUpdate(content);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void createOrUpdateContents(List<Content> listOfContent) {
        try {
            for (Content content : listOfContent) {
                getHelper().getContentDao().createOrUpdate(content);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void createIfNotExistsContents(List<Content> listOfContent) {
        try {
            for (Content content : listOfContent) {
                getHelper().getContentDao().createIfNotExists(content);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Section
     */
    
    public List<Section> getSection(String mp3) {
        List<Section> listOfSection = null;
        try {
            listOfSection = getHelper().getSectionDao().query(getHelper().getSectionDao().queryBuilder().where().like("mp3", mp3).prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listOfSection;
    }
    
    public void addSections(List<Section> listOfSection) {
        try {
            for (Section section : listOfSection) {
                getHelper().getSectionDao().createOrUpdate(section);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void removeSections() {
        Log.d(TAG, "Remove sections");
        
        try {
            TableUtils.clearTable(helper.getConnectionSource(), Section.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Guest
     */
    
    public List<Guest> getGuest(String mp3) {
        List<Guest> listOfGuest = null;
        try {
            listOfGuest = getHelper().getGuestDao().query(getHelper().getGuestDao().queryBuilder().where().like("mp3", mp3).prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listOfGuest;
    }
    
    public void addGuests(List<Guest> listOfGuest) {
        try {
            for (Guest guest : listOfGuest) {
                getHelper().getGuestDao().createOrUpdate(guest);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void removeGuests() {
        Log.d(TAG, "Remove guests");
        
        try {
            TableUtils.clearTable(helper.getConnectionSource(), Guest.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Item
     */
    
    public List<Item> getItem(String mp3) {
        List<Item> listOfItem = null;
        try {
            listOfItem = getHelper().getItemDao().query(getHelper().getItemDao().queryBuilder().where().like("mp3", mp3).prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listOfItem;
    }
    
    public void addItems(List<Item> listOfItem) {
        try {
            for (Item item : listOfItem) {
                getHelper().getItemDao().createOrUpdate(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void removeItems() {
        Log.d(TAG, "Remove items");
        
        try {
            TableUtils.clearTable(helper.getConnectionSource(), Item.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Quote
     */
    
    public List<Quote> getQuote(String mp3) {
        List<Quote> listOfQuote = null;
        try {
            listOfQuote = getHelper().getQuoteDao().query(getHelper().getQuoteDao().queryBuilder().where().like("mp3", mp3).prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listOfQuote;
    }
    
    public void addQuotes(List<Quote> listOfQuote) {
        try {
            for (Quote quote : listOfQuote) {
                getHelper().getQuoteDao().createOrUpdate(quote);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void addQuote(Quote quote) {
        try {
            getHelper().getQuoteDao().createOrUpdate(quote);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void removeQuotes() {
        Log.d(TAG, "Remove quotes");
        
        try {
            TableUtils.clearTable(helper.getConnectionSource(), Quote.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Episode
     */
    
    public Episode getEpisode(String mp3) {
        
        Episode episode = null;
        try {
            List<Episode> listOfEpisode = getHelper().getEpisodeDao().query(getHelper().getEpisodeDao().queryBuilder().where().like("mp3", mp3).prepare());
            if (listOfEpisode != null && !listOfEpisode.isEmpty())
                episode = listOfEpisode.get(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return episode;
    }
    
    public List<Episode> getEpisodes(String mp3) {
        List<Episode> listOfEpisode = null;
        try {
            listOfEpisode = getHelper().getEpisodeDao().query(getHelper().getEpisodeDao().queryBuilder().where().like("mp3", mp3).prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listOfEpisode;
    }
    
    public void addEpisodes(List<Episode> listOfEpisode) {
        try {
            for (Episode episode : listOfEpisode) {
                getHelper().getEpisodeDao().createOrUpdate(episode);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void removeEpisode() {
        Log.d(TAG, "Remove episodes");
        
        try {
            TableUtils.clearTable(helper.getConnectionSource(), Episode.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<Episode> getAllEpisodes() {
        List<Episode> listOfEpisode = null;
        try {
            listOfEpisode = getHelper().getEpisodeDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listOfEpisode;
    }
    
    /**
     * Link
     */
    
    public List<Link> getLinks(String mp3) {
        List<Link> listOfLinks = null;
        try {
            listOfLinks = getHelper().getLinkDao().query(getHelper().getLinkDao().queryBuilder().where().like("mp3", mp3).prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listOfLinks;
    }
    
    public void addLinks(List<Link> listOfLinks) {
        try {
            for (Link link : listOfLinks) {
                getHelper().getLinkDao().createOrUpdate(link);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void removeLinks() {
        Log.d(TAG, "Remove links");
        
        try {
            TableUtils.clearTable(helper.getConnectionSource(), Link.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
