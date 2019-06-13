package com.stuffbox.webscraper.adapters;


import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import android.net.Uri;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.squareup.picasso.Picasso;
import com.stuffbox.webscraper.R;
import com.stuffbox.webscraper.activities.WatchVideo;
import com.stuffbox.webscraper.models.Anime;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


public class AnimeDataAdapter extends RecyclerView.Adapter<AnimeDataAdapter.MyViewHolder> {

    private ArrayList<Anime> mAnimeList;

    int size;
private Context context;
    SQLiteDatabase recent;
  public  AnimeDataAdapter(Context context, ArrayList<Anime> AnimeList) {
        this.context=context;
        this.mAnimeList = AnimeList;

    }
    class MyViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView title, episodeno;
        private Uri animeuri,imageuri;
        private ImageView imageofanime;
        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.animename);
            episodeno = view.findViewById(R.id.episodeno);
           imageofanime= view.findViewById(R.id.img);
           cardView= view.findViewById(R.id.cardview);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_data, parent, false);

        return new MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.title.setText(mAnimeList.get(position).getName());
       holder.episodeno.setText(mAnimeList.get(position).getEpisodeno());
        holder.animeuri= Uri.parse(mAnimeList.get(position).getLink());
         recent=context.openOrCreateDatabase("recent",Context.MODE_PRIVATE,null);
         holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, WatchVideo.class);
                intent.putExtra("link",mAnimeList.get(position).getLink());
               intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                int ep=holder.episodeno.getText().toString().lastIndexOf(" ");
                size=0;
                recent.execSQL("delete from anime where EPISODELINK='"+holder.animeuri.toString()+"'");
                String z="'"+ holder.title.getText().toString()+"','"+holder.episodeno.getText().toString()+"','"+holder.animeuri.toString()+"','"+mAnimeList.get(position).getImageLink()+"'"; //sql string
                recent.execSQL("INSERT INTO anime VALUES("+z+");");
                intent.putExtra("noofepisodes",holder.episodeno.getText().toString().substring(ep+1,holder.episodeno.getText().toString().length()));
                intent.putExtra("animename",holder.title.getText().toString());
                intent.putExtra("imagelink",mAnimeList.get(position).getImageLink());
                intent.putExtra("size",size);
                intent.putExtra("camefrom","mainactivity");

                context.getApplicationContext().startActivity(intent);
            }
        });

        Picasso.get().load(mAnimeList.get(position).getImageLink()).into(holder.imageofanime);
    }
    @Override
    public int getItemCount() {
        return mAnimeList.size();
    }

}

