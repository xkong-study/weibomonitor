package cn.marwin.entity;

import java.util.List;

public class Weibo {
    // 元数据
    private String id;
    private String user;
    private String pic;
    private String time;
    private String url;
    private String content;
    private String follow_count;
    private String followers_count;
    private String reposts_count;
    private String comments_count;
    private String attitudes_count;
    private String verified_reason;
    // 加工信息
    private String key; // 在redis中存储的key
    private int posCount;
    private int negCount;
    private int otherCount;
    private int female;
    private Integer male;
    private Integer orVerified;
    private Integer blVerified;
    private Integer noVerified;
    private List<Comment> comments;
    private String statuses_count;
    public Weibo() {}

    public Weibo(String id, String user, String pic, String time, String url, String content, String statuses_count, String follow_count, String followers_count, String reposts_count, String comments_count, String attitudes_count, String verified_reason) {
        this.id = id;
        this.user = user;
        this.pic = pic;
        this.time = time;
        this.url = url;
        this.content = content;
        this.statuses_count=statuses_count;
        this.follow_count=follow_count;
        this.followers_count=followers_count;
        this.reposts_count=reposts_count;
        this.attitudes_count=attitudes_count;
        this.comments_count=comments_count;
        this.verified_reason = verified_reason;
    }

    public Weibo(String id,String statuses_count, String follow_count, String followers_count, String reposts_count, String comments_count, String attitudes_count) {
        this.id = id;
        this.statuses_count=statuses_count;
        this.follow_count=follow_count;
        this.followers_count=followers_count;
        this.reposts_count=reposts_count;
        this.attitudes_count=attitudes_count;
        this.comments_count=comments_count;
    }


    public String getVerified_reason() {
        return verified_reason;
    }
    public String getFollowers_count() {
        return followers_count;
    }

    public void setFollowers_count(String followers_count) {
        this.followers_count = followers_count;
    }

    public String getComments_count() {
        return comments_count;
    }

    public void setComments_count(String comments_count) {
        this.comments_count = comments_count;
    }

    public String getAttitudes_count(){return attitudes_count;}
    public void setAttitudes_count(String attitudes_count){this.attitudes_count=attitudes_count;}

    public String getReposts_count() {
        return reposts_count;
    }

    public void setReposts_count(String reposts_count) {
        this.reposts_count = reposts_count;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPosCount() {
        return posCount;
    }

    public void setPosCount(int posCount) {
        this.posCount = posCount;
    }

    public int getNegCount() {
        return negCount;
    }

    public void setNegCount(int negCount) {
        this.negCount = negCount;
    }
    public String getFollow_count() {
        return follow_count;
    }

    public void setFollow_count(String follow_count) {
        this.follow_count = follow_count;
    }

    public int getOtherCount() {
        return otherCount;
    }

    public void setOtherCount(int otherCount) {
        this.otherCount = otherCount;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }
    public String getStatuses_count() {
        return statuses_count;
    }
    public void setStatuses_count(String statuses_count){this.statuses_count=statuses_count;}
    public void setFemale(Integer female) {
        this.female = female;
    }
    public Integer getFemale() {return female;}

    public void setMale(Integer male) {this.male = male;}
    public Integer getMale() {return male;}
    public void setOrVerified(Integer orVerified) {
        this.orVerified = orVerified;
    }
    public Integer getOrVerified() {return orVerified;}
    public void setBlVerified(Integer blVerified) {
        this.blVerified = blVerified;
    }
    public Integer getBlVerified() {return blVerified;}
    public void setNoVerified(Integer noVerified) {
        this.noVerified = noVerified;
    }
    public Integer getNoVerified() {return noVerified;}

}