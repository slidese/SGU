package se.slide.sgu.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.xml.crypto.Data;

import se.slide.sgu.backend.model.Episode;
import se.slide.sgu.backend.model.Host;
import se.slide.sgu.backend.model.Link;
import se.slide.sgu.backend.model.Quote;
import se.slide.sgu.backend.model.ScienceOrFiction;
import se.slide.sgu.backend.model.Section;
import se.slide.sgu.backend.sax.MetaDataParser;

/** An endpoint class we are exposing */
@Api(name = "metaDataEndpoint", version = "v1", namespace = @ApiNamespace(ownerDomain = "backend.sgu.slide.se", ownerName = "backend.sgu.slide.se", packagePath=""))
public class MetaDataEndpoint {

    // Make sure to add this endpoint to your web.xml file if this is a web application.

    private static final Logger LOG = Logger.getLogger(MetaDataEndpoint.class.getName());

    @ApiMethod(name = "getEpisodeByUid")
    public Episode getEpisodeByUid(@Named("uid") String uid) {

        LOG.info("Calling getEpisodeByUid method, uid: " + uid);

        MetaData meta = new MetaData();
        Episode episode = meta.getEpisodeByUid(uid);

        return episode;
    }

    @ApiMethod(name = "getEpisodes")
    public List<Episode> getEpisodes() {

        LOG.info("Calling getEpisodes method");

        return new MetaData().getEpisodes();
    }

