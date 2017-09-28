package com.devbrackets.android.exomediademo.ui.activity.view;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.AttributeSet;
import android.view.View;

public class HorizontalPortraitSwitchingButton extends android.support.v7.widget.AppCompatImageButton {


    public HorizontalPortraitSwitchingButton(Context context) {
        super(context);
        setup(context);
    }

    public HorizontalPortraitSwitchingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    public HorizontalPortraitSwitchingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context);
    }

    protected void setup(Context context) {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeOrientation();
            }
        });
    }

    private synchronized void changeOrientation() {
        Context context = getContext();
        Activity activity;
        if (!(context instanceof Activity)) {
            return;
        }
        activity = (Activity) context;

        int requestedOrientation = activity.getRequestedOrientation();

        switch (requestedOrientation) {
            case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            default:
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
        }

    }
}
