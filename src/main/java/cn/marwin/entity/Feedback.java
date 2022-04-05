package cn.marwin.entity;

public class Feedback {
    private String comment;
    private String sentiment;
    private String feed;
    public Feedback(String comment, String sentiment){
        this.comment=comment;
        this.sentiment=sentiment;
    }

    public void setKey(String feed) {
        this.feed = feed;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }
    public void setFeed(String feed) {
        this.feed = feed;
    }
}