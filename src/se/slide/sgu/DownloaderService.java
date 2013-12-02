
package se.slide.sgu;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Base64;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.developerworks.android.FeedParser;
import org.developerworks.android.FeedParserFactory;
import org.developerworks.android.Message;
import org.developerworks.android.ParserType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParserException;

import se.slide.sgu.db.DatabaseManager;
import se.slide.sgu.model.Content;
import se.slide.sgu.model.Episode;
import se.slide.sgu.model.Item;
import se.slide.sgu.model.Section;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Get context in a service? http://stackoverflow.com/questions/987072/using-application-context-everywhere

public class DownloaderService extends Service {
    
    private final String TAG = "DownloaderService";
    
    public static final String ACTION_DOWNLOAD_STARTED     = "se.slide.sgu.intent.action.DOWNLOAD_STARTED";
    public static final String ACTION_DOWNLOAD_FINISHED    = "se.slide.sgu.intent.action.DOWNLOAD_FINISHED";
    
    public static final String EXTRA_USER_INITIATED        = "se.slide.sgu.intent.extra.USER_INITIATED";
    public static final String EXTRA_DOWNLOAD_STATE        = "se.slide.sgu.intent.extra.DOWNLOAD_STATE";
    
    private final int NOTIFICATION_ID                      = 13;
    private final int NOTIFICATION_NEW                     = 14;
    
    private MetadataAsyncTask mMetadataAsyncTask           = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLog.v(TAG, "Started DownloaderService");
        
        DatabaseManager.init(this);
        GlobalContext.INSTANCE.init(this);
        ContentDownloadManager.INSTANCE.init(this);

        String username = PreferenceManager.getDefaultSharedPreferences(this).getString("username", null);
        String password = PreferenceManager.getDefaultSharedPreferences(this).getString("password", null);
        
        if (username == null || password == null) {
            MyLog.v(TAG, "Username or password is null, stopping service");
            stopSelf();
            return START_NOT_STICKY;
        }
        
        // Are we already running?
        if (mMetadataAsyncTask != null) {
            MyLog.v(TAG, "AsyncTask is not null which means we're already running, stop new request");
            return START_NOT_STICKY;
        }
        
        // Is this automatic or user initiated?
        boolean manuallyStarted = false;
        manuallyStarted = intent.getBooleanExtra(EXTRA_USER_INITIATED, false);
        MyLog.v(TAG, "Is manually started: " + manuallyStarted);
    
        // Let our activity know we have started
        Intent i = new Intent();
        i.setAction(ACTION_DOWNLOAD_STARTED);
        sendBroadcast(i);

        long lastEpisodeInMs = PreferenceManager.getDefaultSharedPreferences(this).getLong("last_episode_in_ms", 0L);
        boolean autoDownload = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("auto_download", false);
        
