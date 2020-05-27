package com.stuffbox.webscraper.scrapers;

import com.stuffbox.webscraper.models.Quality;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VidstreamScraper extends  Scraper {
    private final Document gogoAnimeDocument;

   public  VidstreamScraper(Document gogoAnimeDocument) {
        this.gogoAnimeDocument = gogoAnimeDocument;

    }

    @Override
    public ArrayList<Quality> getQualityUrls() {
        ArrayList<Quality> qualities = new ArrayList<>();

        try {
            String vidStreamUrl = "https:" + gogoAnimeDocument.getElementsByClass("play-video").get(0).getElementsByTag("iframe").get(0).attr("src");
            //String vidCdnUrl = vidStreamUrl.replace("streaming.php", "load.php");

            Document vidStreamPageDocument = Jsoup.connect(vidStreamUrl).get();
            Pattern pattern = Pattern.compile("https:\\/\\/vidstreaming.io\\/goto.php?.*=");
            Matcher matcher = pattern.matcher(vidStreamPageDocument.outerHtml());
            matcher.find();
            String qualityUrl = vidStreamPageDocument.outerHtml().substring(matcher.start(),matcher.end());
            String quality = "Unknown";

            qualities.add(new Quality(quality,qualityUrl));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return qualities;

    }



    @Override
    public String getHost() {
        return "Exoplayer";
    }
}
