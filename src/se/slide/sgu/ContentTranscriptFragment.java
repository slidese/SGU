package se.slide.sgu;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Spinner;
import android.widget.TextView;

import de.l3s.boilerpipe.extractors.ArticleExtractor;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import se.slide.sgu.db.DatabaseManager;
import se.slide.sgu.model.Episode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class ContentTranscriptFragment extends Fragment {
    
    private final String TAG = "ContentTranscriptFragment";
    
    public static final String CONTENT_MP3 = "content_mp3";
    
    private WebView mWeb;

    public ContentTranscriptFragment() {
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        String mp3 = getArguments().getString(CONTENT_MP3);
        
        Episode episode = getEpisode(mp3);
        
        View view = inflater.inflate(R.layout.webview_holder, null);
        
        mWeb = (WebView) view.findViewById(R.id.web);
        Spinner spinner = (Spinner) view.findViewById(R.id.urls);
        
        // Hide the spinner in this view
        spinner.setVisibility(View.GONE);
        
        mWeb.getSettings().setJavaScriptEnabled(true);
        mWeb.getSettings().setSupportZoom(true);
        mWeb.getSettings().setBuiltInZoomControls(true);
        mWeb.getSettings().setUseWideViewPort(true);
        mWeb.getSettings().setLoadWithOverviewMode(true);
        
        if (episode != null && episode.transcript != null)
            mWeb.loadUrl(episode.transcript);
        
        //new MeAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
        
        return view;
    }
    
    private Episode getEpisode(String mp3) {
        List<Episode> listOfEpisodes = DatabaseManager.getInstance().getEpisode(mp3);
        
        if (listOfEpisodes != null && listOfEpisodes.size() > 0)
            return listOfEpisodes.get(0);
        else
            return null;
    }
    
    private class MeAsyncTask extends AsyncTask {

        @Override
        protected String doInBackground(Object... params) {
            
            String ret = "";
            
            HttpClient httpclient = new DefaultHttpClient();

            // Prepare a request object
            HttpGet httpget = new HttpGet("http://www.sgutranscripts.org/wiki/SGU_Episode_426"); 

            // Execute the request
            HttpResponse response;
            try {
                response = httpclient.execute(httpget);
                // Examine the response status
                Log.i("Praeda",response.getStatusLine().toString());

                // Get hold of the response entity
                HttpEntity entity = response.getEntity();
                // If the response does not enclose an entity, there is no need
                // to worry about connection release

                if (entity != null) {

                    // A Simple JSON Response Read
                    InputStream instream = entity.getContent();
                    String result= convertStreamToString(instream);
                    // now you have the string representation of the HTML request
                    
                    String text = ArticleExtractor.INSTANCE.getText(result);
                    
                    ret = text;
                    
                    instream.close();
                }


            } catch (Exception e) {}
            
            return ret;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            
            //mContent.setText((String)result);
        }
        
    }
    
    private static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
