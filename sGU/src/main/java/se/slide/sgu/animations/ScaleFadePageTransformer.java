package se.slide.sgu.animations;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;


public class ScaleFadePageTransformer implements ViewPager.PageTransformer {

    private int mScreenXOffset;
    
    public ScaleFadePageTransformer(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        mScreenXOffset = display.getWidth()/2;
    }
        
    @SuppressLint("NewApi")
    @Override
    public void transformPage(View page, float position) {
        final float transformValue = Math.abs(Math.abs(position) - 1);
        // apply fade effect
        page.setAlpha(transformValue);
        if (position > 0) {
            // apply zoom effect only for pages to the right
            page.setScaleX(transformValue);
            page.setScaleY(transformValue);
            page.setPivotX(0.5f);
            final float translateValue = position * -mScreenXOffset;
            if (translateValue > -mScreenXOffset) {
                page.setTranslationX(translateValue);
            } else {
                page.setTranslationX(0);
            }
        }
    }
}
