package com.stuffbox.webscraper.adapters;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.net.Uri;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;


import com.squareup.picasso.Picasso;
import com.stuffbox.webscraper.R;
import com.stuffbox.webscraper.activities.TestVideoView;
import com.stuffbox.webscraper.activities.WatchVideo;
import com.stuffbox.webscraper.database.AnimeDatabase;
import com.stuffbox.webscraper.models.Anime;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


public class AnimeDataAdapter extends RecyclerView.Adapter<AnimeDataAdapter.MyViewHolder> {

    private List<Anime> mAnimeList;

    int size;
    private Context context;
    SQLiteDatabase recent;

    public AnimeDataAdapter(Context context, List<Anime> AnimeList) {
        this.context = context;
        this.mAnimeList = AnimeList;

    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView title, episodeno;
        private Uri animeuri, imageuri;
        private ImageView imageofanime;

        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.animename);
            episodeno = view.findViewById(R.id.episodeno);
            imageofanime = view.findViewById(R.id.img);
            cardView = view.findViewById(R.id.cardview);
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
        holder.episodeno.setText(mAnimeList.get(position).getEpisodeNo());
        holder.animeuri = Uri.parse(mAnimeList.get(position).getLink());
        recent = context.openOrCreateDatabase("recent", Context.MODE_PRIVATE, null);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, WatchVideo.class);
                intent.putExtra("link", mAnimeList.get(position).getLink());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                int ep = holder.episodeno.getText().toString().lastIndexOf(" ");
                size = 0;
                AnimeDatabase database = AnimeDatabase.getInstance(context);
                //database.animeDao().deleteAnime(mAnimeList.get(position));
//                recent.execSQL("delete from anime where EPISODELINK='" + holder.animeuri.toString() + "'");
//                String z = "'" + holder.title.getText().toString() + "','" + holder.episodeno.getText().toString() + "','" + holder.animeuri.toString() + "','" + mAnimeList.get(position).getImageLink() + "','"+time+"'"; //sql string
//                String query = "INSERT INTO anime VALUES(" + z + ");";
//                Log.i("query",query);
//                recent.execSQL("INSERT INTO anime VALUES(" + z + ");");
                Anime temp = mAnimeList.get(position);
                database.animeDao().deleteAnimeByNameAndEpisodeNo(temp.getName(),temp.getEpisodeNo());
                 Anime anime = new Anime(temp.getName(),temp.getLink(),temp.getEpisodeNo(),temp.getImageLink(),temp.getTime());
                database.animeDao().insertAnime(anime);
                intent.putExtra("noofepisodes", holder.episodeno.getText().toString().substring(ep + 1, holder.episodeno.getText().toString().length()));
                intent.putExtra("animename", holder.title.getText().toString());
                intent.putExtra("imagelink", mAnimeList.get(position).getImageLink());
                intent.putExtra("size", size);
                intent.putExtra("camefrom", "mainactivity");

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

