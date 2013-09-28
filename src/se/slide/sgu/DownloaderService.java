
package se.slide.sgu;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import se.slide.sgu.db.DatabaseManager;
import se.slide.sgu.model.Content;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

// Get context in a service? http://stackoverflow.com/questions/987072/using-application-context-everywhere

public class DownloaderService extends Service {
    
    public static final String CONTENT_UPDATED = "se.slide.sgu.CONTENT_UPDATED";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String username = PreferenceManager.getDefaultSharedPreferences(this).getString("username", null);
        String password = PreferenceManager.getDefaultSharedPreferences(this).getString("password", null);

        if (username == null || password == null)
            stopSelf();

        new DownloadAsyncTask(username, password);

        return super.onStartCommand(intent, flags, startId);
    }

    private class DownloadAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private String username;
        private String password;

        public DownloadAsyncTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean returnValue = true;

            StringBuilder builder = new StringBuilder();

            DefaultHttpClient httpclient = new DefaultHttpClient();

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

            try {
                HttpResponse response = httpclient.execute(httpost);

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
                    Utils.sendExceptionToGoogleAnalytics(getApplicationContext(), Thread.currentThread().getName(), e, false);
                }

            } catch (ClientProtocolException e) {
                e.printStackTrace();
                returnValue = false;
                Utils.sendExceptionToGoogleAnalytics(getApplicationContext(), Thread.currentThread().getName(), e, false);
            } catch (IOException e) {
                e.printStackTrace();
                returnValue = false;
                Utils.sendExceptionToGoogleAnalytics(getApplicationContext(), Thread.currentThread().getName(), e, false);
            }

            // Try to parse the html content
            try {
                parseMembersOnlyHtml(builder.toString());
            } catch (Exception e) {
                e.printStackTrace();
                returnValue = false;
                Utils.sendExceptionToGoogleAnalytics(getApplicationContext(), Thread.currentThread().getName(), e, false);
            }

            return returnValue;

        }

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

            DatabaseManager.getInstance().addContent(listOfContent);

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            
            if (result) {
                Intent intent = new Intent();
                intent.setAction(CONTENT_UPDATED);
                sendBroadcast(intent);    
            }
            else {
                // Show notification?
            }
            
        }

    }
}
