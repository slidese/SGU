
package se.slide.sgu;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
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

    public static final int MODE_ADFREE = 0;
    public static final int MODE_PREMIUM = 1;

    private ContentListener mListener;

    ListView mListview;
    Button mPlayButton;
    ContentAdapter mAdapter;
    RelativeLayout mNoContent;
    TextView mNoContentMessage;

    public ContentFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (ContentListener) activity;
            
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ContentListener");
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
            }
        });
        
        mNoContent = (RelativeLayout) view.findViewById(R.id.holder_no_content);
        mNoContentMessage = (TextView) view.findViewById(R.id.message_no_content);

        /*
         * View footerView = inflater.inflate(R.layout.footer, null, false);
         * mListview.addFooterView(footerView);
         */

        // LinearLayout playerLinearLayout = (LinearLayout)
        // view.findViewById(R.id.player_linearlayout);
        // playerLinearLayout.setBackground(new
        // ColorDrawable(Color.parseColor("#aa000000")));

        /*
         * mPlayButton = (ImageButton) view.findViewById(R.id.playButton);
         * mPlayButton.setOnClickListener(new OnClickListener() {
         * @Override public void onClick(View view) {
         * Toast.makeText(view.getContext(), "Click!",
         * Toast.LENGTH_SHORT).show(); } });
         */

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
        
        new FetchContentAsyncTask(mListener.getMode()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        
    }
    
    private void setAdapter(List<Content> listOfContent) {
        mAdapter = new ContentAdapter(getActivity(), R.layout.list_item_card, listOfContent);
        mListview.setAdapter(mAdapter);
        
        String username = GlobalContext.INSTANCE.getPreferenceString("username", null);

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
