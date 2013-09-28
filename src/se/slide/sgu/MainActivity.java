
package se.slide.sgu;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.analytics.tracking.android.EasyTracker;

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
import org.codechimp.apprater.AppRater;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import se.slide.sgu.db.DatabaseManager;
import se.slide.sgu.model.Content;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {
    
    private static String TAG = "MainActivity";

    ListView mListview;
    ContentAdapter mAdapter;
    
    private BroadcastReceiver downloadBroadcastReceiver = new DownloadBroadcastReceiver();
    private ServiceConnection serviceConnection = new AudioPlayerServiceConnection();
    private AudioPlayer audioPlayer;
    private Intent audioPlayerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        /*
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        ActionBar actionBar = getActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#aa000000")));
        actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#aa000000")));
        */
        
        
        /*
        FadingActionBarHelper helper = new FadingActionBarHelper()
        .actionBarBackground(R.drawable.ab_background_textured_example) // Color of ActionBar
        .headerLayout(R.layout.header)
        .contentLayout(R.layout.activity_listview);
        
        setContentView(helper.createView(this));
        helper.initActionBar(this);
        */
        
        getActionBar().setTitle("");
        //getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_background_textured_example));
        
        setContentView(R.layout.fragment_adfreecontent);

        DatabaseManager.init(this);
        
        mListview = (ListView) findViewById(android.R.id.list);
        
        mListview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                
                //Toast.makeText(view.getContext(), "Yo", Toast.LENGTH_SHORT).show();
                
                MediaPlayer player = new MediaPlayer();
                int cupo = player.getCurrentPosition();
                
                System.out.println(cupo);
                
                Content content = mAdapter.getItem(position);
                
                //String filename = formatFilename(content.title) + ".mp3";
                String filename = Utils.formatFilename(content.title);
                
                Uri uri = Uri.parse(content.mp3);
                
                String sguDirectory = Environment.DIRECTORY_DOWNLOADS + "/sgu/";
                
                File checkfile = Environment.getExternalStoragePublicDirectory(sguDirectory + filename);
                
                
                if (checkfile.exists()) {
            
                    /*
                    Intent intent = new Intent(view.getContext(), PlayService.class);
                    intent.setAction(PlayService.ACTION_PLAY);
                    intent.putExtra(PlayService.EXTRA_FILENAME, checkfile.getAbsolutePath());
                    startService(intent);
                    */
                    
                    audioPlayer.addTrack(content);
                    
                }
                
                Environment.getExternalStoragePublicDirectory(sguDirectory).mkdirs();

                DownloadManager mgr = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request req = new DownloadManager.Request(uri);

                req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                   .setAllowedOverRoaming(false)
                   .setTitle(content.title)
                   .setDescription(content.description)
                   .setDestinationInExternalPublicDir(sguDirectory, filename);

                
                mgr.enqueue(req);
                 
                
                 
            }
            
        });
        
        List<Content> listOfContent = DatabaseManager.getInstance().getAllContents();
        updateAdapter(listOfContent);
 
        /*
        String[] items = new String[4];
        items[0] = "Mike";
        items[1] = "Mike2";
        items[2] = "Mike3";
        items[3] = "Mike4";
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        mListview.setAdapter(adapter);
        */
        
        // Set type of progress indicator
        setProgressBarIndeterminate(true);
        
        //bind to service
        audioPlayerIntent = new Intent(this, AudioPlayer.class);
        bindService(audioPlayerIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        AppRater.app_launched(this);
    }
    
    @Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance(this).activityStop(this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        IntentFilter filter = new IntentFilter(DownloaderService.CONTENT_UPDATED);
        registerReceiver(downloadBroadcastReceiver, filter);
        
        //refreshScreen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        unregisterReceiver(downloadBroadcastReceiver);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // maybe AudioPlayer = null;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        if (item.getItemId() == R.id.action_reload) {
            
            String username = PreferenceManager.getDefaultSharedPreferences(this).getString("username", "");
            String password = PreferenceManager.getDefaultSharedPreferences(this).getString("password", "");
            
            new ReloadTask(username, password).execute();
            
            return true;
        }
        else if (item.getItemId() == R.id.action_settings) {
            
            audioPlayer.stop();
            
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        
        return super.onMenuItemSelected(featureId, item);
    }
    
    public void updateAdapter(List<Content> listOfContent) {
        mAdapter = new ContentAdapter(this, R.layout.list_item_card, listOfContent);
        mListview.setAdapter(mAdapter);
    }
    
    public class ReloadTask extends AsyncTask<Void, Void, String> {

        String username;
        String password;
        
        public ReloadTask(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO Auto-generated method stub
            StringBuilder builder = new StringBuilder();
            
            DefaultHttpClient httpclient = new DefaultHttpClient();

            httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
            
            HttpPost httpost = new HttpPost("http://www.theskepticsguide.org/wp-login.php");

            List <NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("log", username));
            nvps.add(new BasicNameValuePair("pwd", password));
            nvps.add(new BasicNameValuePair("rememberme", "forever"));
            nvps.add(new BasicNameValuePair("redirect_to", "/members-only-content"));
            nvps.add(new BasicNameValuePair("wp-submit", "Login"));


            try {
                httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                HttpResponse response = httpclient.execute(httpost);
                
                //StringBuilder builder = new StringBuilder();
                String inputLine;
                BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                try {
                      while ((inputLine = in.readLine()) != null) {
                          builder.append(inputLine);
                             //System.out.println(inputLine);
                      }
                      in.close();
                 } catch (IOException e) {
                      e.printStackTrace();
                 }
                
                //response.getEntity().getContent()
                
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            return builder.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            
            parseMembersOnlyHtml(result);
            
            setProgressBarIndeterminateVisibility(false);
        }
        
    }
    
    public void parseMembersOnlyHtml(String html) {
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
        
        updateAdapter(listOfContent);
        DatabaseManager.getInstance().addContent(listOfContent);
        
    }
    
    public String formatFilename(String filename) {
        int dashIndex = filename.indexOf(" -");
        String name = filename.substring(0, dashIndex);
        name = name.replace("#", "");
        
        return name;
    }
    
    /*
    public void playMp3(String filepath) {
        Intent intent = new Intent();  
        intent.setAction(android.content.Intent.ACTION_VIEW);  
        File file = new File(filepath);
        intent.setDataAndType(Uri.fromFile(file), "audio/*");  
        startActivity(intent);
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    private class DownloadBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"DownloadBroadcastReceiver.onReceive action=" + intent.getAction());
            if(intent.getAction().equals(DownloaderService.CONTENT_UPDATED)) {
                // update playlist
            }
        }
    }
    
    private final class AudioPlayerServiceConnection implements ServiceConnection {
        @SuppressWarnings("unchecked")
        public void onServiceConnected(ComponentName className, IBinder baBinder) {
            Log.d(TAG,"AudioPlayerServiceConnection: Service connected");
            audioPlayer = ((LocalBinder<AudioPlayer>) baBinder).getService();
            startService(audioPlayerIntent);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG,"AudioPlayerServiceConnection: Service disconnected");
            audioPlayer = null;
        }
    }
}
