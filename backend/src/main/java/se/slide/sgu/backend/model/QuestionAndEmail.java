package se.slide.sgu.backend.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slide on 2014-07-07.
 */
public class QuestionAndEmail extends Section {

    public long _id;
    public int index;
    public String subject;
    public String message;
    public List<Link> links;

    public QuestionAndEmail() {

        links = new ArrayList<Link>();

    }

}
