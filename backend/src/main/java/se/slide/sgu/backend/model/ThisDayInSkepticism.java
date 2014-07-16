package se.slide.sgu.backend.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by slide on 2014-07-07.
 */
public class ThisDayInSkepticism {

    public long _id;
    public Date when;
    public String description;
    public List<Link> links;

    public ThisDayInSkepticism() {

        links = new ArrayList<Link>();

    }

}
