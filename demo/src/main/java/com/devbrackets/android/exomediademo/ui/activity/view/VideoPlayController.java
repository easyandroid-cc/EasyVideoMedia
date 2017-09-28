package com.devbrackets.android.exomediademo.ui.activity.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.devbrackets.android.exomediademo.R;

/**
 * 音量和屏幕亮度的控制器
 */
public class VideoPlayController extends FrameLayout {
    private View startView;
    private View restartView;
    private View errorView;

    public VideoPlayController(@NonNull Context context) {
        super(context);
        setup(context);
    }

    public VideoPlayController(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    public VideoPlayController(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoPlayController(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup(context);
    }

    protected void setup(Context context) {
        View.inflate(context, R.layout.video_play_control_view, this);
        startView = findViewById(R.id.start);
        restartView = findViewById(R.id.reStart);
        errorView = findViewById(R.id.error);
        startView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onStartClick();
//                    hideAllView();
                }
            }
        });
        restartView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onRestartClick();
//                    hideAllView();
                }
            }
        });
        errorView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onErrorClick();
//                    hideAllView();
                }
            }
        });
    }

    public void showStartView() {
        startView.setVisibility(View.VISIBLE);
        restartView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
    }

    public boolean isShowRestartView() {
        return restartView.getVisibility() == View.VISIBLE;
    }

    public void showRestartView() {
        startView.setVisibility(View.GONE);
        restartView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
    }

    public void showErrorView() {
        startView.setVisibility(View.GONE);
        restartView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
    }

    public void hideAllView() {
        startView.setVisibility(View.GONE);
        restartView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
    }

    OnPlayButtonClickListener listener;

    public void setOnPlayButtonClickListener(OnPlayButtonClickListener listener) {
        this.listener = listener;
    }

    public interface OnPlayButtonClickListener {
        void onStartClick();

        void onRestartClick();

        void onErrorClick();
    }


}
