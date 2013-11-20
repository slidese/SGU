package se.slide.sgu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Spinner;

import se.slide.sgu.db.DatabaseManager;
import se.slide.sgu.model.Episode;

import java.util.List;

public class ContentTranscriptFragment extends Fragment {
    
    private final String TAG = "ContentTranscriptFragment";
    
    public static final String CONTENT_GUID = "content_guid";
    
    private WebView mWeb;

    public ContentTranscriptFragment() {
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        String guid = getArguments().getString(CONTENT_GUID);
        
        Episode episode = DatabaseManager.getInstance().getEpisodeBy(guid);
        
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
        
        return view;
    }
    
}
