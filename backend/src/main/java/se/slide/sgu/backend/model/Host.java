package se.slide.sgu.backend.model;

import java.util.List;

/**
 * Created by slide on 2014-07-07.
 */
public class Host {

    public static int HOST_STEVEN  = 10;
    public static int HOST_BOB     = 20;
    public static int HOST_JAY     = 30;
    public static int HOST_REBECCA = 40;
    public static int HOST_EVAN    = 50;

    public long _id;
    public int hostId;
    public String firstname;
    public String lastname;
    public String description;
    public List<Link> links;

}
