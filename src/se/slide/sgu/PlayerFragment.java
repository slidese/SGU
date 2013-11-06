package se.slide.sgu;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class PlayerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        View view = inflater.inflate(R.layout.fragment_player, null);
        
        final LinearLayout playerLinearLayout = (LinearLayout) view.findViewById(R.id.player_linearlayout);
        //playerLinearLayout.setBackground(new ColorDrawable(Color.parseColor("#88000000")));
        
        final LinearLayout playerMainInfoLinearLayout = (LinearLayout) view.findViewById(R.id.player_maininfo_linearlayout);
        //playerMainInfoLinearLayout.setBackground(new ColorDrawable(Color.parseColor("#dd000000")));
        
        ImageButton showContentButton = (ImageButton) view.findViewById(R.id.showContentButton);
        showContentButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (playerMainInfoLinearLayout.getVisibility() == View.VISIBLE) {
                    AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
                    anim.setDuration(250);
                    anim.setFillAfter(true);
                    //anim.setRepeatMode(Animation.REVERSE);
                    playerMainInfoLinearLayout.startAnimation(anim);
                    
                    playerMainInfoLinearLayout.setVisibility(View.INVISIBLE);
                    playerMainInfoLinearLayout.setClickable(false);
                }
                else {
                    AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
                    anim.setDuration(250);
                    anim.setFillAfter(true);
                    playerMainInfoLinearLayout.startAnimation(anim);
                    
                    playerMainInfoLinearLayout.setVisibility(View.VISIBLE);
                    playerMainInfoLinearLayout.setClickable(true);
                }
                    
            }
        });
        
        return view;
    }
}
