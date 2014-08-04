package se.slide.sgu.backend.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slide on 2014-07-07.
 */
public class Section {

    public static int TYPE_NEWSITEM             = 10;
    public static int TYPE_QUESTIONANDEMAIL     = 20;
    public static int TYPE_QUOTE                = 30;
    public static int TYPE_SCIENCEORFICTION     = 40;
    public static int TYPE_THISDAYINSKEPTICISM  = 50;
    public static int TYPE_WHOSTHATNOISY        = 60;

    public long _id;
    public int number;
    public String start;
    public int pointsToType;
    public int pointsToId;

    public String title;
    public List<Link> links;

    public Section() {

        links = new ArrayList<Link>();

    }

}
