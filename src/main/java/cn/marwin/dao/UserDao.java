package cn.marwin.dao;

import cn.marwin.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface UserDao {
    public List<User> showUser();

    /**
     * 用户数据新增
     */
    @Insert("insert into t_user(weiName,weiNo,age,username,userId,userAge) values (#{weiName},#{weiNo},#{age},#{userName},#{userId},#{userAge})")
    void addUser(User user);

    /**
     * 用户数据修改
     */
    @Update("update t_user set weiName=#{weiName},weiNo=#{weiNo}，age#{age}，username#{userName}，userId#{userId}，userAge#{userAge}) where id=#{id}")
    void updateUser(User user);

    /**
     * 用户数据删除
     */
    @Delete("delete from t_user where id=#{id}")
    void deleteUser(int id);

    /**
     * 根据用户名称查询用户信息
     *
     */
    @Select("SELECT weiName,weiNo,age,username,userId,userAge from t_user")
    List<User> showUser(User user);

    /**
     * 根据用户ID查询用户信息
     *
     */
    @Select("SELECT id,weiName,weiNo FROM t_user where id=#{userId}")
    User findById(@Param("userId") int userId);


    /**
     * 根据用户age查询用户信息
     */
    @Select("SELECT id,weiName,weiNo FROM t_user where weiNo = #{userAge}")
    User findByAge(@Param("userAge") int userAge);


    @Select("SELECT * FROM t_user")
    List<User> findAll();
}

