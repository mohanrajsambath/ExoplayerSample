package com.example.innobot_linux_2.exoplayersample;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.CaptioningManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.DefaultHlsPlaylistParserFactory;
import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.TrackSelectionView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

public class MainActivity extends AppCompatActivity implements SpeedListener, TrackDetectListenter {

    private ZoomableExoPlayerView videoview;
    private Handler mainHandler;
    private Timeline.Window window;

    private DataSource.Factory mediaDataSourceFactory;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;

    private boolean shouldAutoPlay;
    private int playerWindow;
    private long playerPosition;
    private BandwidthMeter bandwidthMeter;
    private DefaultExtractorsFactory extractorsFactory;
    private ImageButton exo_fullscreen_button, exo_playback, exo_video, exo_audio, exo_caption, exo_playerpause;
    private boolean fullscreen=true;
    private LinearLayout lL_parentLayout;
    private TextView txtVw_dummy;
    private DefaultTimeBar exo_progress;
    private boolean video=false, audio=false, caption=false;
    String speed="Normal";
    private TrackSelectionHelper trackSelectionHelper;
    private TrackSelectionView trackSelectionView;
    private static final TrackSelection.Factory FIXED_FACTORY=new FixedTrackSelection.Factory();
    TrackSelection.Factory TrackSelectionFactory=new FixedTrackSelection.Factory();

    DataSource.Factory FIXFactory=new DataSource.Factory() {
        @Override
        public DataSource createDataSource() {
            return null;
        }
    };
    private DefaultTrackSelector.SelectionOverride override;
    private boolean isCCdisabled;
    boolean isClosedCaption=true;
    private TrackGroupArray trackGroups;
    MappingTrackSelector.MappedTrackInfo trackInfo;
    MainActivity getActivityContext;
    private CaptionStyleCompat captionStyleCompat;
    private boolean isSubTittlePresent;
    private boolean isCClastStatus;
    CaptioningManager captionstyle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        shouldAutoPlay=true;
        getActivityContext=this;
        bandwidthMeter=new DefaultBandwidthMeter();
        mediaDataSourceFactory=new DefaultDataSourceFactory(this, Util.getUserAgent(this, "mediaPlayerSample"), (TransferListener) bandwidthMeter);
        mainHandler=new Handler();
        window=new Timeline.Window();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializePlayer();

