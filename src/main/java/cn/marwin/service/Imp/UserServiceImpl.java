package cn.marwin.service.Imp;

import cn.marwin.dao.UserDao;
import cn.marwin.entity.User;
import cn.marwin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *
 * Title: UserServiceImpl
 * Description:
 * 用户操作实现类
 * Version:1.0.0
 * @author pancm
 * @date 2018年1月9日
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    @Override
    public boolean addUser(User user) {
        boolean flag=false;
        try{
            userDao.addUser(user);
            flag=true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    @Override
    public boolean updateUser(User user) {
        boolean flag=false;
        try{
            userDao.updateUser(user);
            flag=true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    @Override
    public boolean deleteUser(int id) {
        boolean flag=false;
        try{
            userDao.deleteUser(id);
            flag=true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return flag;
    }


    @Override
    public User findUserById(int userId) {
        return userDao.findById(userId);
    }

    @Override
    public User findUserByAge(int userAge) {
        return userDao.findByAge(userAge);
    }

    @Override
    public List<User> getUserList() {
        return userDao.findAll();
    }
}

