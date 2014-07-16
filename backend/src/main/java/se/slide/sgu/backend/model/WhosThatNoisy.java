package se.slide.sgu.backend.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by slide on 2014-07-07.
 */
public class WhosThatNoisy {

    public long _id;
    public String description;
    public List<Link> links;

    public WhosThatNoisy() {

        links = new ArrayList<Link>();

    }

}