        FloatingActionButton fab=(FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void initializePlayer() {
        lL_parentLayout=(LinearLayout) findViewById(R.id.lL_parentLayout);
        videoview=(ZoomableExoPlayerView) findViewById(R.id.videoview);
        videoview.requestFocus();
        txtVw_dummy=(TextView) findViewById(R.id.txtVw_dummy);
        // TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelection.Factory videoTrackSelectionFactory=new AdaptiveTrackSelection.Factory();
        trackSelector=new DefaultTrackSelector(videoTrackSelectionFactory);
        trackSelectionHelper=new TrackSelectionHelper(trackSelector, videoTrackSelectionFactory);


//        trackSelector.setRendererDisabled(2, isCCdisabled);
//        isCCdisabled = false;
        //   override = new DefaultTrackSelector.SelectionOverride(videoTrackSelectionFactory,);
        player=ExoPlayerFactory.newSimpleInstance(this, new DefaultRenderersFactory(this), trackSelector, new DefaultLoadControl());
        PlaybackParameters playbackParameters=new PlaybackParameters(1.0f, 1.0f);
        player.setPlaybackParameters(playbackParameters);
        videoview.getSubtitleView().setFixedTextSize(1, 15.0533f);
        videoview.setPlayer(player);
        player.setPlayWhenReady(true);
//        DefaultHttpDataSource sample = new DefaultHttpDataSource(Util.getUserAgent(this, "mediaPlayerSample"), null);
//        sample.setRequestProperty("Referer", "https://staging.cyranosystems.com/");
        extractorsFactory=new DefaultExtractorsFactory();


//        MediaSource mediaSource = new ExtractorMediaSource(Uri.parse("https://cyrano-data-dev.s3.amazonaws.com/content/video/3598f75e-7f73-4647-88e8-f0849ae43957.mp4?AWSAccessKeyId=AKIAJ5DUXSEFFZYJWY5Q&Expires=1493118156&Signature=9qlbFuhRkPbmHRONgKhH1MyPUSo%3D"),
//                mediaDataSourceFactory, extractorsFactory, null, null);
//        LoopingMediaSource loopingSource = new LoopingMediaSource(mediaSource);

        //for mp4
        MediaSource mediaSource=mediaData();

        // for DASH
        MediaSource dataSourceFactory=dashMediaSource();

        //for subtitle
        MediaSource subtitleSource=subtitle();

        MediaSource hlsSource=hlsMediaSource();

        MediaSource hlsVtt=hlsMediaSourcevtt();

        player.prepare(mediaSource);

        player.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                MappingTrackSelector.MappedTrackInfo mappedTrackInfo=trackSelector.getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        Toast.makeText(MainActivity.this, "Unsupported", Toast.LENGTH_SHORT).show();
                    } else {
                        System.out.println(" supported video " + mappedTrackInfo.getTrackGroups(0));
                    }
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_AUDIO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        Toast.makeText(MainActivity.this, "Unsupported", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {


            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    player.seekTo(0);
                    player.setPlayWhenReady(false);
                } else if (playbackState == Player.STATE_READY) {

                  // videoview.hideController();
                } else if (playbackState == Player.STATE_IDLE) {

                }else if (playbackState == Player.STATE_BUFFERING){
                    videoview.showController();
                }

            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                videoview.setPlayer(player);
                player.setPlayWhenReady(true);

            }

            @Override
            public void onSeekProcessed() {

            }
        });

        /* player.addListener(new ExoPlayer.EventListener() {
         *//*    @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }*//*

            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
                    if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        Toast.makeText(MainActivity.this, "Unsupported", Toast.LENGTH_SHORT).show();
                    } else {
                        System.out.println(" supported video " + mappedTrackInfo.getTrackGroups(0));
                    }
                    if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        Toast.makeText(MainActivity.this, "Unsupported", Toast.LENGTH_SHORT).show();
                    }
                }



            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//                System.out.println("Play ended " + playbackState);
                if (playbackState == ExoPlayer.STATE_ENDED) {
                    player.seekTo(0);
                    player.setPlayWhenReady(false);
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                videoview.setPlayer(player);
                player.setPlayWhenReady(true);
            }

            @Override
            public void onSeekProcessed() {

            }



        });*/

        player.addAnalyticsListener(new AnalyticsListener() {
            @Override
            public void onPlayerStateChanged(EventTime eventTime, boolean playWhenReady, int playbackState) {

            }
        });

        exo_fullscreen_button=(ImageButton) findViewById(R.id.exo_fullscreen_button);
        exo_fullscreen_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFullscreen(MainActivity.this, fullscreen);
                fullscreen=!fullscreen;
            }
        });
        exo_playback=(ImageButton) findViewById(R.id.exo_playback);
        exo_playback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialogFragment bottomSheetDialogFragment=new SpeedDialogFragment().newInstance(speed);
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
//                showPopUp();
            }
        });
        exo_progress=(DefaultTimeBar) findViewById(R.id.exo_progress);
        exo_progress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                PlaybackParameters playbackParameters=new PlaybackParameters(1.0f, 1.0f);
                player.setPlaybackParameters(playbackParameters);
                videoview.setPlayer(player);
                player.setPlayWhenReady(true);
                return false;
            }
        });
        exo_video=(ImageButton) findViewById(R.id.exo_video);
        exo_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MappingTrackSelector.MappedTrackInfo mappedTrackInfo=trackSelector.getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
//                    trackSelectionHelper.showSelectionDialog(MainActivity.this, "Video",
//                            trackSelector.getCurrentMappedTrackInfo(), 0);
                }

            }
        });
        exo_audio=(ImageButton) findViewById(R.id.exo_audio);
        exo_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MappingTrackSelector.MappedTrackInfo mappedTrackInfo=trackSelector.getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
//                    trackSelectionHelper.showSelectionDialog(MainActivity.this, "Audio",
//                            trackSelector.getCurrentMappedTrackInfo(), 1);
                }
            }
        });

        exo_caption=(ImageButton) findViewById(R.id.exo_caption);
        exo_caption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MappingTrackSelector.MappedTrackInfo mappedTrackInfo=trackSelector.getCurrentMappedTrackInfo();
