package com.stuffbox.webscraper.models;

public class Anime {
    String name;
    String  link;
    String  episodeno;
    String imageLink;

    public Anime(String name, String link, String imageLink) {
        this.name = name;
        this.link = link;
        this.imageLink = imageLink;
        episodeno="";
    }

    public Anime(String name, String link, String episodeno, String imageLink) {
        this.name = name;
        this.link = link;
        this.episodeno = episodeno;
        this.imageLink = imageLink;
    }

    public Anime()
    {

    }

    public String getEpisodeno() {
        return episodeno;
    }

    public void setEpisodeno(String episodeno) {
        this.episodeno = episodeno;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }
}
