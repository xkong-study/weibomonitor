package cn.marwin.entity;

public class Check {
    private String weiName;
    private Integer weiNo;
    public Check(String weiName,Integer weiNo) {
        this.weiName=weiName;
        this.weiNo=weiNo;
    }
    public String getWeiName() {
        return weiName;
    }

    public void setWeiName(String weiName) {
        this.weiName = weiName;
    }
    public Integer getWeiNo() {
        return weiNo;
    }

    public void setWeiNo(Integer weiNo) {
        this.weiNo = weiNo;
    }

}
