package se.slide.sgu;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import se.slide.sgu.model.Episode;
import se.slide.sgu.model.Item;
import se.slide.sgu.model.Link;
import se.slide.sgu.model.Quote;
import se.slide.sgu.model.Section;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SectionParser {
    
    // We don't use namespaces
    private static final String ns = null;
   
    public List<Episode> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readXml(parser);
        } finally {
            in.close();
        }
    }
    
    private List<Episode> readXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Episode> listOfEpisodes = new ArrayList<Episode>();
        
        parser.require(XmlPullParser.START_TAG, ns, "sgu");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            
            if (name.equals("episode")) {
                listOfEpisodes.add(readEpisode(parser));
            } else {
                skip(parser);
            }
        }
        
        return listOfEpisodes;
    }
    
     // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
     // to their respective "read" methods for processing. Otherwise, skips the tag.
     private Episode readEpisode(XmlPullParser parser) throws XmlPullParserException, IOException {
         parser.require(XmlPullParser.START_TAG, ns, "episode");
         
         Episode episode = new Episode();
         
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
             
             if (name.equals("mp3")) {
                 episode.mp3 = getText(parser, "mp3");
             }
             else if (name.equals("title")) {
                 episode.title = getText(parser, "title");
             }
             else if (name.equals("transcript")) {
                 episode.transcript = getText(parser, "transcript");
             }
             else if (name.equals("description")) {
                 episode.description = getText(parser, "description");
             }
             else if (name.equals("sections")) {
                 episode.listOfSection = readSections(parser); 
             }
             else if (name.equals("hosts")) {
                 episode.hosts = readHosts(parser);
             }
             else if (name.equals("quote")) {
                 episode.quote = readQuote(parser);
             }
             else if (name.equals("scienceorfiction")) {
                 episode.listOfItem = readScienceorfiction(parser);
             }
             else {
                 skip(parser);
             }
         }
         
         // Set the unique mp3 for each section
         for (Section section : episode.listOfSection) {
             section.mp3 = episode.mp3;
         }
         
         for (Item item : episode.listOfItem) {
             item.mp3 = episode.mp3;
         }
         
         episode.quote.mp3 = episode.mp3;
         
         return episode;
     }
     
     private List<Item> readScienceorfiction(XmlPullParser parser) throws XmlPullParserException, IOException {
         parser.require(XmlPullParser.START_TAG, ns, "scienceorfiction");
         
         List<Item> listOfItem = new ArrayList<Item>();
         
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
             
             if (name.equals("item")) {
                 listOfItem.add(readItem(parser));
             }
             else {
                 skip(parser);
             }
             
         }
         return listOfItem;
     }
     
     private Item readItem(XmlPullParser parser) throws XmlPullParserException, IOException, NumberFormatException {
         parser.require(XmlPullParser.START_TAG, ns, "item");
         
         Item item = new Item();
         List<Link> listOfLinks = new ArrayList<Link>();
         
         boolean science = true; //default to science? ;)
         
         String sci = parser.getAttributeValue(null, "science");
         if (sci != null && sci.equalsIgnoreCase("false"))
             science = false;
         
         item.science = science;
         
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
             
             if (name.equals("title")) {
                 item.title = getText(parser, "title");
             }
             else if (name.equals("description")) {
                 item.description = getText(parser, "description");
             }
             else if (name.equals("link")) {
                 item.link = getText(parser, "link");
                 
                 Link link = new Link();
                 link.url = item.link;
                 listOfLinks.add(link);
             }
             else {
                 skip(parser);
             }
         }
         
         for (Link link : listOfLinks) {
             link.belongsToSection = Link.BELONG_TO_SCIENCE_OR_FICTION;
             link.title = item.title;
         }
         
         return item;
     }
     
     private Quote readQuote(XmlPullParser parser) throws XmlPullParserException, IOException {
         parser.require(XmlPullParser.START_TAG, ns, "quote");
         
         Quote quote = new Quote();
         
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
             
             if (name.equals("text")) {
                 quote.text = getText(parser, "text");
             }
             else if (name.equals("by")) {
                 quote.by = getText(parser, "by");
             } 
             else {
                 skip(parser);
             }
             
         }
         return quote;
     }
     
     private String readHosts(XmlPullParser parser) throws XmlPullParserException, IOException {
         parser.require(XmlPullParser.START_TAG, ns, "hosts");
         
         List<String> hosts = new ArrayList<String>();
         
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
             
             if (name.equals("host")) {
                 hosts.add(readHost(parser));
             }
             else {
                 skip(parser);
             }
             
         }
         
         // Convert to SQLite friendly String
         StringBuilder builder = new StringBuilder();
         for (int i = 0; i < hosts.size(); i++) {
             String id = hosts.get(i);
             
             if (id == null)
                 continue;
             
             builder.append(hosts.get(i));
             
             if (i < hosts.size() - 1)
                 builder.append(";");
         }
         
         return builder.toString();
     }
     
     private String readHost(XmlPullParser parser) throws XmlPullParserException, IOException, NumberFormatException {
         parser.require(XmlPullParser.START_TAG, ns, "host");
         
         String id = parser.getAttributeValue(null, "id");
         
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             skip(parser);
         }
                  
         return id;
     }
     
     // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
     // to their respective "read" methods for processing. Otherwise, skips the tag.
     private List<Section> readSections(XmlPullParser parser) throws XmlPullParserException, IOException {
         parser.require(XmlPullParser.START_TAG, ns, "sections");
         
         List<Section> sections = new ArrayList<Section>();
         
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
             
             if (name.equals("section")) {
                 sections.add(readSection(parser));
             }
             else {
                 skip(parser);
             }
             
         }
         return sections;
     }
     
     private Section readSection(XmlPullParser parser) throws XmlPullParserException, IOException, NumberFormatException {
         parser.require(XmlPullParser.START_TAG, ns, "section");
         
         String number = null;
         String title = null;
         String start = null;
         List<Link> listOfLinks = new ArrayList<Link>();
         
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
             
             if (name.equals("number")) {
                 number = getText(parser, "number");
             }
             else if (name.equals("title")) {
                 title = getText(parser, "title");
             }
             else if (name.equals("link")) {
                 String url = getText(parser, "link");
                 Link link = new Link();
                 link.url = url;
                 listOfLinks.add(link);
             }
             else if (name.equals("start")) {
                 start = getText(parser, "start");
             }
             else {
                 skip(parser);
             }
         }
         
         // These might go wrong
         int num = Integer.valueOf(number);
         int sta = Formatter.convertStartToSeconds(start);
         
         for (Link link : listOfLinks) {
             link.belongsToSection = Link.BELONG_TO_SECTION;
             link.number = num;
             link.title = title;
         }
         
         Section section = new Section();
         section.title = title;
         section.number = num;
         section.start = sta;
         
         return section;
     }
     
     private String getText(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
         parser.require(XmlPullParser.START_TAG, ns, tag);
         String text = readText(parser);
         parser.require(XmlPullParser.END_TAG, ns, tag);
         return text;
     }
     
     // For the tags title and summary, extracts their text values.
     private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
         String result = "";
         if (parser.next() == XmlPullParser.TEXT) {
             result = parser.getText();
             parser.nextTag();
         }
         return result;
     }
     
     private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
         if (parser.getEventType() != XmlPullParser.START_TAG) {
             throw new IllegalStateException();
         }
         int depth = 1;
         while (depth != 0) {
             switch (parser.next()) {
             case XmlPullParser.END_TAG:
                 depth--;
                 break;
             case XmlPullParser.START_TAG:
                 depth++;
                 break;
             }
         }
      }
}
