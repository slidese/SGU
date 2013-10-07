package se.slide.sgu;

import android.os.DropBoxManager.Entry;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import se.slide.sgu.model.Section;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SectionParser {
    
    // We don't use namespaces
    private static final String ns = null;
   
    public List<Section> parse(InputStream in) throws XmlPullParserException, IOException {
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
    
    private List<Section> readXml(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Section> sections = new ArrayList<Section>();

        parser.require(XmlPullParser.START_TAG, ns, "sgu");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            
            if (name.equals("episode")) {
                sections.addAll(readEpisode(parser));
            } else {
                skip(parser);
            }
        }
        
        return sections;
    }
    
     // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
     // to their respective "read" methods for processing. Otherwise, skips the tag.
     private List<Section> readEpisode(XmlPullParser parser) throws XmlPullParserException, IOException {
         parser.require(XmlPullParser.START_TAG, ns, "episode");
         
         String mp3 = null;
         List<Section> sections = new ArrayList<Section>();
         
         while (parser.next() != XmlPullParser.END_TAG) {
             if (parser.getEventType() != XmlPullParser.START_TAG) {
                 continue;
             }
             String name = parser.getName();
             
             if (name.equals("mp3")) {
                 mp3 = getText(parser, "mp3");
             }
             else if (name.equals("sections")) {
                 sections.addAll(readSections(parser));
             }
             else {
                 skip(parser);
             }
         }
         
         // Set the unique mp3 for each section
         for (Section section : sections) {
             section.mp3 = mp3;
         }
         
         return sections;
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
