package com.stuffbox.webscraper.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stuffbox.webscraper.Downloader;
import com.stuffbox.webscraper.R;
import com.stuffbox.webscraper.activities.WatchVideo;


public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.MyViewHolder> {
    private ArrayList<String> mSiteLink ;
    private  ArrayList<String> mEpisodeList;
    String animename;
    Activity activity;
    private Context context;
    SQLiteDatabase recent;
    private  String imagelink;
   public  EpisodeAdapter(Context context, ArrayList<String> SiteList, ArrayList<String> EpisodeList, String imagelink, String animename,Activity activity) {
        this.mSiteLink = SiteList;
        this.context=context;
        this.animename=animename;
        this.mEpisodeList=EpisodeList;
        this.imagelink=imagelink;
        this.activity=activity;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView button;
        private Button download;
        private LinearLayout layout;
        MyViewHolder(View view) {
            super(view);
            layout=view.findViewById(R.id.linearlayouta);
            button=view.findViewById(R.id.notbutton);
            download=view.findViewById(R.id.downloadchoice);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapterforepisode, parent, false);

        return new MyViewHolder(itemView);
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
holder.button.setText(animename+" Episode "+ (position+1));
      holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, WatchVideo.class);
                intent.putExtra("link",mSiteLink.get(position));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                recent=context.openOrCreateDatabase("recent", Context.MODE_PRIVATE,null);
                String z="'"+ animename+"','Episode "+(position+1)+"','"+mSiteLink.get(position)+"','"+imagelink+"'";
                intent.putExtra("animename",animename);
                intent.putExtra("imagelink",imagelink);
                recent.execSQL("delete from anime where EPISODELINK='"+mSiteLink.get(position)+"'");
                recent.execSQL("INSERT INTO anime VALUES("+z+");");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("noofepisodes",String.valueOf(mEpisodeList.size()));
                intent.putExtra("animenames",animename);
                intent.putExtra("selectepisodelink",mSiteLink.get(position));
                intent.putExtra("camefrom","selectepisode");
                context.getApplicationContext().startActivity(intent);
            }
        });
        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Downloader(mSiteLink.get(position),context,activity,animename,String.valueOf(position+1)).execute();

            }
        });
    }
    @Override
    public int getItemCount() {
        return mEpisodeList.size();
    }

}