    /**
     * This method initializes the backend database. It pulls the old metadata XML from the web
     * and inserts it into tables
     */
    @ApiMethod(name = "init")
    public List<Episode> init() {

        List<Episode> listOfEpisodes = new ArrayList<Episode>();
        URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();
        try {
            URL url = new URL("http://x12.se/sgu_metadata.xml");
            HTTPResponse response = fetcher.fetch(url);



            //String xml = new String(content, "UTF-8");
            //LOG.info(xml);

            // if redirects are followed, this returns the final URL we are redirected to
            URL finalUrl = response.getFinalUrl();

            // 200, 404, 500, etc
            int responseCode = response.getResponseCode();


            byte[] content = response.getContent();

            InputStream is = new ByteArrayInputStream(content);

            MetaDataParser metadataParser = new MetaDataParser();
            listOfEpisodes = metadataParser.parse(is);
            for (Episode episode : listOfEpisodes) {
                //LOG.info(episode.title);
                //System.out.println(episode.title);
            }

            List<HTTPHeader> headers = response.getHeaders();

            for(HTTPHeader header : headers) {
                String headerName = header.getName();
                String headerValue = header.getValue();
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error1: " + e.getMessage());
            // new URL throws MalformedUrlException, which is impossible for us here
        }

        LOG.info("Fetched XML and parsed it to episode objects");

        DatabaseHelper.INSTANCE.deleteHosts();
        DatabaseHelper.INSTANCE.deleteEpisode();
        DatabaseHelper.INSTANCE.deleteEpisode2Host();
        DatabaseHelper.INSTANCE.deletePremiumContent();
        DatabaseHelper.INSTANCE.deleteQuotes();
        DatabaseHelper.INSTANCE.deleteScienceOrFiction();
        DatabaseHelper.INSTANCE.deleteLink();
        DatabaseHelper.INSTANCE.deleteScienceOrFiction2Link();
        DatabaseHelper.INSTANCE.deleteWhosThatNoisy();
        DatabaseHelper.INSTANCE.deleteTag();
        DatabaseHelper.INSTANCE.deleteNewsItem();
        DatabaseHelper.INSTANCE.deleteNewsItem2Link();
        DatabaseHelper.INSTANCE.deleteSection();
        DatabaseHelper.INSTANCE.deleteSection2Link();
        DatabaseHelper.INSTANCE.deleteThisDayInSkepticism();

        LOG.info("Deleted data from database tables");

        Map<String, Object> map = new HashMap<String, Object>();

        map.put(DatabaseHelper.HOST_HOSTID, 10);
        map.put(DatabaseHelper.HOST_FIRSTNAME, "Steven");
        map.put(DatabaseHelper.HOST_LASTNAME, "Novella");
        map.put(DatabaseHelper.HOST_DESCRIPTION, "Dr. Novella is an academic neurologist at Yale University School of Medicine. In addition to being the host of The Skeptics’ Guide podcast, he is the president and co-founder of the New England Skeptical Society. He is also the author of NeuroLogicaBlog, a popular science blog that covers news and issues in neuroscience, but also general science, scientific skepticism, philosophy of science, critical thinking, and the intersection of science with the media and society. Dr. Novella also contributes every Sunday to The Rogues Gallery, the official blog of the SGU, every Monday to SkepticBlog, and every Wednesday to Science-Based Medicine, a blog dedicated to issues of science and medicine. He is also a fellow of the Committee for Skeptical Inquiry (CSI) and a founding fellow of the Institute for Science in Medicine. He has a regular column in the Skeptical Inquirer – The Science of Medicine. Dr. Novella is also a Senior Fellow for the James Randi Educational Foundation and directs their Science-Based Medicine program. Dr. Novella is available for public lectures, radio, podcast, or other media appearances. Contact him through the contact page or at snovella@theness.com to request an appearance.");
        int host_steven = DatabaseHelper.INSTANCE.insertHost(map);

        map.put(DatabaseHelper.HOST_HOSTID, 20);
        map.put(DatabaseHelper.HOST_FIRSTNAME, "Bob");
        map.put(DatabaseHelper.HOST_LASTNAME, "Novella");
        map.put(DatabaseHelper.HOST_DESCRIPTION, "Bob Novella is a co-founder and Vice-President of the New England Skeptical Society. He co-hosts the Skeptics’ Guide to the Universe podcast and blogs for SGU’s Rogues Gallery. He has also written numerous articles that are widely published in skeptical literature. Bob’s scientific interests lie in the extremes, from the gargantuan to the infinitesimal: astronomy and cosmology to particle physics and quantum mechanics. He is especially fascinated by the human capacity for self-deception and anticipated future technologies such as nanotechnology, artificial intelligence, and human augmentation.");
        int host_bob = DatabaseHelper.INSTANCE.insertHost(map);

        map.put(DatabaseHelper.HOST_HOSTID, 30);
        map.put(DatabaseHelper.HOST_FIRSTNAME, "Jay");
        map.put(DatabaseHelper.HOST_LASTNAME, "Novella");
        map.put(DatabaseHelper.HOST_DESCRIPTION, "Jay has been a skeptical activist for 15 years who serves as a co-host and producer of The Skeptics’ Guide To The Universe, a popular science podcast. Jay is also the producer and director of SGU Video Productions whose videos can be seen on the SGU YouTube channel. He also is the Director of Marketing and Technology for The New England Skeptical Society, a not for profit organization focused on promoting higher standards of education, especially in the areas of science and critical thinking. For the past 3 years, Jay has served on the board of directors for the Northeast Conference on Science and Skepticism, a yearly conference held in NYC. Jay also is a regular contributor to The Rogues Gallery, a popular science and skepticism blog.");
        int host_jay = DatabaseHelper.INSTANCE.insertHost(map);

        map.put(DatabaseHelper.HOST_HOSTID, 40);
        map.put(DatabaseHelper.HOST_FIRSTNAME, "Rebecca");
        map.put(DatabaseHelper.HOST_LASTNAME, "Watson");
        map.put(DatabaseHelper.HOST_DESCRIPTION, "Rebecca Watson is a writer and speaker who runs the Skepchick Network. She travels around the world delivering entertaining talks on science, atheism, feminism, and skepticism. There is currently an asteroid orbiting the sun with her name on it. You can follow her every fascinating move on Twitter: @rebeccawatson.");
        int host_rebecca = DatabaseHelper.INSTANCE.insertHost(map);

        map.put(DatabaseHelper.HOST_HOSTID, 50);
        map.put(DatabaseHelper.HOST_FIRSTNAME, "Evan");
        map.put(DatabaseHelper.HOST_LASTNAME, "Bernstein");
        map.put(DatabaseHelper.HOST_DESCRIPTION, "Evan Bernstein is a co-host of The Skeptics’ Guide to the Universe and The Skeptics Guide 5×5 podcasts. He also serves as the Connecticut chapter chairman of the New England Skeptical Society. Evan is a technical adviser for official NESS investigations, and is the SGU’s audio engineer for live remote events. He has been published as part of The Skeptical Blog Anthology book (The Young Australian Skeptics), and he has appeared as a guest on several skeptic-themed podcasts. Traveling the world with his co-hosts, Evan as given live presentations to private corporations and at educational seminars on topics including; the direct harms of pseudoscience, woo in the martial arts, and the truth behind paranormal investigations. Evan earned his BA in Communications from Central Connecticut State University, and by day, owns and manages his financial services corporation. He has been an active participant in the modern skeptical movement since 1996.");
        int host_evan = DatabaseHelper.INSTANCE.insertHost(map);

        map = new HashMap<String, Object>();
        map.put(DatabaseHelper.TAG_ID, 10);
        map.put(DatabaseHelper.TAG_NAME, "Astronomy / Physics");
        DatabaseHelper.INSTANCE.insertTag(map);

        map.put(DatabaseHelper.TAG_ID, 11);
        map.put(DatabaseHelper.TAG_NAME, "Biology / Evolution");
        DatabaseHelper.INSTANCE.insertTag(map);

        map.put(DatabaseHelper.TAG_ID, 12);
        map.put(DatabaseHelper.TAG_NAME, "Chemistry / Earth Science");
        DatabaseHelper.INSTANCE.insertTag(map);

        map.put(DatabaseHelper.TAG_ID, 13);
        map.put(DatabaseHelper.TAG_NAME, "Critical Thinking");
        DatabaseHelper.INSTANCE.insertTag(map);

        map.put(DatabaseHelper.TAG_ID, 14);
        map.put(DatabaseHelper.TAG_NAME, "Faith / Religion");
        DatabaseHelper.INSTANCE.insertTag(map);

        map.put(DatabaseHelper.TAG_ID, 15);
        map.put(DatabaseHelper.TAG_NAME, "Health & Life Sciences");
        DatabaseHelper.INSTANCE.insertTag(map);

        map.put(DatabaseHelper.TAG_ID, 16);
        map.put(DatabaseHelper.TAG_NAME, "History");
        DatabaseHelper.INSTANCE.insertTag(map);

        map.put(DatabaseHelper.TAG_ID, 17);
        map.put(DatabaseHelper.TAG_NAME, "Humor");
        DatabaseHelper.INSTANCE.insertTag(map);

        map.put(DatabaseHelper.TAG_ID, 18);
        map.put(DatabaseHelper.TAG_NAME, "Pseudoscience");
        DatabaseHelper.INSTANCE.insertTag(map);

        map.put(DatabaseHelper.TAG_ID, 19);
        map.put(DatabaseHelper.TAG_NAME, "Technology");
        DatabaseHelper.INSTANCE.insertTag(map);

        map.put(DatabaseHelper.TAG_ID, 20);
        map.put(DatabaseHelper.TAG_NAME, "Live Recording");
        int i = DatabaseHelper.INSTANCE.insertTag(map);

        LOG.info("Inserted static data");

        /*
        map.put(DatabaseHelper.TAG_ID, 20);
        map.put(DatabaseHelper.TAG_NAME, "This Day in Skepticism");
        int i = DatabaseHelper.INSTANCE.insertTag(map);
        */

        int count = 0;

        for (Episode episode : listOfEpisodes) {

            if (episode.title.startsWith("Premium")) {
                map = new HashMap<String, Object>();
                map.put(DatabaseHelper.PREMIUM_GUID, episode.guid);
                map.put(DatabaseHelper.PREMIUM_UID, episode.guid.substring(episode.guid.indexOf("#")+1));
                map.put(DatabaseHelper.PREMIUM_ID1, null);
                map.put(DatabaseHelper.PREMIUM_ACTIVE, true);
                map.put(DatabaseHelper.PREMIUM_TITLE, episode.title);
                map.put(DatabaseHelper.PREMIUM_DESCRIPTION, episode.description);
                map.put(DatabaseHelper.PREMIUM_IMAGE, episode.image);
                map.put(DatabaseHelper.PREMIUM_TRANSCRIPT, episode.transcript);
                int premium_id = DatabaseHelper.INSTANCE.insertPremiumContent(map);

                continue; // Go to next item in listOfEpisodes
            }

            map = new HashMap<String, Object>();
            map.put(DatabaseHelper.EPISODE_GUID, episode.guid);
            map.put(DatabaseHelper.EPISODE_UID, episode.guid.substring(episode.guid.indexOf("#")+1));
            map.put(DatabaseHelper.EPISODE_ID1, episode.guid.substring(episode.guid.lastIndexOf("/")+1, episode.guid.indexOf("#")));
            map.put(DatabaseHelper.EPISODE_ACTIVE, true);
            map.put(DatabaseHelper.EPISODE_TITLE, episode.title);
            map.put(DatabaseHelper.EPISODE_DESCRIPTION, episode.description);
            map.put(DatabaseHelper.EPISODE_IMAGE, episode.image);
            map.put(DatabaseHelper.EPISODE_TRANSCRIPT, episode.transcript);
            int episode_id = DatabaseHelper.INSTANCE.insertEpisode(map);

            for (Host host : episode.hosts) {
                if (host.hostId == 10)
                    DatabaseHelper.INSTANCE.insertEpisode2Host(episode_id, host_steven);
                else if (host.hostId == 20)
                    DatabaseHelper.INSTANCE.insertEpisode2Host(episode_id, host_bob);
                else if (host.hostId == 30)
                    DatabaseHelper.INSTANCE.insertEpisode2Host(episode_id, host_jay);
                else if (host.hostId == 40)
                    DatabaseHelper.INSTANCE.insertEpisode2Host(episode_id, host_rebecca);
                else if (host.hostId == 50)
                    DatabaseHelper.INSTANCE.insertEpisode2Host(episode_id, host_evan);
            }

            String bywhom = "";
            String text = "";

            for (Quote quote : episode.quotes) {
                //map = new HashMap<String, Object>();
                //map.put(DatabaseHelper.EPISODE_ID, episode_id);
                //map.put(DatabaseHelper.QUOTE_BYWHOM, quote.by);
                //map.put(DatabaseHelper.QUOTE_TEXT, quote.text.replaceAll("\"", ""));
                //int quote_id = DatabaseHelper.INSTANCE.insertQuote(map);

                bywhom = quote.by;
                text = quote.text.replaceAll("\"", "");
            }

            for (Section section : episode.sections) {
                String type = "";
                int auto_id = -1;

                map = new HashMap<String, Object>();
                map.put(DatabaseHelper.EPISODE_ID, episode_id);
                map.put(DatabaseHelper.SECTION_NUMBER, section.number);
                map.put(DatabaseHelper.SECTION_START, section.start);
                //int section_id = DatabaseHelper.INSTANCE.insertSection(map);

                // Default for integers
                map.put(DatabaseHelper.THISDAY_DAY, 0);
                map.put(DatabaseHelper.THISDAY_MONTH, 0);
                map.put(DatabaseHelper.SCIENCE_SCIENCE, false);

                if (section.title.startsWith("Introduction")) {
                    type = DatabaseHelper.TYPE_INTRODUCTION;

                    map.put(DatabaseHelper.INTRO_TITLE, "Introduction");
                    map.put(DatabaseHelper.INTRO_DESCRIPTION, "");
                }
                else if (section.title.startsWith("This Day in Skepticism")) {
                    type = DatabaseHelper.TYPE_THISDAYINSKEPTICISM;

                    map.put(DatabaseHelper.THISDAY_TITLE, section.title);
                    map.put(DatabaseHelper.THISDAY_DESCRIPTION, "");
                    map.put(DatabaseHelper.THISDAY_DAY, 0);
                    map.put(DatabaseHelper.THISDAY_MONTH, 0);
                    //DatabaseHelper.INSTANCE.insertThisDayInSkepticism(map);
                }
                else if (section.title.startsWith("Who's That Noisy")) {
                    type = DatabaseHelper.TYPE_WHOSTHATNOISY;

                    map.put(DatabaseHelper.NOISY_TITLE, "Who's That Noisy");
                    map.put(DatabaseHelper.NOISY_ANSWER, "");
                    //DatabaseHelper.INSTANCE.insertWhosThatNoisy(map);
                }
                else if (section.title.startsWith("Science or Fiction")) {
                    type = DatabaseHelper.TYPE_SCIENCEORFICTION;



                }
                else if (section.title.startsWith("Skeptical Quote of the Week")) {
                    type = DatabaseHelper.TYPE_QUOTE;

                    map.put(DatabaseHelper.QUOTE_BYWHOM, bywhom);
                    map.put(DatabaseHelper.QUOTE_TEXT, text);
                }
                else if (section.title.startsWith("Question #") || section.title.startsWith("Questions and Emails")) {
                    type = DatabaseHelper.TYPE_QUESTIONANDEMAIL;

                    map.put(DatabaseHelper.EMAIL_TITLE, section.title);
                    map.put(DatabaseHelper.EMAIL_MESSAGE, "");
                }
                else if (section.title.startsWith("Interview")) {
                    type = DatabaseHelper.TYPE_INTERVIEW;

                    if (section.title.indexOf(":") > 0) {
                        map.put(DatabaseHelper.INTERVIEW_WITH, section.title.substring(11));
                    } else if (section.title.indexOf(" with ") > 0) {
                        map.put(DatabaseHelper.INTERVIEW_WITH, section.title.substring(15));
                    } else {
                        map.put(DatabaseHelper.INTERVIEW_WITH, section.title);
                    }

                    map.put(DatabaseHelper.INTERVIEW_DESCRIPTION, "");
                }
                else if (section.title.startsWith("Name That Logical Fallacy")) {
                    type = DatabaseHelper.TYPE_LOGICALFALLACY;

                    map.put(DatabaseHelper.LOGICAL_TITLE, "Name That Logical Fallacy");

                }
                else {
                    type = DatabaseHelper.TYPE_NEWSITEM;

                    map.put(DatabaseHelper.NEWS_TITLE, section.title);
                }

                // Set the type
                map.put(DatabaseHelper.SECTION_TYPE, type);

                if (type.equalsIgnoreCase(DatabaseHelper.TYPE_SCIENCEORFICTION)) {
                    for (ScienceOrFiction scienceOrFiction : episode.scienceorfictions) {
                        map.put(DatabaseHelper.SCIENCE_TITLE, scienceOrFiction.title);
                        map.put(DatabaseHelper.SCIENCE_DESCRIPTION, scienceOrFiction.description);
                        map.put(DatabaseHelper.SCIENCE_SCIENCE, scienceOrFiction.science);

                        int section_id = DatabaseHelper.INSTANCE.insertSection(map);

                        for (Link link : scienceOrFiction.links) {
                            Map<String, Object> linkmap = new HashMap<String, Object>();
                            linkmap.put(DatabaseHelper.LINK_URL, link.url);

                            int link_id = DatabaseHelper.INSTANCE.insertLink(linkmap);

                            DatabaseHelper.INSTANCE.insertSection2Link(section_id, link_id);
                        }

                    }
                } else {
                    // Insert section
                    int section_id = DatabaseHelper.INSTANCE.insertSection(map);

                    for (Link link : section.links) {
                        Map<String, Object> linkmap = new HashMap<String, Object>();
                        linkmap.put(DatabaseHelper.LINK_URL, link.url);
                        int link_id = DatabaseHelper.INSTANCE.insertLink(linkmap);

                        DatabaseHelper.INSTANCE.insertSection2Link(section_id, link_id);
                    }
                }



            }

            LOG.info("Added episode " + ++count + " of " + listOfEpisodes.size());

        }

        /*
        LOG.info("Inserting...");

        try {
            Connection connection = null;
            try {





                Class.forName("com.mysql.jdbc.Driver");
                String url = "jdbc:mysql://127.0.0.1:3306/test?user=root";
                String insertSteven = "insert into host (`hostId`, `firstname`, `lastname`, `description`) values ('10', 'Steven', 'Novella', 'The Doctor')";

                connection = DriverManager.getConnection(url);



                PreparedStatement statement = connection.prepareStatement(insertSteven);
                boolean success = statement.execute();


                LOG.info("Insert: " + success);

            } finally {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.severe("Error init");
        }
        */

        LOG.info("Finished initiating episodes");


        return listOfEpisodes;
    }

    /**
     * This method gets the <code>MetaData</code> object associated with the specified <code>id</code>.
     * @param id The id of the object to be returned.
     * @return The <code>MetaData</code> associated with <code>id</code>.
     */
    @ApiMethod(name = "getMetaData")
    public MetaData getMetaData(@Named("id") Long id) {
        // Implement this function

        LOG.info("Calling getMetaData method");
        return null;
    }

    /**
     * This inserts a new <code>MetaData</code> object.
     * @param metaData The object to be added.
     * @return The object to be added.
     */
    @ApiMethod(name = "insertMetaData")
    public MetaData insertMetaData(MetaData metaData) {
        // Implement this function

        LOG.info("Calling insertMetaData method");
        return metaData;
    }
}