//                if (!isCCdisabled) {
//                    trackSelector.setRendererDisabled(2, isClosedCaption);
//                    trackSelector.clearSelectionOverrides();
//                    isCCdisabled = true;
//                    trackSelector.clearSelectionOverrides();
//                    exo_caption.setImageResource(R.drawable.captionselected);
//                } else {
//                    trackSelector.setRendererDisabled(2, false);
//                    isCCdisabled = false;
//                    trackSelector.clearSelectionOverrides(2);
//                    exo_caption.setImageResource(R.drawable.caption);
//                }


                if (mappedTrackInfo != null) {
                    trackSelectionHelper.showSelectionDialog(MainActivity.this, "Track", trackSelector.getCurrentMappedTrackInfo(), 2);

                }

            }
        });

        exo_playerpause=(ImageButton) findViewById(R.id.exo_pause);
        exo_playerpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseplayer();
            }
        });

    }

    private void updateSubtitle(MappingTrackSelector.MappedTrackInfo mappedTrackInfo, int i) {
        trackSelectionHelper.updateSubtittle(mappedTrackInfo, i, this);
    }

    public void disable(int item) {
        switch (item) {
            case 0:
                video=!video;
                if (video) {
                    exo_video.setImageResource(R.drawable.video_false);
                } else {
                    exo_video.setImageResource(R.drawable.video_true);
                }
                trackSelector.buildUponParameters().setRendererDisabled(0, video);
                trackSelector.buildUponParameters().clearSelectionOverrides();
                break;
            case 1:
                audio=!audio;
                if (audio) {
                    exo_audio.setImageResource(R.drawable.audio_false);
                } else {
                    exo_audio.setImageResource(R.drawable.audio_true);
                }
                trackSelector.buildUponParameters().setRendererDisabled(1, audio);
                trackSelector.buildUponParameters().clearSelectionOverrides();
                break;
            case 2:
                caption=!caption;
                if (caption) {
                    exo_caption.setImageResource(R.drawable.captionselected);
                } else {
                    exo_caption.setImageResource(R.drawable.caption);
                }
                trackSelector.buildUponParameters().setRendererDisabled(2, caption);
                trackSelector.buildUponParameters().clearSelectionOverrides();
                break;
            default:
                break;
        }
    }

    /*private List<StreamKey> getOfflineStreamKeys(Uri uri) {
        return (List<StreamKey>) Uri.parse("https://cyrano-data-dev.s3-eu-west-1.amazonaws.com/content/video/0c745547-422a-4393-b395-fe25af3f57e7.mp4?X-Amz-Algorithm=AWS4-" +
                "HMAC-SHA256&X-Amz-Date=20171207T100313Z&X-Amz-SignedHeaders=host&X-Amz-Expires=86400&X-Amz-Credential=AKIAJ5DUXSEFFZYJ" +
                "WY5Q%2F20171207%2Feu-west-1%2Fs3%2Faws4_request&X-Amz-Signature=b1777c8ed8494997feae5357e06e0513b17e8e2461ecc593f953e9f1533fe968");
    }*/

    //normal media source
    public MediaSource mediaData() {
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            File videoFileDir = new File(extStorageDirectory, "/Clips/Videos/big_buck_bunny.mp4");


      //  String urimp4="/Clips/Videos/big_buck_bunny.mp4";
        if(videoFileDir.exists()){
            Uri mp4VideoUri=Uri.parse(videoFileDir.getAbsolutePath());
            DefaultDataSourceFactory dataSourceFactory=new DefaultDataSourceFactory(getActivityContext, Util.getUserAgent(this, "ExoPlayer"));
            MediaSource videosource=new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(mp4VideoUri);
            return videosource;
        }else{
            Toast.makeText(getActivityContext,"NO file Found",Toast.LENGTH_SHORT).show();
            return null;
        }


    }

    //dash media source
    public MediaSource dashMediaSource() {
        DefaultHttpDataSourceFactory dataSourceFactory=new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "ExoPlayer"));
        dataSourceFactory.getDefaultRequestProperties().set("Referer", "https://app.cyranosystems.com/cyrano/");
//        Uri uri = Uri.parse("http://demos.webmproject.org/dash/201410/vp9_glass/manifest_vp9_opus.mpd");
//        Uri uri = Uri.parse("https://bitmovin-a.akamaihd.net/content/playhouse-vr/mpds/105560.mpd");
//        Uri uri = Uri.parse("http://www.bok.net/dash/tears_of_steel/cleartext/stream.mpd");
//        Uri uri = Uri.parse("http://dash.edgesuite.net/akamai/test/caption_test/ElephantsDream/elephants_dream_480p_heaac5_1.mpd");
        Uri uri=Uri.parse(" http://media.axprod.net/ExoPlayer/Captions/Manifest.mpd");
