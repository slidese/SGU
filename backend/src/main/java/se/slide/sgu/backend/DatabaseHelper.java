package se.slide.sgu.backend;

import com.google.cloud.sql.jdbc.Statement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

/**
 * Created by slide on 2014-07-16.
 */
public enum DatabaseHelper {
    INSTANCE;

    private final String url;

    DatabaseHelper() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        url = "jdbc:mysql://127.0.0.1:3306/test?user=root";
    }

    public final static String HOST_HOSTID                          = "host_hostId";
    public final static String HOST_FIRSTNAME                       = "host_firstname";
    public final static String HOST_LASTNAME                        = "host_lastname";
    public final static String HOST_DESCRIPTION                     = "host_description";

    public final static String EPISODE_GUID                         = "episode_guid";
    public final static String EPISODE_UID                          = "episode_uid";
    public final static String EPISODE_ID1                          = "episode_id1";
    public final static String EPISODE_ACTIVE                       = "episode_active";
    public final static String EPISODE_TITLE                        = "episode_title";
    public final static String EPISODE_DESCRIPTION                  = "episode_description";
    public final static String EPISODE_IMAGE                        = "episode_image";
    public final static String EPISODE_TRANSCRIPT                   = "episode_transcript";

    public final static String PREMIUM_GUID                         = "premium_guid";
    public final static String PREMIUM_UID                          = "premium_uid";
    public final static String PREMIUM_ID1                          = "premium_id1";
    public final static String PREMIUM_ACTIVE                       = "premium_active";
    public final static String PREMIUM_TITLE                        = "premium_title";
    public final static String PREMIUM_DESCRIPTION                  = "premium_description";
    public final static String PREMIUM_IMAGE                        = "premium_image";
    public final static String PREMIUM_TRANSCRIPT                   = "premium_transcript";

    public final static String QUOTE_TEXT                           = "quote_text";
    public final static String QUOTE_BYWHOM                         = "quote_bywhom";

    public final static String SCIENCE_TITLE                        = "science_title";
    public final static String SCIENCE_DESCRIPTION                  = "science_description";
    public final static String SCIENCE_SCIENCE                      = "science_science";

    public final static String LINK_URL                             = "link_url";

    public final static String EPISODE_ID                           = "episode_id";
    public final static String TAGS                                 = "tags";

    public final static String SECTION_NUMBER                       = "section_number";
    public final static String SECTION_START                        = "section_start";
    public final static String SECTION_TYPE                         = "section_type";

    public final static String TAG_ID                               = "tagid";
    public final static String TAG_NAME                             = "tagname";

    public final static String NOISY_TITLE                          = "noisy_title";
    public final static String NOISY_ANSWER                         = "noisy_answer";

    public final static String INTRO_TITLE                          = "intro_title";
    public final static String INTRO_DESCRIPTION                    = "intro_description";

    public final static String INTERVIEW_WITH                       = "interview_with";
    public final static String INTERVIEW_DESCRIPTION                = "interview_description";

    public final static String THISDAY_TITLE                        = "thisday_title";
    public final static String THISDAY_DESCRIPTION                  = "thisday_description";
    public final static String THISDAY_DAY                          = "thisday_day";
    public final static String THISDAY_MONTH                        = "thisday_month";

    public final static String NEWS_TITLE                           = "news_title";

    public final static String LOGICAL_TITLE                        = "logical_title";

    public final static String EMAIL_TITLE                          = "email_title";
    public final static String EMAIL_MESSAGE                        = "email_message";

    public final static String REVIEW_TITLE                         = "review_title";
    public final static String REVIEW_DESCRIPTION                   = "review_description";

    public final static String TYPE_INTRODUCTION                    = "introduction";
    public final static String TYPE_THISDAYINSKEPTICISM             = "thisdayinskepticism";
    public final static String TYPE_NEWSITEM                        = "newsitem";
    public final static String TYPE_WHOSTHATNOISY                   = "whosthatnoisy";
    public final static String TYPE_QUESTIONANDEMAIL                = "questionandemail";
    public final static String TYPE_SCIENCEORFICTION                = "scienceorfiction";
    public final static String TYPE_QUOTE                           = "quote";
    public final static String TYPE_INTERVIEW                       = "interview";
    public final static String TYPE_LOGICALFALLACY                  = "logicalfallacy";

    public void deleteHosts() {

        try {

            String sql = "DELETE FROM host";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int insertHost(Map<String, Object> values) {

        int auto_id = -1;

        try {

            String sql = "INSERT INTO host (hostId, firstname, lastname, description, created, updated) VALUES (?,?,?,?,?,?)";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                statement.setInt(1, (Integer)values.get(HOST_HOSTID));
                statement.setString(2, (String) values.get(HOST_FIRSTNAME));
                statement.setString(3, (String) values.get(HOST_LASTNAME));
                statement.setString(4, (String) values.get(HOST_DESCRIPTION));
                statement.setObject(5, new Timestamp(new Date().getTime()));
                statement.setObject(6, new Timestamp(new Date().getTime()));

                statement.executeUpdate();

                ResultSet rs = statement.getGeneratedKeys();
                rs.next();
                auto_id = rs.getInt(1);

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return auto_id;
    }

    public int insertTag(Map<String, Object> values) {

        int auto_id = -1;

        try {

            String sql = "INSERT INTO tag (tagid, tagname, created, updated) VALUES (?,?,?,?)";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                statement.setInt(1, (Integer)values.get(TAG_ID));
                statement.setString(2, (String) values.get(TAG_NAME));
                statement.setObject(3, new Timestamp(new Date().getTime()));
                statement.setObject(4, new Timestamp(new Date().getTime()));

                statement.executeUpdate();

                ResultSet rs = statement.getGeneratedKeys();
                rs.next();
                auto_id = rs.getInt(1);

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return auto_id;
    }


    public void deleteEpisode() {

        try {

            String sql = "DELETE FROM episode";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deletePremiumContent() {

        try {

            String sql = "DELETE FROM premium_content";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int insertEpisode(Map<String, Object> values) {

        int auto_id = -1;

        try {

            String sql = "INSERT INTO episode (guid, uid, id, active, title, description, image, transcript, created, updated) VALUES (?,?,?,?,?,?,?,?,?,?)";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                statement.setString(1, (String)values.get(EPISODE_GUID));
                statement.setString(2, (String) values.get(EPISODE_UID));
                statement.setString(3, (String) values.get(EPISODE_ID1));
                statement.setBoolean(4, (Boolean) values.get(EPISODE_ACTIVE));
                statement.setString(5, (String) values.get(EPISODE_TITLE));
                statement.setString(6, (String) values.get(EPISODE_DESCRIPTION));
                statement.setString(7, (String) values.get(EPISODE_IMAGE));
                statement.setString(8, (String) values.get(EPISODE_TRANSCRIPT));
                statement.setObject(9, new Timestamp(new Date().getTime()));
                statement.setObject(10, new Timestamp(new Date().getTime()));

                statement.executeUpdate();

                ResultSet rs = statement.getGeneratedKeys();
                rs.next();
                auto_id = rs.getInt(1);

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return auto_id;
    }

    public int insertPremiumContent(Map<String, Object> values) {

        int auto_id = -1;

        try {

            String sql = "INSERT INTO premium_content (guid, uid, id, title, active, description, image, transcript, created, updated) VALUES (?,?,?,?,?,?,?,?,?,?)";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                statement.setString(1, (String)values.get(PREMIUM_GUID));
                statement.setString(2, (String) values.get(PREMIUM_UID));
                statement.setString(3, (String) values.get(PREMIUM_ID1));
                statement.setBoolean(4, (Boolean) values.get(PREMIUM_ACTIVE));
                statement.setString(5, (String) values.get(PREMIUM_TITLE));
                statement.setString(6, (String) values.get(PREMIUM_DESCRIPTION));
                statement.setString(7, (String) values.get(PREMIUM_IMAGE));
                statement.setString(8, (String) values.get(PREMIUM_TRANSCRIPT));
                statement.setObject(9, new Timestamp(new Date().getTime()));
                statement.setObject(10, new Timestamp(new Date().getTime()));

                statement.executeUpdate();

                ResultSet rs = statement.getGeneratedKeys();
                rs.next();
                auto_id = rs.getInt(1);

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return auto_id;
    }

    public void deleteEpisode2Host() {

        try {

            String sql = "DELETE FROM episode_2_host";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertEpisode2Host(int episode_id, int host_id) {

        try {

            String sql = "INSERT INTO episode_2_host (episode_id, host_id) VALUES (?,?)";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.setInt(1, episode_id);
                statement.setInt(2, host_id);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*

select * from episode_2_host junction
inner join episode on junction.episode_id = episode._id
inner join host on junction.host_id = host._id
where episode.title = 'Episode 450';

     */

    public void deleteQuotes() {

        try {

            String sql = "DELETE FROM quote";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int insertQuote(Map<String, Object> values) {

        int auto_id = -1;

        try {

            String sql = "INSERT INTO quote (episode_id, text, bywhom, created, updated) VALUES (?,?,?,?,?)";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                statement.setInt(1, (Integer) values.get(EPISODE_ID));
                statement.setString(2, (String)values.get(QUOTE_TEXT));
                statement.setString(3, (String) values.get(QUOTE_BYWHOM));
                statement.setObject(4, new Timestamp(new Date().getTime()));
                statement.setObject(5, new Timestamp(new Date().getTime()));

                statement.executeUpdate();

                ResultSet rs = statement.getGeneratedKeys();
                rs.next();
                auto_id = rs.getInt(1);

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return auto_id;
    }

    public void deleteWhosThatNoisy() {

        try {

            String sql = "DELETE FROM whosthatnoisy";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteTag() {

        try {

            String sql = "DELETE FROM tag";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int insertWhosThatNoisy(Map<String, Object> values) {

        int auto_id = -1;

        try {

            String sql = "INSERT INTO whosthatnoisy (episode_id, title, answer, tags, created, updated) VALUES (?,?,?,?,?,?)";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                statement.setInt(1, (Integer)values.get(EPISODE_ID));
                statement.setString(2, (String) values.get(NOISY_TITLE));
                statement.setString(3, (String) values.get(NOISY_ANSWER));
                statement.setString(4, (String) values.get(TAGS));
                statement.setObject(5, new Timestamp(new Date().getTime()));
                statement.setObject(6, new Timestamp(new Date().getTime()));

                statement.executeUpdate();

                ResultSet rs = statement.getGeneratedKeys();
                rs.next();
                auto_id = rs.getInt(1);

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return auto_id;
    }

    public void deleteEpisode2Quote() {

        try {

            String sql = "DELETE FROM episode_2_quote";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertEpisode2Quote(int episode_id, int quote_id) {

        try {

            String sql = "INSERT INTO episode_2_quote (episode_id, quote_id) VALUES (?,?)";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.setInt(1, episode_id);
                statement.setInt(2, quote_id);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void deleteScienceOrFiction() {

        try {

            String sql = "DELETE FROM scienceorfiction";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int insertScienceOrFiction(Map<String, Object> values) {

        int auto_id = -1;

        try {

            String sql = "INSERT INTO scienceorfiction (episode_id, title, description, science, created, updated) VALUES (?,?,?,?,?,?)";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                statement.setInt(1, (Integer)values.get(EPISODE_ID));
                statement.setString(2, (String)values.get(SCIENCE_TITLE));
                statement.setString(3, (String)values.get(SCIENCE_DESCRIPTION));
                statement.setBoolean(4, (Boolean) values.get(SCIENCE_SCIENCE));
                statement.setObject(5, new Timestamp(new Date().getTime()));
                statement.setObject(6, new Timestamp(new Date().getTime()));

                statement.executeUpdate();

                ResultSet rs = statement.getGeneratedKeys();
                rs.next();
                auto_id = rs.getInt(1);

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return auto_id;
    }

    public void insertScienceOrFiction2Link(int scienceorfiction_id, int link_id) {

        try {

            String sql = "INSERT INTO scienceorfiction_2_link (scienceorfiction_id, link_id) VALUES (?,?)";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.setInt(1, scienceorfiction_id);
                statement.setInt(2, link_id);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void deleteLink() {

        try {

            String sql = "DELETE FROM link";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int insertLink(Map<String, Object> values) {

        int auto_id = -1;

        try {

            String sql = "INSERT INTO link (url, created, updated) VALUES (?,?,?)";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                statement.setString(1, (String)values.get(LINK_URL));
                statement.setObject(2, new Timestamp(new Date().getTime()));
                statement.setObject(3, new Timestamp(new Date().getTime()));

                statement.executeUpdate();

                ResultSet rs = statement.getGeneratedKeys();
                rs.next();
                auto_id = rs.getInt(1);

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return auto_id;
    }

    public void deleteScienceOrFiction2Link() {

        try {

            String sql = "DELETE FROM scienceorfiction_2_link";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteSection() {

        try {

            String sql = "DELETE FROM section";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int insertSection(Map<String, Object> values) {

        int auto_id = -1;

        try {

            String sql = "INSERT INTO section (" +
                    "episode_id, type, number, start, start_android1_premium, start_android1_normal, created, updated," +
                    "intro_title, intro_description," +
                    "thisday_title, thisday_description, thisday_day, thisday_month," +
                    "news_title," +
                    "noisy_title, noisy_answer," +
                    "interview_with, interview_description," +
                    "science_title, science_description, science_science," +
                    "quote_text, quote_bywhom," +
                    "email_title, email_message," +
                    "review_title, review_description," +
                    "logical_title)" +
                    " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"; // 29

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                statement.setInt(1, (Integer)values.get(EPISODE_ID));
                statement.setString(2, (String) values.get(SECTION_TYPE));
                statement.setInt(3, (Integer) values.get(SECTION_NUMBER));
                statement.setString(4, (String)values.get(SECTION_START));
                statement.setString(5, (String)values.get(SECTION_START));
                statement.setString(6, "");
                statement.setObject(7, new Timestamp(new Date().getTime()));
                statement.setObject(8, new Timestamp(new Date().getTime()));

                statement.setString(9, (String)values.get(INTRO_TITLE));
                statement.setString(10, (String)values.get(INTRO_DESCRIPTION));

                statement.setString(11, (String)values.get(THISDAY_TITLE));
                statement.setString(12, (String)values.get(THISDAY_DESCRIPTION));
                statement.setInt(13, (Integer)values.get(THISDAY_DAY));
                statement.setInt(14, (Integer)values.get(THISDAY_MONTH));

                statement.setString(15, (String)values.get(NEWS_TITLE));

                statement.setString(16, (String)values.get(NOISY_TITLE));
                statement.setString(17, (String)values.get(NOISY_ANSWER));

                statement.setString(18, (String)values.get(INTERVIEW_WITH));
                statement.setString(19, (String)values.get(INTERVIEW_DESCRIPTION));

                statement.setString(20, (String)values.get(SCIENCE_TITLE));
                statement.setString(21, (String)values.get(SCIENCE_DESCRIPTION));
                statement.setBoolean(22, (Boolean)values.get(SCIENCE_SCIENCE));

                statement.setString(23, (String)values.get(QUOTE_TEXT));
                statement.setString(24, (String)values.get(QUOTE_BYWHOM));

                statement.setString(25, (String)values.get(EMAIL_TITLE));
                statement.setString(26, (String)values.get(EMAIL_MESSAGE));

                statement.setString(27, (String)values.get(REVIEW_TITLE));
                statement.setString(28, (String)values.get(REVIEW_DESCRIPTION));

                statement.setString(29, (String)values.get(LOGICAL_TITLE));

                statement.executeUpdate();

                ResultSet rs = statement.getGeneratedKeys();
                rs.next();
                auto_id = rs.getInt(1);

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return auto_id;
    }

    public void deleteThisDayInSkepticism() {

        try {

            String sql = "DELETE FROM thisdayinskepticism";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int insertThisDayInSkepticism(Map<String, Object> values) {

        int auto_id = -1;

        try {

            String sql = "INSERT INTO thisdayinskepticism (episode_id, title, description, day, month, created, updated) VALUES (?,?,?,?,?,?,?)";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                statement.setInt(1, (Integer)values.get(EPISODE_ID));
                //statement.setString(2, (String) values.get(THISDAYINSKEPTICISM_TITLE));
                //statement.setString(3, (String)values.get(THISDAYINSKEPTICISM_DESCRIPTION));
                //statement.setInt(4, (Integer) values.get(THISDAYINSKEPTICISM_DAY));
                //statement.setInt(5, (Integer) values.get(THISDAYINSKEPTICISM_MONTH));
                statement.setObject(6, new Timestamp(new Date().getTime()));
                statement.setObject(7, new Timestamp(new Date().getTime()));

                statement.executeUpdate();

                ResultSet rs = statement.getGeneratedKeys();
                rs.next();
                auto_id = rs.getInt(1);

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return auto_id;
    }

    public void deleteNewsItem() {

        try {

            String sql = "DELETE FROM newsitem";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int insertNewsItem(Map<String, Object> values) {

        int auto_id = -1;

        try {

            String sql = "INSERT INTO newsitem (episode_id, title, tags, created, updated) VALUES (?,?,?,?,?)";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                statement.setInt(1, (Integer)values.get(EPISODE_ID));
                //statement.setString(2, (String) values.get(NEWSITEM_TITLE));
                //statement.setString(3, (String)values.get(NEWSITEM_TAGS));
                statement.setObject(4, new Timestamp(new Date().getTime()));
                statement.setObject(5, new Timestamp(new Date().getTime()));

                statement.executeUpdate();

                ResultSet rs = statement.getGeneratedKeys();
                rs.next();
                auto_id = rs.getInt(1);

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return auto_id;
    }

    public void deleteNewsItem2Link() {

        try {

            String sql = "DELETE FROM newsitem_2_link";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertNewsItem2Link(int newsitem_id, int link_id) {

        try {

            String sql = "INSERT INTO newsitem_2_link (newsitem_id, link_id) VALUES (?,?)";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.setInt(1, newsitem_id);
                statement.setInt(2, link_id);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void deleteSection2Link() {

        try {

            String sql = "DELETE FROM section_2_link";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertSection2Link(int section_id, int link_id) {

        try {

            String sql = "INSERT INTO section_2_link (section_id, link_id) VALUES (?,?)";

            Connection connection = null;
            try {

                connection = DriverManager.getConnection(url);
                PreparedStatement statement = connection.prepareStatement(sql);

                statement.setInt(1, section_id);
                statement.setInt(2, link_id);

                statement.executeUpdate();

            } finally {
                connection.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
