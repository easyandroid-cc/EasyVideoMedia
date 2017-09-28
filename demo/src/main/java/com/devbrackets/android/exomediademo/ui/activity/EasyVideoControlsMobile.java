package com.devbrackets.android.exomediademo.ui.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.devbrackets.android.exomedia.ui.widget.VideoControlsMobile;
import com.devbrackets.android.exomedia.util.TimeFormatUtil;
import com.devbrackets.android.exomediademo.R;
import com.devbrackets.android.exomediademo.ui.activity.view.VideoPlayController;
import com.devbrackets.android.exomediademo.ui.activity.view.VideoSlideController;

public class EasyVideoControlsMobile extends VideoControlsMobile {

    VideoSlideController videoSlideController;

    VideoPlayController videoPlayController;//播放 错误 重播

    public EasyVideoControlsMobile(Context context) {
        super(context);
    }

    public EasyVideoControlsMobile(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EasyVideoControlsMobile(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EasyVideoControlsMobile(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void setup(final Context context) {
        super.setup(context);
        videoSlideController = (VideoSlideController) findViewById(R.id.videoController);
        videoPlayController = (VideoPlayController) findViewById(R.id.videoPlayController);
        videoSlideController.setVideoControls(this);
        videoSlideController.setOnSeekBarChangeListener(new VideoControllerChangedListener());
        titleTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = (Activity) context;
                activity.onBackPressed();
            }
        });
        videoPlayController.setOnPlayButtonClickListener(new VideoPlayController.OnPlayButtonClickListener() {
            @Override
            public void onStartClick() {
                onPlayPauseClick();
            }

            @Override
            public void onRestartClick() {
                if (videoView != null) {
                    videoView.restart();
                }
                hideAllView();
            }

            @Override
            public void onErrorClick() {
                if (videoView != null) {
                    videoView.restart();
                }
                hideAllView();
            }
        });
    }

    public void showStartView() {
        videoPlayController.showStartView();
    }

    public void showErrorView() {
        loadingProgressBar.setVisibility(View.GONE);
        seekBar.setEnabled(false);
        seekBar.setVisibility(View.INVISIBLE);
        playPauseButton.setEnabled(false);
        videoPlayController.showErrorView();
    }

    public void showRestartView() {
        seekBar.setEnabled(false);
        seekBar.setVisibility(View.INVISIBLE);
        playPauseButton.setEnabled(false);
        videoPlayController.showRestartView();
    }

    public void hideAllView() {
        seekBar.setEnabled(true);
        seekBar.setVisibility(View.VISIBLE);
        playPauseButton.setEnabled(true);
        videoPlayController.hideAllView();
    }

    public long getCurrentPosition() {
        return videoView.getCurrentPosition();
    }

    public long getDuration() {
        return videoView.getDuration();
    }

    @Override
    protected int getLayoutResource() {
        return com.devbrackets.android.exomediademo.R.layout.exo_playback_control_view;
    }

    public class VideoControllerChangedListener {
        private long seekToTime;

        public void onProgressChanged(int progress) {
            seekToTime = progress;
            if (currentTimeTextView != null) {
                currentTimeTextView.setText(TimeFormatUtil.formatMs(seekToTime));
            }
        }

        public void onStartTrackingTouch() {
            userInteracting = true;
            if (seekListener == null || !seekListener.onSeekStarted()) {
                internalListener.onSeekStarted();
            }
        }

        public void onStopTrackingTouch() {
            userInteracting = false;
            if (seekListener == null || !seekListener.onSeekEnded(seekToTime)) {
                internalListener.onSeekEnded(seekToTime);
            }
        }
    }
}