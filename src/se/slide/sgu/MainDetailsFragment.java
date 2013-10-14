package se.slide.sgu;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import se.slide.sgu.animations.ScaleFadePageTransformer;

import java.util.Locale;

public class MainDetailsFragment extends Fragment implements ActionBar.OnNavigationListener {
    
    private final String TAG = "ContentDetailsFragment";
    
    private SectionsPagerAdapter                mSectionsPagerAdapter;
    private ViewPager                           mViewPager;

    final String[] actions = new String[] {
            "Details",
            "Transcript",
            "Links"
    };
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        
        Log.d(TAG, "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        
        Log.d(TAG, "onStop");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        
        View view = inflater.inflate(R.layout.fragment_viewpager, null);
        
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageTransformer(true, new ScaleFadePageTransformer(getActivity()));
        
        final ActionBar actionBar = getActivity().getActionBar();
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, actions);
        
        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setListNavigationCallbacks(adapter, this);
        
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });
        
        return view;
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        if (itemPosition == 0) {
            mViewPager.setCurrentItem(0, true);
        }
        else if (itemPosition == 1) {
            mViewPager.setCurrentItem(1, true);
        }
        else if (itemPosition == 2) {
            mViewPager.setCurrentItem(2, true);
        }
        
        return false;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DummySectionFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            Fragment fragment = new DummySectionFragment();
            Bundle args = new Bundle();
            args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.tab_details).toUpperCase(l);
                case 1:
                    return getString(R.string.tab_transcript).toUpperCase(l);
                case 2:
                    return getString(R.string.tab_links).toUpperCase(l);
            }
            return null;
        }
    }
    
    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public DummySectionFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            /*
            View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
            TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
            dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
            */
            
            View view = inflater.inflate(R.layout.list_item_card_nocontent, null);
            return view;
        }
    }

    
}
