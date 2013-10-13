
package se.slide.sgu;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import se.slide.sgu.db.DatabaseManager;
import se.slide.sgu.model.Content;

import java.util.List;

public class ContentFragment extends Fragment {
    
    private final String TAG = "ContentFragment";

    public static final String CONTENT_MODE = "content_mode";
    
    public static final int MODE_ADFREE = 0;
    public static final int MODE_PREMIUM = 1;

    private StartActivity mListener;

    ListView mListview;
    Button mPlayButton;
    ContentAdapter mAdapter;
    RelativeLayout mNoContent;
    TextView mNoContentMessage;
    int mMode;

    public ContentFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (StartActivity) activity;
            
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must be the StartActivity");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_content, null);

        mListview = (ListView) view.findViewById(android.R.id.list);
        mListview.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                // This will be implemented in version 2
                //Toast.makeText(view.getContext(), "Click!", Toast.LENGTH_SHORT).show();
                Content content = mAdapter.getItem(position);
                mListener.showContentDetails(content);
            }
        });
        
        mNoContent = (RelativeLayout) view.findViewById(R.id.holder_no_content);
        mNoContentMessage = (TextView) view.findViewById(R.id.message_no_content);
        
        mMode = getArguments().getInt(CONTENT_MODE);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateAdapter();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        
    }

    public void refresh() {
        updateAdapter();
    }

    private void updateAdapter() {
        Log.d(TAG, "updateAdapter");
        
        //new FetchContentAsyncTask(mMode).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        
        List<Content> listOfContent;
        if (mMode == MODE_ADFREE)
            listOfContent = DatabaseManager.getInstance().getAdFreeContents();
        else
            listOfContent = DatabaseManager.getInstance().getPremiumContents();
        
        setAdapter(listOfContent);
    }
    
    private void setAdapter(List<Content> listOfContent) {
        mAdapter = new ContentAdapter(getActivity(), R.layout.list_item_card, listOfContent);
        mListview.setAdapter(mAdapter);
        
        String username = GlobalContext.INSTANCE.getPreferenceString("username", null);

        /*
        if (mListview.getCount() < 1) {
            mListview.setVisibility(View.GONE);
            mNoContent.setVisibility(View.VISIBLE);
            mNoContentMessage.setText(Html.fromHtml(getString(R.string.no_content_no_episodes)));
        }
        else if (username == null) {
            mListview.setVisibility(View.GONE);
            mNoContent.setVisibility(View.VISIBLE);
            mNoContentMessage.setText(Html.fromHtml(getString(R.string.no_content_getting_started)));
        }
        else {
            mListview.setVisibility(View.VISIBLE);
            mNoContent.setVisibility(View.GONE);
        }
        */
        
        
    }
    
    private class FetchContentAsyncTask extends AsyncTask<Void, Void, List<Content>> {
        
        private int mode = MODE_ADFREE;
        
        public FetchContentAsyncTask(int mode) {
            this.mode = mode;
        }
        
        @Override
        protected List<Content> doInBackground(Void... params) {
            List<Content> listOfContent = null;
            if (mode == MODE_ADFREE)
                listOfContent = DatabaseManager.getInstance().getAdFreeContents();
            else
                listOfContent = DatabaseManager.getInstance().getPremiumContents();
            
            return listOfContent;
        }

        @Override
        protected void onPostExecute(List<Content> listOfContent) {
            super.onPostExecute(listOfContent);
            
            setAdapter(listOfContent);
        }
    }
}
