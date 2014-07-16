package se.slide.sgu.backend.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slide on 2014-07-07.
 */
public class ScienceOrFiction {

    public long _id;
    public boolean science;
    public String title;
    public String description;
    public List<Link> links;

    public ScienceOrFiction() {

        links = new ArrayList<Link>();

    }
}