//        Uri uri = Uri.parse("http://irtdashreference-i.akamaihd.net/dash/live/901161/bfs/manifestARD.mpd");
//        Uri uri = Uri.parse("http://irtdashreference-i.akamaihd.net/dash/live/901161/bfs/manifestBR.mpd");

        DashMediaSource dashMediaSource=new DashMediaSource(uri, dataSourceFactory,
                new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);


        return new DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri);


    }

    public MediaSource hlsMediaSource() {
        DefaultHttpDataSourceFactory dataSourceFactory=new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "ExoPlayer"));
        dataSourceFactory.getDefaultRequestProperties().set("Referer", "https://app.cyranosystems.com/cyrano/");
        String source="http://sample.vodobox.com/planete_interdite/planete_interdite_alternate.m3u8";
//        String source1 = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8";
//        String source1 = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8";
        /*  MediaSource mediaSource=new HlsMediaSource(Uri.parse(source),mediaDataSourceFactory, mainHandler, null);*/
        return new HlsMediaSource.Factory(dataSourceFactory).setPlaylistParserFactory(new DefaultHlsPlaylistParserFactory()).createMediaSource(Uri.parse(source));
    }

    public MediaSource hlsMediaSourcevtt() {
        // String source = "http://sample.vodobox.com/planete_interdite/planete_interdite_alternate.m3u8";
        //String source2="https://tungsten.aaplimg.com/VOD/bipbop_adv_fmp4_example/master.m3u8 ";
        String source1="https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8";
        // String source1 = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8";
        // String sour = "https://s3-eu-west-1.amazonaws.com/cyrano-temp/test-audio/aa10a5e5-0d88-dac7-400f-f20a7dce6841.m3u8";
        // String source3 = "https://cdn-rc.cyranosystems.com/content/audio/aa10a5e5-0d88-dac7-400f-f20a7dce6841.mp3";
//String source3 ="https://cdn.curiositystream.com/system/Encoding/HW/000/000/685/670/storyhouse_insidethearizona_rev3_preview-3ea65eb4706954bfe635f261c129a4dd41f92dbb_v2.m3u8";
       /* MediaSource mediaSource=new HlsMediaSource(Uri.parse(source2),
                mediaDataSourceFactory, mainHandler, null);*/
        DefaultHttpDataSourceFactory dataSourceFactory=new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "ExoPlayer"));

        MediaSource mediaSource1=new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(source1));

//        following lines are to test vtt but as of now havent got an url with video alone since reference of m3u8 has built in subtitles
        Uri subtitleUri=Uri.parse("http://html5doctor.com/demos/webvtt/subtitles.vtt");
        Format subtitleFormat=Format.createTextSampleFormat(
                null, // An identifier for the track. May be null.
                MimeTypes.TEXT_VTT, // The mime type. Must be set correctly.
                Format.NO_VALUE, // Selection flags for the track.
                null);

        MediaSource subtitleSource=new SingleSampleMediaSource.Factory(dataSourceFactory).createMediaSource(subtitleUri, subtitleFormat, C.TIME_UNSET);
        //   subtitleUri, dataSourceFactory, subtitleFormat, C.TIME_UNSET);

        MergingMediaSource mergedSource=
                new MergingMediaSource(mediaSource1, subtitleSource);
        return mergedSource;
    }

    public MediaSource subtitle() {
        DefaultHttpDataSourceFactory dataSourceFactory=new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "ExoPlayer"));

        Uri subtitleUri=Uri.parse("http://www.storiesinflight.com/js_videosub/video.srt");
//        Uri subtitleUri = Uri.parse("https://thelastreformationmovie.com/cc/English.srt");
//        Uri uri = Uri.parse("http://demos.webmproject.org/dash/201410/vp9_glass/manifest_vp9_opus.mpd");
//        Uri uri = Uri.parse("http://dash.edgesuite.net/akamai/test/caption_test/ElephantsDream/elephants_dream_480p_heaac5_1.mpd");
        Uri uri=Uri.parse("http://media.axprod.net/ExoPlayer/Captions/Manifest.mpd");
        Format subtitleFormat=Format.createTextSampleFormat(
                null, // An identifier for the track. May be null.
                MimeTypes.APPLICATION_SUBRIP, // The mime type. Must be set correctly.
                Format.NO_VALUE, // Selection flags for the track.
                null);
