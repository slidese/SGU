package se.slide.sgu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

import se.slide.sgu.db.DatabaseManager;
import se.slide.sgu.model.Content;
import se.slide.sgu.model.Link;

import java.util.ArrayList;
import java.util.List;

public class ContentLinksFragment extends Fragment {

    private final String TAG = "ContentLinksFragment";
    
    public static final String CONTENT_MP3 = "content_mp3";
    
    private Spinner mUrls;

    public ContentLinksFragment() {
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        String mp3 = getArguments().getString(CONTENT_MP3);
        
        View view = inflater.inflate(R.layout.webview_holder, null);
        
        final WebView web = (WebView) view.findViewById(R.id.web);
        //web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setSupportZoom(true);
        web.getSettings().setBuiltInZoomControls(true);
        web.getSettings().setUseWideViewPort(true);
        web.getSettings().setLoadWithOverviewMode(true);
        
        mUrls = (Spinner) view.findViewById(R.id.urls);

        List<Link> listOfLinks = DatabaseManager.getInstance().getLinks(mp3);
        
        final LinkAdapter adapter = new LinkAdapter(getActivity(), R.layout.spinner_item, listOfLinks);
        mUrls.setAdapter(adapter);
        mUrls.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Link link = adapter.getItem(position);
                web.loadUrl(link.url);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                
            }
            
        });
        
        return view;
    }
    
}
