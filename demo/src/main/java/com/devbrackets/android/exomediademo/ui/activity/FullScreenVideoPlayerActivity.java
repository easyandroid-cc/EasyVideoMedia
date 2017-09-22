package com.devbrackets.android.exomediademo.ui.activity;

import android.annotation.TargetApi;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.devbrackets.android.exomedia.listener.VideoControlsVisibilityListener;
import com.devbrackets.android.exomedia.ui.widget.VideoControls;
import com.devbrackets.android.exomedia.ui.widget.VideoView;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
import static android.view.View.SYSTEM_UI_FLAG_LOW_PROFILE;
import static android.view.View.SYSTEM_UI_FLAG_VISIBLE;

/**
 * A simple example of making a fullscreen video player activity.
 * <p>
 * <b><em>NOTE:</em></b> the VideoView setup is done in the {@link VideoPlayerActivity}
 */
public class FullScreenVideoPlayerActivity extends VideoPlayerActivity {
    private FullScreenListener fullScreenListener = new FullScreenListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUiFlags();
        System.out.println("cgp onCreate");
        if (videoView.getVideoControls() != null) {
            videoView.getVideoControls().setVisibilityListener(new ControlsVisibilityListener());
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        exitFullscreen();
        System.out.println("cgp onDestroy");
    }

    private void goFullscreen() {
        setUiFlags(true);
    }

    private void exitFullscreen() {
        setUiFlags(false);
    }

    /**
     * Correctly sets up the fullscreen flags to avoid popping when we switch
     * between fullscreen and not
     */
    private void initUiFlags() {
        int flags = SYSTEM_UI_FLAG_VISIBLE;

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            flags |= SYSTEM_UI_FLAG_LAYOUT_STABLE
////                    | SYSTEM_UI_FLAG_FULLSCREEN
//                    | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
//        }

        View decorView = getWindow().getDecorView();
        if (decorView != null) {
            decorView.setSystemUiVisibility(flags);
            decorView.setOnSystemUiVisibilityChangeListener(fullScreenListener);
        }
    }

    /**
     * Applies the correct flags to the windows decor view to enter
     * or exit fullscreen mode
     *
     * @param fullscreen True if entering fullscreen mode
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    protected void setUiFlags(boolean fullscreen) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            View decorView = getWindow().getDecorView();
            if (decorView != null) {
                decorView.setSystemUiVisibility(fullscreen ? updateNavVisibility() : SYSTEM_UI_FLAG_VISIBLE);
            }
        }
    }
//.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

    /**
     * Determines the appropriate fullscreen flags based on the
     * systems API version.
     *
     * @return The appropriate decor view flags to enter fullscreen mode when supported
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private int getFullscreenUiFlags() {
        int flags = SYSTEM_UI_FLAG_LOW_PROFILE;//| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        return flags;
    }

    //这个状态会隐藏通知栏和nav 栏，适合在横屏使用，滑动通知栏都会出现
    int updateNavVisibility() {
        int newVis = SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | SYSTEM_UI_FLAG_LAYOUT_STABLE;

        newVis |= SYSTEM_UI_FLAG_LOW_PROFILE | SYSTEM_UI_FLAG_FULLSCREEN
                | SYSTEM_UI_FLAG_HIDE_NAVIGATION | SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        return newVis;
    }

    @Override
    public void onBackPressed() {
        System.out.println("isLandscape()=" + isLandscape());
        if (isLandscape()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            super.onBackPressed();
        }
    }

    private boolean isLandscape() {
        System.out.println("getRequestedOrientation()=" + getRequestedOrientation());
        int requestedOrientation = getRequestedOrientation();
        switch (requestedOrientation){
            case  ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
            case  ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE:
                return true;
        }
        return false;
    }

    /**
     * Listens to the system to determine when to show the default controls
     * for the {@link VideoView}
     */
    private class FullScreenListener implements View.OnSystemUiVisibilityChangeListener {
        @Override
        public void onSystemUiVisibilityChange(int visibility) {
            if ((visibility & SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                videoView.showControls();
            }
        }
    }

    /**
     * A Listener for the {@link VideoControls}
     * so that we can re-enter fullscreen mode when the controls are hidden.
     */
    private class ControlsVisibilityListener implements VideoControlsVisibilityListener {
        @Override
        public void onControlsShown() {
            // No additional functionality performed
        }

        @Override
        public void onControlsHidden() {
            //竖屏幕才执行ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                goFullscreen();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        content.setVisibility(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? View.GONE : View.VISIBLE);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
            goFullscreen();
        } else {
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
            exitFullscreen();
        }

        System.out.println("cgp newConfig getLayoutDirection=" + newConfig.getLayoutDirection());
        System.out.println("cgp newConfig orientation=" + newConfig.orientation);
    }


    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("cgp onStart");
    }
}
