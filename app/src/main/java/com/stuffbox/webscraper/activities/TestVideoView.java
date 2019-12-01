
package com.stuffbox.webscraper.activities;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.stuffbox.webscraper.R;
import com.stuffbox.webscraper.constants.Constants;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class TestVideoView extends AppCompatActivity  {
    String finallink;
    ImageButton nextepisode,prevepisode;
    String l;
    long time;
    String nextlink;
    String link;
    LinearLayout controls;
    int s;
    String selectepisodelink;
    SQLiteDatabase recent;
    ProgressBar progressBar;
    int qualitysetter=0;

    private PlayerView playerView;
    private static final String ACTION_MEDIA_CONTROL = "media_control";
    private static final String EXTRA_CONTROL_TYPE = "control_type";
    private ArrayList<String> storinggoogleurls=new ArrayList<>();
    int episodeno;
    org.jsoup.nodes.Document reacheddownloadlink;
    int current;
    String animenames;
    SimpleExoPlayer simpleExoPlayer;
    org.jsoup.nodes.Document mBlogDocument ;
    TextView title;
    String nextvideolink=null,previousvideolink=null;
    int epno;
    String backstack="notlost";
    String animename,imagelink;
    private BroadcastReceiver receiver;
    private  ArrayList<String> storingquality=new ArrayList<>();
    com.google.android.exoplayer2.upstream.DataSource.Factory datasourcefactory;
    ImageButton qualitychanger;
    String id;
    private  PictureInPictureParams.Builder mPictureInPictureParamsBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.videoviewer);
        recent=openOrCreateDatabase("recent",MODE_PRIVATE,null);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        if(android.os.Build.VERSION.SDK_INT>=26)
            mPictureInPictureParamsBuilder=new PictureInPictureParams.Builder();
        title=findViewById(R.id.titleofanime);
        title.setVisibility(View.GONE);
        if(getIntent().getStringExtra("selectepisodelink")!=null) {
            selectepisodelink = getIntent().getStringExtra("selectepisodelink");
            animenames=getIntent().getStringExtra("animenames");
        }
        controls=findViewById(R.id.wholecontroller);
        progressBar=findViewById(R.id.buffer);
        qualitychanger=findViewById(R.id.qualitychanger);
        nextepisode=findViewById(R.id.exo_nextvideo);
        prevepisode=findViewById(R.id.exo_prevvideo);
        decorView = getWindow().getDecorView();
        uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        new FindVideoLink(getApplicationContext()).execute();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        playerView = findViewById(R.id.exoplayer);
        playerView.setPlayer(simpleExoPlayer);

        datasourcefactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "tryingexoplayer"));

    }
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("videolink",finallink);

        super.onSaveInstanceState(outState);
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();

        }

    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
    @SuppressLint("StaticFieldLeak")
    private class FindVideoLink extends AsyncTask<Void, Void, Void> {
        // Bad variable names start here
        String x;
        Context context;
        FindVideoLink(Context context)
        {
            this.context=context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }
        @Override
        protected Void doInBackground(Void... params) {
            qualitysetter=0;
            finallink=null;
            storinggoogleurls.clear();
            storingquality.clear();
            try {
                if(nextlink==null)
                    link = getIntent().getStringExtra("link");
                else
                    link=nextlink;
                animename=getIntent().getStringExtra("animename");
                imagelink=getIntent().getStringExtra("imagelink");
                int gettingindex=link.lastIndexOf("-");
                epno=Integer.parseInt(link.substring(gettingindex+1));
                s=Integer.parseInt(getIntent().getStringExtra("noofepisodes")); //no of episodes
                if(link.equals(Constants.url+"ansatsu-kyoushitsu-tv--episode-1"))  //edge case
                    mBlogDocument=Jsoup.connect(Constants.url+"ansatsu-kyoushitsu-episode-1").get();
                else
                    mBlogDocument = Jsoup.connect(link).get();
                previousvideolink=mBlogDocument.select("div[class=anime_video_body_episodes_l]").select("a").attr("abs:href");
                nextvideolink=mBlogDocument.select("div[class=anime_video_body_episodes_r]").select("a").attr("abs:href");
                Elements mElementDataSize = mBlogDocument.select("iframe");
                x = mElementDataSize.attr("src");
                if(mElementDataSize.size()==0)
                {
                    Elements elements=mBlogDocument.select("li[class=mp4]").select("a");
                    l=elements.attr("data-video");;
                }
                else{
                    l = "https:" + x;
                    String vidstreamlink=mElementDataSize.attr("src");
                    int abc=vidstreamlink.indexOf("id=");
                    int k=abc;
                    while(vidstreamlink.charAt(k)!='=')
                        k++;
                    k++;
                    while(vidstreamlink.charAt(k)!='&')
                        k++;
                    id=vidstreamlink.substring(abc,k);

                    String downloadlink="https://vidstream.co/download?"+id;

                    reacheddownloadlink=Jsoup.connect(downloadlink).timeout(0).get();
                    Elements elements1 = reacheddownloadlink.select("div[class=dowload]").select("a");
                    while (elements1.eq(qualitysetter).attr("href").contains("googlevideo")
                            ||elements1.eq(qualitysetter).attr("href").contains("googleuser")
                        //   || elements1.eq(qualitysetter).attr("href").contains("cdnfile.info")
                    )
                    { storinggoogleurls.add(elements1.eq(qualitysetter).attr("href"));
                        String x=String.valueOf(elements1.eq(qualitysetter).text());
                        String c= x.substring(10, 15);
                        storingquality.add(c);
                        qualitysetter++;}
                    qualitysetter--;

                    if (qualitysetter == -1) {
                        qualitysetter = 0;
                        int ind=0;
                        org.jsoup.nodes.Document rapidvideo;
                        while((!elements1.eq(ind).text().equals("Download Rapidvideo") )&&ind<elements1.size())
                            ind++;
                        if(!(ind==elements1.size()))
                            rapidvideo = Jsoup.connect(elements1.eq(ind).attr("href")).get();
                        else
                            rapidvideo=Jsoup.connect(elements1.eq(qualitysetter+1).attr("href")).get();
                           /*  Old rapidvideo scraping
                             Elements e = rapidvideo.select("div[class=video]");
                           if(e.size()>0)
                            {
                            Elements f = e.eq(e.size() - 1).select("span").select("a");
                            if(f.size()>0) {
                                qualitysetter=-1;
                                for (int m = 0; m < f.size(); m++) {
                                    if (!(f.eq(m).attr("href").contains("premium"))) {
                                        storinggoogleurls.add(f.eq(m).attr("href"));
                                        String p = f.eq(m).select("span").html();
                                        int index = p.indexOf(" ");
                                        storingquality.add(p.substring(index + 1, p.length()));
                                        qualitysetter++;
                                    }
                                }
                                finallink=f.eq(qualitysetter).attr("href");
                                current=qualitysetter;
                            }
                        } */
                        Elements links = rapidvideo.select("a[id=button-download]");
                        if(links.size()>0)
                        {
                            qualitysetter=-1;
                            for(int m = 0;m<links.size();m++)
                            {
                                if (!(links.eq(m).attr("href").contains("premium"))) {
                                    storinggoogleurls.add(links.eq(m).attr("href"));
                                    String p = links.eq(m).text();
                                    int index = p.indexOf(" ");
                                    storingquality.add(p.substring(index + 1));
                                    qualitysetter++;
                                }
                            }
                            finallink=links.eq(qualitysetter).attr("href");
                            current=qualitysetter;
                        }
                    }

                    else
                    {

                        finallink = elements1.eq(qualitysetter).attr("href");
                        current=qualitysetter;
                    } }

            } catch (Exception e1) {
                e1.printStackTrace();
            }


            if(finallink==null) {
                String rapid = mBlogDocument.select("li[class=rapidvideo]").select("a").attr("data-video");
                if (rapid != null&&rapid.contains("rapidvideo")) {
                    try

                    {
                        org.jsoup.nodes.Document scrapingrapidvideo = Jsoup.connect(rapid).get();
                        qualitysetter=-1;
                        String rapidvideolink = scrapingrapidvideo.select("video[id=videojs]").select("source").attr("src");
                        storinggoogleurls.add(rapidvideolink);
                        storingquality.add(scrapingrapidvideo.select("video[id=videojs]").select("source").attr("title"));
                        qualitysetter++;
                        current=qualitysetter;
                        finallink = rapidvideolink;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            disableSSLCertificateVerify();

            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {

                Connection.Response response = null;
                try {
                    response = Jsoup.connect(finallink).followRedirects(false).execute();
                    if (response != null) {
                        finallink=response.header("location");

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }



            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    title.setText(animename+" Episode "+epno);
                    title.setVisibility(View.VISIBLE);


                    if(finallink==null)
                    {
                        Intent intent = new Intent(context, webvideo.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("videostreamlink", l);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        Log.d("playingvideourl",finallink);

                        MediaSource vediosource = new ExtractorMediaSource.Factory(datasourcefactory).createMediaSource(Uri.parse(finallink));
                        simpleExoPlayer.prepare(vediosource);
                        playerView.getPlayer().setPlayWhenReady(true);
                        final String[] a= storingquality.toArray(new String[0]);
                        qualitychanger.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(TestVideoView.this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                                builder.setTitle("Quality")
                                        .setItems(a, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                                if(current!=which)
                                                {
                                                    long t=playerView.getPlayer().getCurrentPosition();

                                                    current=which;
                                                    MediaSource vediosource=    new ExtractorMediaSource.Factory(datasourcefactory).createMediaSource(Uri.parse(storinggoogleurls.get(which)));
                                                    simpleExoPlayer.prepare(vediosource);
                                                    playerView.getPlayer().setPlayWhenReady(true);
                                                    playerView.getPlayer().seekTo(t);
                                                }}
                                        });
                                builder.show();
                            }
                        });

                        simpleExoPlayer.addListener(new Player.EventListener() {
                            @Override
                            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

                            }

                            @Override
                            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

                            }

                            @Override
                            public void onLoadingChanged(boolean isLoading) {

                            }

                            @Override
                            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                                if(playbackState==ExoPlayer.STATE_ENDED)
                                {
                                    int index=link.lastIndexOf("-");
                                    episodeno=Integer.parseInt(link.substring(index+1,link.length()));
                                    episodeno=episodeno+1;
                                    if(nextvideolink==null||nextvideolink.equals(""))
                                        Toast.makeText(getApplicationContext(),"Last Episode",Toast.LENGTH_SHORT).show();
                                    else
                                    {

                                        nextlink=nextvideolink;
                                        String z="'"+ animename+"','Episode "+episodeno+"','"+nextlink+"','"+imagelink+"'";
                                        recent.execSQL("delete from anime where EPISODELINK='"+nextlink+"'");
                                        simpleExoPlayer.stop();
                                        recent.execSQL("INSERT INTO anime VALUES("+z+");");
                                        new FindVideoLink(getApplicationContext()).execute();
                                    }
                                }
                                if (playbackState == ExoPlayer.STATE_BUFFERING){
                                    progressBar.setVisibility(View.VISIBLE);
                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);
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

                                playerView.getPlayer().release();
                                Intent intent = new Intent(context, webvideo.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("videostreamlink", l);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onPositionDiscontinuity(int reason) {

                            }

                            @Override
                            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

                            }

                            @Override
                            public void onSeekProcessed() {

                            }
                        });
                    }
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressBar.setVisibility(View.GONE);
            nextepisode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int index=link.lastIndexOf("-");
                    episodeno=Integer.parseInt(link.substring(index+1,link.length()));
                    episodeno=episodeno+1;
                    Log.i("nextvideolink",nextvideolink);
                    if(nextvideolink==null||nextvideolink.equals(""))
                        Toast.makeText(getApplicationContext(),"Last Episode",Toast.LENGTH_SHORT).show();
                    else
                    {

                        nextlink=nextvideolink;
                        String z="'"+ animename+"','Episode "+episodeno+"','"+nextlink+"','"+imagelink+"'";
                        recent.execSQL("delete from anime where EPISODELINK='"+nextlink+"'");

                        recent.execSQL("INSERT INTO anime VALUES("+z+");");
                        new FindVideoLink(getApplicationContext()).execute();
                    }
                }
            });

            prevepisode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int index=link.lastIndexOf("-");
                    int episodeno=Integer.parseInt(link.substring(index+1,link.length()));

                    episodeno=episodeno-1;
                    if(previousvideolink==null||previousvideolink.equals(""))

                        Toast.makeText(getApplicationContext(),"First Episode",Toast.LENGTH_SHORT).show();
                    else
                    {
                        nextlink=previousvideolink;
                        String z="'"+ animename+"','Episode "+episodeno+"','"+nextlink+"','"+imagelink+"'";
                        recent.execSQL("delete from anime where EPISODELINK='"+nextlink+"'");
                        recent.execSQL("INSERT INTO anime VALUES("+z+");");
                        new FindVideoLink(getApplicationContext()).execute();
                    }
                }
            });
        }
    }
    private static void disableSSLCertificateVerify() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                        return myTrustedAnchors;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");

            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public  void onPause()
    {
        super.onPause();

        time=playerView.getPlayer().getCurrentPosition();
    }
    @Override
    public void onStop()
    {
        super.onStop();
        time= playerView.getPlayer().getCurrentPosition();
        playerView.getPlayer().setPlayWhenReady(false);

    }

    @Override
    public  void  onResume() {
        super.onResume();

        if (finallink != null) {

            playerView.getPlayer().setPlayWhenReady(true);

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {


        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            playerView.getPlayer().release();
            if(backstack.equals("lost")) {

                ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
                if(am != null) {
                    List<ActivityManager.AppTask> tasks = am.getAppTasks();
                    if (tasks != null && tasks.size() > 1) {

                        tasks.get(0).setExcludeFromRecents(true);
                        tasks.get(1).moveToFront();
                    }
                }


            }
            super.onBackPressed();
        }
        return false;
    }
    @Override
    public void onPictureInPictureModeChanged (boolean isInPictureInPictureMode, Configuration newConfig) {
        if (isInPictureInPictureMode) {
            controls.setVisibility(View.GONE);
            title.setVisibility(View.GONE);
            receiver=new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    if (intent == null )
                        return;
                    Log.i("sojaasd","marja");

                    if(playerView.getPlayer().getPlayWhenReady()) {
                        playerView.getPlayer().setPlayWhenReady(false);
                        updatePictureInPictureActions(R.drawable.pip_play, "play", 0, 0,intent);
                    }
                    else
                    {
                        playerView.getPlayer().setPlayWhenReady(true);
                        updatePictureInPictureActions(R.drawable.pip_pause, "pause", 0, 0,intent);
                    }


                }
            };
            registerReceiver(receiver, new IntentFilter(ACTION_MEDIA_CONTROL));

        } else {

            title.setVisibility(View.VISIBLE);
            controls.setVisibility(View.VISIBLE);
            unregisterReceiver(receiver);
            receiver=null;
        }
    }
    @Override
    public void onUserLeaveHint()
    {
        if(android.os.Build.VERSION.SDK_INT>=26 )
            try
            {
                backstack="lost";
                int  x =  playerView.getPlayer().getPlayWhenReady()? R.drawable.pip_pause: R.drawable.pip_play;
                updatePictureInPictureActions(x,"soja",0,0,null);
                enterPictureInPictureMode(mPictureInPictureParamsBuilder.build());

            }catch (Exception e)
            {
                e.printStackTrace();
            }

    }
    void updatePictureInPictureActions(@DrawableRes int iconId,String title,int controlType,int requestCode,Intent newintent)
    {
        final ArrayList<RemoteAction> actions = new ArrayList<>();
        if(newintent==null)
            newintent=new Intent(ACTION_MEDIA_CONTROL).putExtra(EXTRA_CONTROL_TYPE,controlType);
        final PendingIntent intent =
                PendingIntent.getBroadcast(
                        TestVideoView.this,
                        requestCode,
                        newintent,
                        0);
        final Icon icon = Icon.createWithResource(TestVideoView.this, iconId);
        RemoteAction action=new RemoteAction(icon,title,title,intent);
        actions.add(action);
        mPictureInPictureParamsBuilder.setActions(actions);
        setPictureInPictureParams(mPictureInPictureParamsBuilder.build());
    }
}