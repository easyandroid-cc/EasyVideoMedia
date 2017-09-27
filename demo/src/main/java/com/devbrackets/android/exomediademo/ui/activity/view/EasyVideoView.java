package com.devbrackets.android.exomediademo.ui.activity.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import com.devbrackets.android.exomedia.ui.widget.VideoView;

/**
 * Created by cgpllx on 2017/9/27.
 */

public class EasyVideoView extends VideoView {

    public EasyVideoView(Context context) {
        super(context);
    }

    public EasyVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EasyVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EasyVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


}