        // Start metadata download
        mMetadataAsyncTask = new MetadataAsyncTask(username, password, lastEpisodeInMs, autoDownload, manuallyStarted);
        mMetadataAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        
        return START_NOT_STICKY;
    }
    
    private class MetadataAsyncTask extends AsyncTask<Void, Void, Boolean> {
        
        private String username;
        private String password;
        private long lastEpisodeInMs;
        private long latestEpisodeFound = 0L;
        private boolean autoDownload;
        private boolean manuallyStarted = false;

        public MetadataAsyncTask(String username, String password, long lastEpisodeInMs, boolean autoDownload, boolean manuallyStarted) {
            this.username = username;
            this.password = password;
            this.lastEpisodeInMs = lastEpisodeInMs;
            this.autoDownload = autoDownload;
            this.manuallyStarted = manuallyStarted;
        }
        
        @Override
        protected Boolean doInBackground(Void... params) {
            MyLog.v(TAG, "Doing in background: MetadataAsyncTask");
            
            boolean returnValue = true;
            
            StringBuilder builder = new StringBuilder();
            
            HttpUriRequest request = new HttpGet("http://www.x12.se/sgu_metadata.xml");
            HttpClient httpclient = new DefaultHttpClient();
            
            try {
                HttpResponse response = httpclient.execute(request);
                
                String inputLine;
                BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                try {
                    while ((inputLine = in.readLine()) != null) {
                        builder.append(inputLine);
                    }
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    returnValue = false;
                    GlobalContext.INSTANCE.sendExceptionToGoogleAnalytics("While reading inputstream for metadata", Thread.currentThread().getName(), e, false);
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                returnValue = false;
                GlobalContext.INSTANCE.sendExceptionToGoogleAnalytics("While executing HTTP request, client protocol exception", Thread.currentThread().getName(), e, false);
            } catch (IOException e) {
                e.printStackTrace();
                returnValue = false;
                GlobalContext.INSTANCE.sendExceptionToGoogleAnalytics("While executing HTTP request, some IO problem", Thread.currentThread().getName(), e, false);
            }
            
            SectionParser parser = new SectionParser();
            //List<Section> listOfSection = null;
            List<Episode> listOfEpisodes = null;
            try {
                if (Utils.DEBUG)
                    listOfEpisodes = parser.parse(getResources().openRawResource(R.raw.sgu_metadata));
                else
                    listOfEpisodes = parser.parse(new ByteArrayInputStream(builder.toString().getBytes("UTF-8")));
            } catch (NotFoundException e) {
                e.printStackTrace();
                returnValue = false;
                GlobalContext.INSTANCE.sendExceptionToGoogleAnalytics("While parsing metadata, not found exception", Thread.currentThread().getName(), e, false);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                returnValue = false;
                GlobalContext.INSTANCE.sendExceptionToGoogleAnalytics("While parsing metadata, xml pull parser exception", Thread.currentThread().getName(), e, false);
            } catch (IOException e) {
                e.printStackTrace();
                returnValue = false;
                GlobalContext.INSTANCE.sendExceptionToGoogleAnalytics("While parsing metadata, IO exception", Thread.currentThread().getName(), e, false);
            }
            
            if (listOfEpisodes != null) {
                // Clear old items
                DatabaseManager.getInstance().removeSections();
                DatabaseManager.getInstance().removeEpisode();
                DatabaseManager.getInstance().removeItems();
                DatabaseManager.getInstance().removeQuotes();
                DatabaseManager.getInstance().removeGuests();
                DatabaseManager.getInstance().removeLinks();
                
                for (Episode episode : listOfEpisodes) {
                    DatabaseManager.getInstance().addSections(episode.listOfSection);
                    DatabaseManager.getInstance().addGuests(episode.listOfGuests);
                    DatabaseManager.getInstance().addQuote(episode.quote);
                    DatabaseManager.getInstance().addItems(episode.listOfItem);
                    
                    for (Item item : episode.listOfItem) {
                        DatabaseManager.getInstance().addLinks(item.listOfLinks);
                    }
                    
                    for (Section section : episode.listOfSection) {
                        DatabaseManager.getInstance().addLinks(section.listOfLinks);
                    }
                }
                
                DatabaseManager.getInstance().addEpisodes(listOfEpisodes);
            }
            
            return returnValue;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            
            if (!result){
                // Send analytics info since we could not get meatdata
            }
            
            // Start RSS download
            new DownloadAsyncTask(username, password, lastEpisodeInMs, autoDownload, manuallyStarted).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            
        }
        
    }

    private class DownloadAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private String username;
        private String password;
        private long lastEpisodeInMs;
        private long latestEpisodeFound = 0L;
        private boolean autoDownload;
        private boolean manuallyStarted = false;
        private boolean newContent = false;

        public DownloadAsyncTask(String username, String password, long lastEpisodeInMs, boolean autoDownload, boolean manuallyStarted) {
            this.username = username;
            this.password = password;
            this.lastEpisodeInMs = lastEpisodeInMs;
            this.autoDownload = autoDownload;
            this.manuallyStarted = manuallyStarted;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            MyLog.v(TAG, "Doing in background: DownloadAsyncTask");
            
            boolean returnValue = true;

            StringBuilder builder = new StringBuilder();

            //DefaultHttpClient httpclient = new DefaultHttpClient();

            /*
            httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);

            HttpPost httpost = new HttpPost("http://www.theskepticsguide.org/wp-login.php");

            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("log", username));
            nvps.add(new BasicNameValuePair("pwd", password));
            nvps.add(new BasicNameValuePair("rememberme", "forever"));
            nvps.add(new BasicNameValuePair("redirect_to", "/members-only-content"));
            nvps.add(new BasicNameValuePair("wp-submit", "Login"));

            try {
                httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                returnValue = false;
                Utils.sendExceptionToGoogleAnalytics(getApplicationContext(), Thread.currentThread().getName(), e, false);
            }
            */
            
            HttpUriRequest request = new HttpGet("https://www.theskepticsguide.org/premium");
            String credentials = username + ":" + password;
            String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
            request.addHeader("Authorization", "Basic " + base64EncodedCredentials);

            HttpClient httpclient = new DefaultHttpClient();

            try {
                HttpResponse response = httpclient.execute(request);

                String inputLine;
                BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                try {
                    while ((inputLine = in.readLine()) != null) {
                        builder.append(inputLine);
                    }
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    returnValue = false;
                    GlobalContext.INSTANCE.sendExceptionToGoogleAnalytics("While reading RSS inputstream, IO exception", Thread.currentThread().getName(), e, false);
                }

            } catch (ClientProtocolException e) {
                e.printStackTrace();
                returnValue = false;
                GlobalContext.INSTANCE.sendExceptionToGoogleAnalytics("While executing HTTP request, client protocol exception", Thread.currentThread().getName(), e, false);
            } catch (IOException e) {
                e.printStackTrace();
                returnValue = false;
                GlobalContext.INSTANCE.sendExceptionToGoogleAnalytics("While executing HTTP request, IO exception", Thread.currentThread().getName(), e, false);
            }

            // Try to parse the content
            try {
                parseRss(builder.toString());
            } catch (Exception e) {
                e.printStackTrace();
                returnValue = false;
                GlobalContext.INSTANCE.sendExceptionToGoogleAnalytics("While parsing RSS, exception", Thread.currentThread().getName(), e, false);
            }

            return returnValue;

        }

        private void parseRss(String rss) throws Exception {
            FeedParser parser = FeedParserFactory.getParser(ParserType.ANDROID_SAX, rss);
            List<Message> messages = parser.parse();
            
            List<Content> listOfContent = new ArrayList<Content>();
            
            for (Message message : messages) {
                Content content = new Content();
                content.title = message.getTitle();
                content.description = message.getDescription();
                content.mp3 = message.getEnclosureUrl().toExternalForm();
                content.length = message.getEnclosureLength();
                content.published = message.getDateObject();
                content.guid = message.getGuId();
                
                listOfContent.add(content);
            }
            
            for (Content content : listOfContent) {
                Date published = content.published;
                
                // Keep track of the latest episode date
                long t = published.getTime();
                if (t > latestEpisodeFound)
                    latestEpisodeFound = t;
                
                if (autoDownload && lastEpisodeInMs != 0 && t > lastEpisodeInMs) {
                    ContentDownloadManager.INSTANCE.addToDownloadQueue(content.mp3, content.title, content.description, Utils.formatFilename(content.title));
                    newContent = true;
                }
            }
            
            DatabaseManager.getInstance().createIfNotExistsContents(listOfContent);
        }
        
        /**
         * Legacy method for parsing the members content Wordpress-page
         * 
         * @param html
         * @throws Exception
         */
        private void parseMembersOnlyHtml(String html) throws Exception {
            Document doc = Jsoup.parse(html);

            Elements elements = doc.select("div.premium");

            List<Content> listOfContent = new ArrayList<Content>();

            for (int i = 0; i < elements.size(); i++) {
                Element e = elements.get(i);

                String mp3 = null;
                String title = null;
                String description = null;

                // First get mp3 filename
                Elements mp3s = e.select("a[href$=.mp3]");
                if (mp3s.size() == 1) {
                    Element href = mp3s.first();
                    mp3 = href.attr("href");
                }

                Elements spanTitles = e.select("h5.section-title span");
                if (spanTitles.size() == 1) {
                    Element span = spanTitles.first();
                    title = span.text();
                }

                Elements divDescriptions = e.select("div.podcasts-description");
                if (divDescriptions.size() == 1) {
                    Element div = divDescriptions.first();
                    description = div.text();
                }

                if (mp3 == null || title == null || description == null)
                    continue;

                listOfContent.add(new Content(title, description, mp3));

            }

            DatabaseManager.getInstance().createIfNotExistsContents(listOfContent);

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            
            if (result) {
                if (manuallyStarted && newContent) {
                    Notification notification = GlobalContext.INSTANCE.buildNotification(getString(R.string.download_new_ticker), getString(R.string.download_new_title), getString(R.string.download_new_text));
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.notify(NOTIFICATION_NEW, notification);
                }
                
                GlobalContext.INSTANCE.savePreference("last_episode_in_ms", latestEpisodeFound);
                
            }
            else {
                Notification notification = GlobalContext.INSTANCE.buildNotification(getString(R.string.download_error_ticker), getString(R.string.download_error_title), getString(R.string.download_error_text));
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(NOTIFICATION_ID, notification);
            }
            
            GlobalContext.INSTANCE.resetContentCache();
            
            Intent intent = new Intent();
            intent.setAction(ACTION_DOWNLOAD_FINISHED);
            intent.putExtra(DownloaderService.EXTRA_DOWNLOAD_STATE, result);
            sendBroadcast(intent);
            
            stopSelf();
        }

    }
}
