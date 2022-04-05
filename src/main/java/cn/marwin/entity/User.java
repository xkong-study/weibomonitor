package cn.marwin.entity;

import java.util.List;

public class User {
    /**
     * 编号
     */
    private Integer id;
    private String weiName;
    private String weiNo;
    /** 姓名 */
    /**
     * 年龄
     */
    private List<User> userList;
    private String userName;
    private String age;
    private String userId;
    private String userAge;

    public User() {

    }

    public User(String weiNo, String weiName, String age, String userName, String userId, String userAge) {
        this.weiNo = weiNo;
        this.weiName = weiName;
        this.age = age;
        this.userName = userName;
        this.userId = userId;
        this.userAge = userAge;
    }

    public List<User> getUserList() {
        return userList;
    }
    public void setUserList(List<User> userList) {
        this.userList=userList;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getWeiName() {
        return weiName;
    }

    public void setWeiName(String weiName) {
        this.weiName = weiName;
    }

    public String getWeiNo() {
        return weiNo;
    }

    public void setWeiNo(String weiNo) {
        this.weiNo = weiNo;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserAge() {
        return userAge;
    }

    public void setUserAge(String userAge) {
        this.userAge = userAge;
    }
}


