package com.stuffbox.webscraper.activities;

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
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.stuffbox.webscraper.R;
import com.stuffbox.webscraper.constants.Constants;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WatchVideo extends AppCompatActivity {
    PlayerView playerView;
    SimpleExoPlayer player;
    LinearLayout controls;
    ImageButton nextEpisodeButton, previousEpisodeButton, qualityChangerButton;
    ProgressBar progressBar;
    TextView title;
    String imageLink;
    String nextVideoLink = null;
    String previousVideoLink = null;
    String m3u8link = "";
    Context context;
    ArrayList<String> qualityUrls = new ArrayList<>();
    ArrayList<String> qualityInfo = new ArrayList<>();
    String vidStreamUrl;
        int currentQuality;
    BroadcastReceiver receiver;

    private static final Pattern urlPattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final String ACTION_MEDIA_CONTROL = "media_control";
    private static final String EXTRA_CONTROL_TYPE = "control_type";
    private String animeName;
    int episodeNumber;
    long time;
    String backStack = "";
    SQLiteDatabase recent;

    private PictureInPictureParams.Builder mPictureInPictureParamsBuilder;
    View.OnClickListener nextEpisodeOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (nextVideoLink == null || nextVideoLink.equals(""))
                Toast.makeText(getApplicationContext(), "Last Episode", Toast.LENGTH_SHORT).show();
            else {
                episodeNumber += 1;
                executeQuery(animeName, episodeNumber, nextVideoLink, imageLink);

                new ScrapeVideoLink(nextVideoLink, context).execute();
            }
        }
    };
    View.OnClickListener previousEpisodeOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (previousVideoLink == null || previousVideoLink.equals(""))
                Toast.makeText(getApplicationContext(), "First Episode", Toast.LENGTH_SHORT).show();
            else {
                episodeNumber -= 1;
                executeQuery(animeName, episodeNumber, previousVideoLink, imageLink);

                new ScrapeVideoLink(previousVideoLink, context).execute();
            }
        }
    };
    View.OnClickListener qualityChangerOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(WatchVideo.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
            builder.setTitle("Quality")
                    .setItems(qualityInfo.toArray(new String[0]), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (currentQuality != which) {
                                long t = player.getCurrentPosition();
                                currentQuality = which;
                                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                                        Util.getUserAgent(context, "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_8; en-US) AppleWebKit/532.5 (KHTML, like Gecko) Chrome/4.0.249.0 Safari/532.5"));

                                HlsMediaSource hlsMediaSource =
                                        new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(qualityUrls.get(currentQuality)));
                                player.prepare(hlsMediaSource);
                                player.setPlayWhenReady(true);
                                player.seekTo(t);
                            }
                        }
                    });
            builder.show();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videoviewer);
        setVideoOptions();
        initUIElements();
        recent=openOrCreateDatabase("recent",MODE_PRIVATE,null);
        context = this;
        player = ExoPlayerFactory.newSimpleInstance(this);
        playerView.setPlayer(player);

        String link= getIntent().getStringExtra("link");
        int lastIndexOfDash = link.lastIndexOf("-");
        episodeNumber = Integer.parseInt(link.substring(lastIndexOfDash+1));
        animeName = getIntent().getStringExtra("animename");

        imageLink=getIntent().getStringExtra("imagelink");

        new ScrapeVideoLink(link,this).execute();
        if(android.os.Build.VERSION.SDK_INT>=26)
            mPictureInPictureParamsBuilder=new PictureInPictureParams.Builder();

    }

    void initUIElements(){
        playerView = findViewById(R.id.exoplayer);
        controls = findViewById(R.id.wholecontroller);
        progressBar=findViewById(R.id.buffer);
        title=findViewById(R.id.titleofanime);
        qualityChangerButton = findViewById(R.id.qualitychanger);
        nextEpisodeButton=findViewById(R.id.exo_nextvideo);
        previousEpisodeButton=findViewById(R.id.exo_prevvideo);
    }

    void setVideoOptions()
    {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    String getM3u8Url(String vidStreamUrl)
    {
        try {
            Document videoStreamPageDocument = Jsoup.connect(vidStreamUrl).get();
            String html =videoStreamPageDocument.outerHtml();
            Matcher matcher = urlPattern.matcher(html);
            String m3u8Link = "";
            while (matcher.find())
            {
                int matchStart = matcher.start(1);
                int matchEnd = matcher.end();
                String link = html.substring(matchStart,matchEnd);
                if(link.contains("m3u8"))
                {
                    m3u8Link = link.substring(0,link.indexOf("'"));
                    break;

                }
            }
            Log.i("foundLink",m3u8Link);
            return  m3u8Link;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
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

    void executeQuery(String animeName, int episodeNumber, String link, String imageLink)
    {
        String deleteQuery = "DELETE from anime where EPISODELINK='\"+nextlink+\"'";
        recent.execSQL(deleteQuery);
        String query="'"+ animeName+"','Episode "+episodeNumber+"','"+link+"','"+imageLink+"'";
        recent.execSQL("INSERT INTO anime VALUES("+query+");");

    }

    class ScrapeVideoLink extends AsyncTask<Void,Void,Void>{
        String gogoAnimeUrl;
        Context context;
        ScrapeVideoLink(String gogoAnimeUrl, Context context)
        {
            this.gogoAnimeUrl= gogoAnimeUrl;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            title.setVisibility(View.GONE);
            m3u8link="";
            qualityInfo.clear();
            qualityUrls.clear();
            progressBar.setVisibility(View.VISIBLE);

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try
            {
                if(m3u8link.equals(""))
                    useFallBack();
                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                        Util.getUserAgent(context, "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_8; en-US) AppleWebKit/532.5 (KHTML, like Gecko) Chrome/4.0.249.0 Safari/532.5"));

                HlsMediaSource hlsMediaSource =
                        new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(qualityUrls.get(currentQuality)));
                player.prepare(hlsMediaSource);
                player.setPlayWhenReady(true);
            }
            catch (Exception e)
            {
                useFallBack();
            }

            player.addListener(new Player.EventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    if(playbackState== ExoPlayer.STATE_ENDED)
                    {
                        if(nextVideoLink==null||nextVideoLink.equals(""))
                            Toast.makeText(getApplicationContext(),"Last Episode",Toast.LENGTH_SHORT).show();
                        else
                        {
                            executeQuery(animeName,episodeNumber,nextVideoLink,imageLink);
                            player.stop();
                            new ScrapeVideoLink(nextVideoLink,context).execute();

                        }
                    }else
                    if (playbackState == ExoPlayer.STATE_BUFFERING){
                        progressBar.setVisibility(View.VISIBLE);
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                   useFallBack();
                }
            });
            progressBar.setVisibility(View.GONE);
            title.setText(animeName+" Episode "+episodeNumber);
            nextEpisodeButton.setOnClickListener(nextEpisodeOnClickListener);
            previousEpisodeButton.setOnClickListener(previousEpisodeOnClickListener);
            qualityChangerButton.setOnClickListener(qualityChangerOnClickListener);
            title.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Document gogoAnimePageDocument = null;
            try {
                if (gogoAnimeUrl.equals("https://www1.gogoanimes.ai/ansatsu-kyoushitsu-tv--episode-1")  )//edge case

                {            Log.i("gogoanimeUrl",Constants.url + "ansatsu-kyoushitsu-tv--episode-1");


                    gogoAnimePageDocument = Jsoup.connect("https://www1.gogoanimes.ai/ansatsu-kyoushitsu-episode-1").get();

                }
                else
                    gogoAnimePageDocument = Jsoup.connect(gogoAnimeUrl).get();                vidStreamUrl = "https:"+gogoAnimePageDocument.getElementsByClass("play-video").get(0).getElementsByTag("iframe").get(0).attr("src");
                previousVideoLink=gogoAnimePageDocument.select("div[class=anime_video_body_episodes_l]").select("a").attr("abs:href");
                nextVideoLink=gogoAnimePageDocument.select("div[class=anime_video_body_episodes_r]").select("a").attr("abs:href");
                m3u8link  = getM3u8Url(vidStreamUrl);
                fillQualityList();

            } catch (Exception e) {
                Log.i("gogoanimeerror",e.toString());
            }
            return null;
        }
    }

    private void fillQualityList() {
        try {
            Document m3u8Page = Jsoup.connect(m3u8link).get();
            String htmlToParse=  m3u8Page.outerHtml();
            Pattern qualityPattern = Pattern.compile("[0-9]{3,4}x[0-9]{3,4}");
            Pattern m3u8LinkPattern = Pattern.compile("(drive\\/\\/hls\\/(\\w)*\\/(\\w)*.m3u8)|(hls\\/(\\w)*\\/(\\w)*.m3u8)");
            Matcher qualityMatcher = qualityPattern.matcher(htmlToParse);
            Matcher m3u8LinkMatcher = m3u8LinkPattern.matcher(htmlToParse);
            int index = m3u8link.indexOf("/hls");
            String baseUrl = m3u8link.substring(0,index+1);

            while(qualityMatcher.find())
            {
                String quality  = htmlToParse.substring(qualityMatcher.start(),qualityMatcher.end());

                qualityInfo.add(quality);


            }
            while(m3u8LinkMatcher.find())
            {
                String qualityUrl  = baseUrl +htmlToParse.substring(m3u8LinkMatcher.start(),m3u8LinkMatcher.end());

                qualityUrls.add(qualityUrl);

            }
            currentQuality = 0;

        } catch (Exception e) {
            Log.i("qualityError",e.toString());
        }

    }

    @Override
    public void onUserLeaveHint()
    {
        if(android.os.Build.VERSION.SDK_INT>=26 && player.getPlayWhenReady()  )
            try
            {
                backStack="lost";
                int  x =  playerView.getPlayer().getPlayWhenReady()? R.drawable.pip_pause: R.drawable.pip_play;
                updatePictureInPictureActions(x,"soja",0,0,null);
                enterPictureInPictureMode(mPictureInPictureParamsBuilder.build());

            }catch (Exception e)
            {
                e.printStackTrace();
            }


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

                    if(player.getPlayWhenReady()) {
                        player.setPlayWhenReady(false);
                        updatePictureInPictureActions(R.drawable.pip_play, "play", 0, 0,intent);
                    }
                    else
                    {
                        player.setPlayWhenReady(true);
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

    void updatePictureInPictureActions(@DrawableRes int iconId, String title, int controlType, int requestCode, Intent newintent)
    {
        final ArrayList<RemoteAction> actions = new ArrayList<>();
        if(newintent==null)
            newintent=new Intent(ACTION_MEDIA_CONTROL).putExtra(EXTRA_CONTROL_TYPE,controlType);
        final PendingIntent intent =
                PendingIntent.getBroadcast(
                        WatchVideo.this,
                        requestCode,
                        newintent,
                        0);
        final Icon icon = Icon.createWithResource(WatchVideo.this, iconId);
        RemoteAction action=new RemoteAction(icon,title,title,intent);
        actions.add(action);
        mPictureInPictureParamsBuilder.setActions(actions);
        setPictureInPictureParams(mPictureInPictureParamsBuilder.build());
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();

        }

    }

    //  LifeCycleEvents
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("videolink",m3u8link);

        super.onSaveInstanceState(outState);
    }

    @Override
    public  void onPause()
    {
        super.onPause();

        time=player.getCurrentPosition();
    }
    @Override
    public void onStop()
    {
        super.onStop();
        time= player.getCurrentPosition();
        player.setPlayWhenReady(false);

    }
    @Override
    public  void  onResume() {
        super.onResume();

        if (!m3u8link.equals("")) {

            playerView.getPlayer().setPlayWhenReady(true);

        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {


        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
            playerView.getPlayer().release();
            if(backStack.equals("lost")) {

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
    void useFallBack(){
        player.release();
        Intent intent = new Intent(context, webvideo.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("videostreamlink",vidStreamUrl );
        startActivity(intent);
        finish();
    }

}
