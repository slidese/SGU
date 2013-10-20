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

import java.util.ArrayList;
import java.util.List;

public class ContentLinksFragment extends Fragment {

    private final String TAG = "ContentTranscriptFragment";
    
    public static final String CONTENT_MP3 = "content_mp3";
    
    private WebView mWeb;
    private Spinner mUrls;

    public ContentLinksFragment() {
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        String mp3 = getArguments().getString(CONTENT_MP3);
        
        View view = inflater.inflate(R.layout.webview_holder, null);
        
        //mWeb = (WebView) view.findViewById(R.id.web);
        final WebView web = (WebView) view.findViewById(R.id.web);
        mUrls = (Spinner) view.findViewById(R.id.urls);
        
        List<Link> listOfLinks = new ArrayList<Link>();
        
        Link l1 = new Link();
        l1.title = "Moviets";
        l1.url ="http://www.dn.se";
        
        Link l2 = new Link();
        l2.title = "The Skeptical Intryou";
        l2.url ="http://www.google.se";
        
        listOfLinks.add(l1);
        listOfLinks.add(l2);
        
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
