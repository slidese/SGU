package se.slide.sgu.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import se.slide.sgu.backend.model.Episode;

/**
 * Created by slide on 2014-07-07.
 */
public class MetaData {

    private static final Logger LOG = Logger.getLogger(MetaData.class.getName());

    public List<Episode> getEpisodes() {

        List<Episode> listOfEpisode = new ArrayList<Episode>();

        try {
            Connection connection = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                String url = "jdbc:mysql://127.0.0.1:3306/test?user=root";
                String queryEpisode = "SELECT * FROM episode";

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(queryEpisode);
                ResultSet rs = statement.executeQuery();

                if (!rs.isBeforeFirst())
                    return null;

                while (rs.next()) {
                    Episode episode = new Episode();
                    episode.guid = rs.getString("guid");
                    episode.uid = rs.getString("uid");
                    episode.title = rs.getString("title");
                    episode.image = rs.getString("image");
                    episode.description = rs.getString("description");
                    episode.transcript = rs.getString("transcript");

                    listOfEpisode.add(episode);
                }

            } finally {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.severe("Error getting episodes getEpisodes:");
        }

        return listOfEpisode;

    }

    public Episode getEpisodeByUid(String uid) {

        Episode episode = null;

        try {
            Connection connection = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                String url = "jdbc:mysql://127.0.0.1:3306/test?user=root";
                String queryEpisode = "SELECT * FROM episode where uid = ?";


                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(queryEpisode);
                statement.setString(1, uid);
                ResultSet rs = statement.executeQuery();

                if (!rs.isBeforeFirst())
                    return null;

                episode = new Episode();

                while (rs.next()) {
                    episode.guid = rs.getString("guid");
                    episode.uid = rs.getString("uid");
                    episode.title = rs.getString("title");
                    episode.image = rs.getString("image");
                    episode.description = rs.getString("description");
                    episode.transcript = rs.getString("transcript");
                }

            } finally {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.severe("Error getting episide getEpisodeByUid:" + uid);
        }

        return episode;

    }

}
