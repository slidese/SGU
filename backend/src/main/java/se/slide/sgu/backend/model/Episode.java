package se.slide.sgu.backend.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slide on 2014-07-07.
 */
public class Episode {

    public String guid;
    public String uid;
    public String title;
    public String description;
    public String transcript;
    public String image;
    public List<Host> hosts;
    public List<Guest> guests;
    public List<Quote> quotes;
    public List<ScienceOrFiction> scienceorfictions;
    public List<NewsItem> newsitems;
    public List<WhosThatNoisy> whosthatnoisy;
    public List<QuestionAndEmail> questionsandemails;
    public ThisDayInSkepticism thisdayinskepticism;
    public List<Section> sections;

    public Episode() {

        hosts = new ArrayList<Host>();
        guests = new ArrayList<Guest>();
        quotes = new ArrayList<Quote>();
        scienceorfictions = new ArrayList<ScienceOrFiction>();
        newsitems = new ArrayList<NewsItem>();
        whosthatnoisy = new ArrayList<WhosThatNoisy>();
        questionsandemails = new ArrayList<QuestionAndEmail>();
        sections = new ArrayList<Section>();

    }
}
