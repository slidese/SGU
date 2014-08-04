package se.slide.sgu.backend.sax;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import se.slide.sgu.backend.MetaDataEndpoint;
import se.slide.sgu.backend.model.Episode;
import se.slide.sgu.backend.model.Host;
import se.slide.sgu.backend.model.Link;
import se.slide.sgu.backend.model.Quote;
import se.slide.sgu.backend.model.ScienceOrFiction;
import se.slide.sgu.backend.model.Section;

/**
 * Created by slide on 2014-07-10.
 */
public class MetaDataParser {

    private static final Logger LOG = Logger.getLogger(MetaDataEndpoint.class.getName());

    public List<Episode> parse(InputStream is) {

        final List<Episode> listOfEpisodes = new ArrayList<Episode>();
        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {

                String currentValue = null;
                Episode episode;
                Host host;
                Quote quote;
                Section section;
                ScienceOrFiction scienceOrFiction;

                Stack<String> tagsStack = new Stack<String>();

                public void startDocument() {
                    tagsStack.push("");
                }

                public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
                    //System.out.println("Start Element:" + qName);

                    tagsStack.push(qName);

                    if (qName.equalsIgnoreCase("episode")) {
                        episode = new Episode();
                    }
                    else if (qName.equalsIgnoreCase("host")) {
                        host = new Host();

                        String id = attributes.getValue("id");
                        if (id.equalsIgnoreCase("1"))
                            host.hostId = Host.HOST_STEVEN;
                        else if (id.equalsIgnoreCase("2"))
                            host.hostId = Host.HOST_BOB;
                        else if (id.equalsIgnoreCase("3"))
                            host.hostId = Host.HOST_JAY;
                        else if (id.equalsIgnoreCase("4"))
                            host.hostId = Host.HOST_REBECCA;
                        else if (id.equalsIgnoreCase("5"))
                            host.hostId = Host.HOST_EVAN;

                        episode.hosts.add(host);
                    }
                    else if (qName.equalsIgnoreCase("quote")) {
                        quote = new Quote();
                    }
                    else if (qName.equalsIgnoreCase("section")) {
                        section = new Section();
                    }
                    else if (qName.equalsIgnoreCase("item")) {
                        scienceOrFiction = new ScienceOrFiction();
                        scienceOrFiction.science = Boolean.parseBoolean(attributes.getValue("science"));
                    }

                }

                public void endElement(String uri, String localName, String qName) throws SAXException {
                    //System.out.println("End Element:" + qName);

                    String tag = tagsStack.peek();
                    if (!qName.equals(tag)) {
                        throw new InternalError();
                    }

                    tagsStack.pop();
                    String parentTag = tagsStack.peek();

                    if (qName.equalsIgnoreCase("episode")) {
                        listOfEpisodes.add(episode);
                    }
                    else if (qName.equalsIgnoreCase("title")) {
                        if (parentTag.equalsIgnoreCase("episode")) {
                            episode.title = currentValue;
                        }
                        else if (parentTag.equalsIgnoreCase("section")) {
                            section.title = currentValue;
                        }
                        else if (parentTag.equalsIgnoreCase("item")) {
                            scienceOrFiction.title = currentValue;
                        }
                    }
                    else if (qName.equalsIgnoreCase("description")) {
                        if (parentTag.equalsIgnoreCase("episode")) {
                            episode.description = currentValue;
                        }
                        else if (parentTag.equalsIgnoreCase("item")) {
                            scienceOrFiction.description = currentValue;
                        }
                    }
                    else if (qName.equalsIgnoreCase("guid")) {
                        if (parentTag.equalsIgnoreCase("episode")) {
                            episode.guid = currentValue;
                        }
                    }
                    else if (qName.equalsIgnoreCase("transcript")) {
                        if (parentTag.equalsIgnoreCase("episode")) {
                            episode.transcript = currentValue;
                        }
                    }
                    else if (qName.equalsIgnoreCase("image")) {
                        if (parentTag.equalsIgnoreCase("episode")) {
                            episode.image = currentValue;
                        }
                    }
                    else if (qName.equalsIgnoreCase("text")) {
                        if (parentTag.equalsIgnoreCase("quote")) {
                            quote.text = currentValue;
                        }
                    }
                    else if (qName.equalsIgnoreCase("by")) {
                        if (parentTag.equalsIgnoreCase("quote")) {
                            quote.by = currentValue;
                        }
                    }
                    else if (qName.equalsIgnoreCase("quote")) {
                        episode.quotes.add(quote);
                    }
                    else if (qName.equalsIgnoreCase("number")) {
                        if (parentTag.equalsIgnoreCase("section")) {
                            section.number = Integer.parseInt(currentValue);
                        }
                    }
                    else if (qName.equalsIgnoreCase("start")) {
                        if (parentTag.equalsIgnoreCase("section")) {
                            section.start = currentValue;
                        }
                    }
                    else if (qName.equalsIgnoreCase("link")) {
                        Link link = new Link();
                        link.url = currentValue;

                        if (parentTag.equalsIgnoreCase("section")) {
                            section.links.add(link);
                        }
                        else if (parentTag.equalsIgnoreCase("item")) {
                            scienceOrFiction.links.add(link);
                        }
                    }
                    else if (qName.equalsIgnoreCase("section")) {
                        episode.sections.add(section);
                    }
                    else if (qName.equalsIgnoreCase("item")) {
                        episode.scienceorfictions.add(scienceOrFiction);
                    }


                }

                public void characters(char ch[], int start, int length) throws SAXException {
                    //System.out.println("Text: " + new String(ch, start, length));
                    currentValue = new String(ch, start, length);
                }

            };

            saxParser.parse(is, handler);


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error2: " + e.getMessage());

        }

        return listOfEpisodes;
    }

}