//
        MediaSource subtitleSource=new SingleSampleMediaSource.Factory(dataSourceFactory).createMediaSource(subtitleUri, subtitleFormat, C.TIME_UNSET);
        MediaSource mediaSource=new DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        //       new DefaultDashChunkSource.Factory(dataSourceFactory), null, null);
        MergingMediaSource mergedSource=
                new MergingMediaSource(mediaSource, subtitleSource);
        return mergedSource;
    }


    private void releasePlayer() {
        if (player != null) {
            shouldAutoPlay=player.getPlayWhenReady();
            playerWindow=player.getCurrentWindowIndex();
            playerPosition=C.TIME_UNSET;
            Timeline timeline=player.getCurrentTimeline();
            if (timeline != null && timeline.getWindow(playerWindow, window).isSeekable) {
                playerPosition=player.getCurrentPosition();
            }
            player.release();
            player=null;
            trackSelectionHelper=null;
            trackSelector=null;
        }
    }

    private void pauseplayer() {
        if (player != null) {
            shouldAutoPlay=player.getPlayWhenReady();
            playerWindow=player.getCurrentWindowIndex();
            playerPosition=C.TIME_UNSET;
            Timeline timeline=player.getCurrentTimeline();
            if (timeline != null && timeline.getWindow(playerWindow, window).isSeekable) {
                playerPosition=player.getCurrentPosition();
            }
            player.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id=item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleFullscreen(Activity activity, boolean fullscreen) {
        int uiOptions=activity.getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions=uiOptions;
        boolean isImmersiveModeEnabled=
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i(this.getPackageName(), "Turning immersive mode mode off. ");
        } else {
            Log.i(this.getPackageName(), "Turning immersive mode mode on.");
        }

        newUiOptions^=View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        newUiOptions^=View.SYSTEM_UI_FLAG_FULLSCREEN;
        newUiOptions^=View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        activity.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);

        try {
            // hide actionbar
            if (activity instanceof AppCompatActivity) {
                if (fullscreen) {
                    // set landscape
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    getSupportActionBar().hide();
                    LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (ViewGroup.LayoutParams.MATCH_PARENT));
                    videoview.setLayoutParams(lp);

                } else {
                    // set Portrait
                    int orientation=this.getResources().getConfiguration().orientation;
                    if (orientation == 1) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                        getSupportActionBar().show();
                        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (ViewGroup.LayoutParams.MATCH_PARENT), 0.5f);
                        videoview.setLayoutParams(lp);
                        txtVw_dummy.setLayoutParams(lp);
                    } else {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                        getSupportActionBar().show();
                        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (ViewGroup.LayoutParams.MATCH_PARENT));
                        videoview.setLayoutParams(lp);
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override //reconfigure display properties on screen rotation
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (ViewGroup.LayoutParams.MATCH_PARENT), 0.5f);
            videoview.setLayoutParams(lp);
            txtVw_dummy.setLayoutParams(lp);
            System.out.println("ORIENTATION_PORTRAIT ");
            // handle change here
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // or here
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (ViewGroup.LayoutParams.MATCH_PARENT));
            videoview.setLayoutParams(lp);
            System.out.println("ORIENTATION_LANDSCAPE ");
        }
    }

    @Override
    public void speedSelected(String speed) {
        this.speed=speed;
        if (speed.equalsIgnoreCase("Normal")) {
            PlaybackParameters playbackParameters=new PlaybackParameters(1f, 1f);
            player.setPlaybackParameters(playbackParameters);
        } else {
            String speedPlay=speed.replace("x", "");
            Float speedInFloat=Float.parseFloat(speedPlay);
            PlaybackParameters playbackParameters=new PlaybackParameters(speedInFloat, speedInFloat);
            player.setPlaybackParameters(playbackParameters);
        }
    }


    @Override
    public void subTittle(Boolean isSubtittle, Boolean isIconVisiability) {
        isSubTittlePresent=isSubtittle;
        if (exo_caption != null) {
            if (isSubTittlePresent && isCClastStatus) {

                // When CC is present and user enabled the cc icon  it applied through that program
                exo_caption.setVisibility(View.VISIBLE);
                exo_caption.setImageResource(R.drawable.captionselected);
                trackSelector.buildUponParameters().setRendererDisabled(2, false);
                isCCdisabled=false;
                //  isCClastStatus = false;
            } else if (isSubTittlePresent) {
                // When CC present for that clips we have to visible the CC icon and disable functionality of CC by default
                exo_caption.setVisibility(View.VISIBLE);
                exo_caption.setImageResource(R.drawable.caption);
                trackSelector.buildUponParameters().setRendererDisabled(2, true);
                trackSelector.buildUponParameters().clearSelectionOverrides();
                isCCdisabled=true;
            }
        }
    }
}
