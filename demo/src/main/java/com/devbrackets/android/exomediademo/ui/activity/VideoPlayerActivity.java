package com.devbrackets.android.exomediademo.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.devbrackets.android.exomedia.listener.OnCompletionListener;
import com.devbrackets.android.exomedia.listener.OnErrorListener;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.devbrackets.android.exomediademo.App;
import com.devbrackets.android.exomediademo.R;
import com.devbrackets.android.exomediademo.data.MediaItem;
import com.devbrackets.android.exomediademo.data.Samples;
import com.devbrackets.android.exomediademo.manager.PlaylistManager;
import com.devbrackets.android.exomediademo.playlist.VideoApi;
import com.devbrackets.android.playlistcore.listener.PlaylistListener;
import com.devbrackets.android.playlistcore.manager.BasePlaylistManager;
import com.devbrackets.android.playlistcore.service.PlaylistServiceCore;

import java.util.LinkedList;
import java.util.List;


public class VideoPlayerActivity extends Activity implements PlaylistListener<MediaItem> {
    public static final String EXTRA_INDEX = "EXTRA_INDEX";
    public static final int PLAYLIST_ID = 6; //Arbitrary, for the example (different from audio)

    protected VideoView videoView;
    protected PlaylistManager playlistManager;

    protected int selectedIndex;
    protected boolean pausedInOnStop = false;
    protected View content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player_activity);
        retrieveExtras();
        init();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (videoView.isPlaying()) {
            pausedInOnStop = true;
            videoView.pause();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (pausedInOnStop) {
            videoView.start();
            pausedInOnStop = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        playlistManager.unRegisterPlaylistListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        playlistManager = App.getPlaylistManager();
        playlistManager.registerPlaylistListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playlistManager.invokeStop();
        videoView.release();
    }

    @Override
    public boolean onPlaylistItemChanged(MediaItem currentItem, boolean hasNext, boolean hasPrevious) {
        return false;
    }

    @Override
    public boolean onPlaybackStateChanged(@NonNull PlaylistServiceCore.PlaybackState playbackState) {
        if (playbackState == PlaylistServiceCore.PlaybackState.STOPPED) {
//            playlistManager.play(0, true);
//            easyVideoControlsMobile.showRestartView();
//            return true;
        } else if (playbackState == PlaylistServiceCore.PlaybackState.ERROR) {
//            showErrorMessage();
//            easyVideoControlsMobile.   onPreviousClick();
//            easyVideoControlsMobile.showErrorView();
        } else if (playbackState == PlaylistServiceCore.PlaybackState.PLAYING) {
            easyVideoControlsMobile.hideAllView();
        } else if (playbackState == PlaylistServiceCore.PlaybackState.PAUSED) {
//            if (isCompletion) {
//                easyVideoControlsMobile.hideAllView();
//                isCompletion = false;
//            }
//            easyVideoControlsMobile.showStartView();
        }

        System.out.println("cgp playbackState= " + playbackState);
        return false;
    }


    /**
     * Retrieves the extra associated with the selected playlist index
     * so that we can start playing the correct item.
     */
    protected void retrieveExtras() {
        Bundle extras = getIntent().getExtras();
        selectedIndex = extras.getInt(EXTRA_INDEX, 0);
    }

    EasyVideoControlsMobile easyVideoControlsMobile;

    protected void init() {
        setupPlaylistManager();
        videoView = (VideoView) findViewById(R.id.video_play_activity_video_view);
        easyVideoControlsMobile = new EasyVideoControlsMobile(videoView.getContext());
        content = findViewById(R.id.content);
        videoView.setControls(easyVideoControlsMobile);
        playlistManager.setVideoPlayer(new VideoApi(videoView));
        playlistManager.play(0, true);

//        videoView.setMeasureBasedOnAspectRatioEnabled();
//        videoView.set
        videoView.isPlaying();
        videoView.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion() {
                easyVideoControlsMobile.showRestartView();
            }
        });

        videoView.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(Exception e) {
                easyVideoControlsMobile.showErrorView();
//                easyVideoControlsMobile.finishLoading();
                return true;
            }
        });
        videoView.setReleaseOnDetachFromWindow(true);
        videoView.setKeepScreenOn(true);
        videoView.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared() {
                if (!videoView.isPlaying()) {
                    easyVideoControlsMobile.showStartView();
                }
            }
        });
        videoView.setPreviewImage(R.mipmap.ic_launcher);
    }

    /**
     * Retrieves the playlist instance and performs any generation
     * of content if it hasn't already been performed.
     */
    private void setupPlaylistManager() {
        playlistManager = App.getPlaylistManager();

        List<MediaItem> mediaItems = new LinkedList<>();
        for (Samples.Sample sample : Samples.getVideoSamples()) {
            MediaItem mediaItem = new MediaItem(sample, false);
            mediaItems.add(mediaItem);
        }

        playlistManager.setAllowedMediaType(BasePlaylistManager.VIDEO);
//        playlistManager.setAllowedMediaType(BasePlaylistManager.AUDIO | BasePlaylistManager.VIDEO);
        playlistManager.setParameters(mediaItems, selectedIndex);
        playlistManager.setId(PLAYLIST_ID);
    }


}
