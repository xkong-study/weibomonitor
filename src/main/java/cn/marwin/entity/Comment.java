package cn.marwin.entity;

public class Comment {
    // 元数据
    private String text;
    private String time;
    private Integer like;
    private String gender;
    private Integer follow_count;
    private Integer id;
    // 加工信息
    private Double score;
    private Integer female;
    private Integer male;
    private Integer verified;
    private Integer noVerified;
    private Integer orVerified;
    private Integer blVerified;


    public Comment() {}

    public Comment(String text, String time,Integer like,String gender,Integer verified,Integer follow_count,Integer id) {
        this.text = text;
        this.time = time;
        this.like = like;
        this.gender=gender;
        this.verified=verified;
        this.follow_count=follow_count;
        this.id =id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getLike() {
        return like;
    }

    public void setLike(Integer like) {
        this.like = like;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
    public String getGender() {return gender;}
    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setFemale(Integer female) {
        this.female = female;
    }
    public Integer getFemale() {return female;}

    public void setMale(Integer female) {
        this.male = male;
    }
    public Integer getMale() {return male;}

    public void setVerified(Integer verified) {
        this.verified = verified;
    }
    public Integer getVerified() {return verified;}

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
    public void setFollow_count(Integer follow_count) {
        this.follow_count = follow_count;
    }
    public Integer getFollow_count() {return follow_count;}

    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getId() {return id;}

}
