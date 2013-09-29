package se.slide.sgu;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class PlayerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        View view = inflater.inflate(R.layout.fragment_player, null);
        
        LinearLayout playerLinearLayout = (LinearLayout) view.findViewById(R.id.player_linearlayout);
        playerLinearLayout.setBackground(new ColorDrawable(Color.parseColor("#88000000")));
        
        return view;
    }
}
