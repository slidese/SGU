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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import se.slide.sgu.backend.model.Episode;
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

                DatabaseHelper.INSTANCE.insert();

                LOG.info("Insert: " + success);

            } finally {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.severe("Error init");
        }


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