package cn.marwin.web;

import cn.marwin.crawler.CrawlTask;
import cn.marwin.entity.*;
import cn.marwin.service.CaculateService;
import cn.marwin.service.TestService;
import cn.marwin.service.UserService;
import cn.marwin.service.WeiboService;
import cn.marwin.util.FileUtils;
import cn.marwin.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.text.AttributedString;
import java.util.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PageController {

    @RequestMapping("/")
    public String index(Model model) {
        Jedis jedis = RedisUtil.getJedis();
        String timestamp = jedis.lindex("timestamp", 0);

        List<Hot> hotList = new ArrayList<>();
        for (int i = 1; i <= CrawlTask.HOT_LIST_SIZE; i++) {
            String hot_key = timestamp + ":hot:" + i;
            List<String> fields = jedis.hmget(hot_key, "desc", "scheme", "status");
            Hot hot = new Hot(fields.get(0), fields.get(1));
            if (fields.get(2) != null) {
                hot.setStatus(Double.valueOf(fields.get(2)));
            }

            List<Weibo> weiboList = new ArrayList<>();
            for (int j = 1; j <= CrawlTask.WB_LIST_SIZE; j++) {
                String wb_key = hot_key + ":wb:" + j;
                fields = jedis.hmget(wb_key, "id", "user", "pic", "time", "url", "content", "statuses_count", "follow_count", "followers_count", "reposts_count", "comments_count", "attitudes_count", "verified_reason", "posCount", "negCount", "otherCount", "female", "male", "noVerified", "orVerified", "blVerified");
                Weibo weibo = new Weibo(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5), fields.get(6), fields.get(7), fields.get(8), fields.get(9), fields.get(10), fields.get(11), fields.get(12));
                weibo.setKey(wb_key);
                if (fields.get(13) != null && fields.get(14) != null && fields.get(15) != null && fields.get(16) != null && fields.get(17) != null && fields.get(18) != null && fields.get(19) != null && fields.get(20) != null) {
                    weibo.setPosCount(Integer.valueOf(fields.get(13)));
                    weibo.setNegCount(Integer.valueOf(fields.get(14)));
                    weibo.setOtherCount(Integer.valueOf(fields.get(15)));
                    weibo.setFemale(Integer.valueOf(fields.get(16)));
                    weibo.setMale(Integer.valueOf(fields.get(17)));
                    weibo.setNoVerified(Integer.valueOf(fields.get(18)));
                    weibo.setOrVerified(Integer.valueOf(fields.get(19)));
                    weibo.setBlVerified(Integer.valueOf(fields.get(20)));
                }

                String cm_key = wb_key + ":cm";
                List<String> cms = jedis.lrange(cm_key, 0, 12);
                List<Comment> comments = new ArrayList<>();
                ObjectMapper mapper = new ObjectMapper();
                try {
                    for (String c : cms) {
                        Comment comment = mapper.readValue(c, Comment.class);
                        comments.add(comment);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                weibo.setComments(comments);
                weiboList.add(weibo);
            }
            hot.setWeiboList(weiboList);
            hotList.add(hot);
        }

        RedisUtil.returnResource(jedis);
        model.addAttribute("hotList", hotList);
        return "index1";
    }

    @RequestMapping("/detail")
    public String detail(String key, Model model) {
        Jedis jedis = RedisUtil.getJedis();

        List<String> fields = jedis.hmget(key, "id", "user", "pic", "time", "url", "content", "statuses_count", "follow_count", "followers_count", "reposts_count", "comments_count", "attitudes_count", "verified_reason", "posCount", "negCount", "otherCount", "female", "male", "noVerified", "orVerified", "blVerified");
        Weibo weibo = new Weibo(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5), fields.get(6), fields.get(7), fields.get(8), fields.get(9), fields.get(10), fields.get(11), fields.get(12));
        weibo.setKey(key);
        if (fields.get(13) != null && fields.get(14) != null && fields.get(15) != null && fields.get(16) != null && fields.get(17) != null && fields.get(18) != null && fields.get(19) != null && fields.get(20) != null) {
            weibo.setPosCount(Integer.valueOf(fields.get(13)));
            weibo.setNegCount(Integer.valueOf(fields.get(14)));
            weibo.setOtherCount(Integer.valueOf(fields.get(15)));
            weibo.setFemale(Integer.valueOf(fields.get(16)));
            weibo.setMale(Integer.valueOf(fields.get(17)));
            weibo.setNoVerified(Integer.valueOf(fields.get(18)));
            weibo.setOrVerified(Integer.valueOf(fields.get(19)));
            weibo.setBlVerified(Integer.valueOf(fields.get(20)));
        }

        String cm_key = key + ":cm";
        List<String> cms = jedis.lrange(cm_key, 0, 100);
        List<Comment> comments = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            for (String c : cms) {
                Comment comment = mapper.readValue(c, Comment.class);
                comments.add(comment);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        RedisUtil.returnResource(jedis);
        model.addAttribute("weibo", weibo);
        model.addAttribute("comments", comments);
        return "detail";
    }

    @RequestMapping("/feedback")
    public String feedback(String comment, String sentiment, Model model) {
        Jedis jedis = RedisUtil.getJedis();
        jedis.rpush("feedback:" + sentiment, comment);
        if (!comment.equals("可恨")) {
            jedis.rpush("fee:", comment);
        }
        List<String> fe = jedis.lrange("feedback:", 0, 200);
        List<String> fe1 = jedis.lrange("fee:", 0, 200);
        RedisUtil.returnResource(jedis);
        model.addAttribute("fe", fe);
        model.addAttribute("fe1", fe1);
        System.out.println(fe);
        System.out.println(fe1);
        return "feedback";
    }
//    @RequestMapping(value = "/feedback", method = RequestMethod.POST)
//    public String feedback(String comment, String sentiment,Model model) {
//        Jedis jedis = RedisUtil.getJedis();
//        if (sentiment.equals("pos") || sentiment.equals("neg")) {
//            jedis.rpush("feedback:" + sentiment, comment);
//        }
//        RedisUtil.returnResource(jedis);
//        model.addAttribute("msg", "用户名或密码错误，请重新登录！");
//        return "userinfo";
//    }

    @RequestMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/data")
    public String data() {
        return "data";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(User user, Model model, HttpSession session) {
        //获取用户名和密码
        String age = user.getAge();
        String userAge = user.getUserAge();
        String userName = user.getUserName();
        String userId = user.getUserId();
        if (age.equals("kong") && userId.equals("数据监控")) {
            //将用户对象添加到Session中
            session.setAttribute("USER_SESSION", user);
            //重定向到主页面的跳转方法
            return "index1";
        }
        model.addAttribute("msg", "用户名或密码错误，请重新登录！");
        return "login";
    }
    @RequestMapping("/userdata")
    public String userdata(Model model) {
        Jedis jedis = RedisUtil.getJedis();
        String timestamp = jedis.lindex("timestamp", 0);
        List<Hot> hotList = new ArrayList<>();
        for (int i = 1; i <= CrawlTask.HOT_LIST_SIZE; i++) {
            String hot_key = timestamp + ":hot:" + i;
            List<String> fields = jedis.hmget(hot_key, "desc", "scheme", "status");
            Hot hot = new Hot(fields.get(0), fields.get(1));
            if (fields.get(2) != null) {
                hot.setStatus(Double.valueOf(fields.get(2)));
            }

            List<Weibo> weiboList = new ArrayList<>();
            for (int j = 1; j <= CrawlTask.WB_LIST_SIZE; j++) {
                String wb_key = hot_key + ":wb:" + j;
                fields = jedis.hmget(wb_key, "id", "user", "pic", "time", "url", "content", "statuses_count", "follow_count", "followers_count", "reposts_count", "comments_count", "attitudes_count", "verified_reason", "posCount", "negCount", "otherCount", "female", "male", "noVerified", "orVerified", "blVerified");
                Weibo weibo = new Weibo(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5), fields.get(6), fields.get(7), fields.get(8), fields.get(9), fields.get(10), fields.get(11), fields.get(12));
                weibo.setKey(wb_key);
                if (fields.get(13) != null && fields.get(14) != null && fields.get(15) != null && fields.get(16) != null && fields.get(17) != null && fields.get(18) != null && fields.get(19) != null && fields.get(20) != null) {
                    weibo.setPosCount(Integer.valueOf(fields.get(13)));
                    weibo.setNegCount(Integer.valueOf(fields.get(14)));
                    weibo.setOtherCount(Integer.valueOf(fields.get(15)));
                    weibo.setFemale(Integer.valueOf(fields.get(16)));
                    weibo.setMale(Integer.valueOf(fields.get(17)));
                    weibo.setNoVerified(Integer.valueOf(fields.get(18)));
                    weibo.setOrVerified(Integer.valueOf(fields.get(19)));
                    weibo.setBlVerified(Integer.valueOf(fields.get(20)));
                }

                String cm_key = wb_key + ":cm";
                List<String> cms = jedis.lrange(cm_key, 0, 10);
                List<Comment> comments = new ArrayList<>();
                ObjectMapper mapper = new ObjectMapper();
                try {
                    for (String c : cms) {
                        Comment comment = mapper.readValue(c, Comment.class);
                        comments.add(comment);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                weibo.setComments(comments);
                weiboList.add(weibo);
            }
            hot.setWeiboList(weiboList);
            hotList.add(hot);
        }

        RedisUtil.returnResource(jedis);
        model.addAttribute("hotList", hotList);
        return "userdata";
    }

//
//    @RequestMapping(value = "/login", method = RequestMethod.POST)
//    public String login(HttpSession session, User user,Model model, RedirectAttributes redirectAttributes){
//        String age = user.getAge();
//        String userAge = user.getUserAge();
//        String userName= user.getUserName();
//        String userId= user.getUserId();
//        if ( age.equals("kong")  && userId.equals("运维")) {
//            //将用户对象添加到Session中
//            session.setAttribute("USER_SESSION", user);
//            //重定向到主页面的跳转方法
//            return "index1";
//        }
//       else {
//            redirectAttributes.addFlashAttribute("message","您的用户名或者密码有错误奥！请仔细检查~~");
//            return "login";
//        }
//
//    }


    @RequestMapping(value = "/logout")
    public String logout(HttpSession session) {
        //清除session
        session.invalidate();
        //重定向到登录页面的跳转方法
        return "login";
    }

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/userinfo")
    public String ModelAndView(Model model) {
        List<User> userList = userService.getUserList();
        model.addAttribute("userList", userList);
        System.out.printf(String.valueOf(model));
        return "userinfo";
    }


//    @RequestMapping("/check")
//    public ModelAndView createForm(HttpServletRequest httpServletRequest,Model model) {
//        String weiNo=httpServletRequest.getParameter("weiNo");
//        String weiName=httpServletRequest.getParameter("weiName");
//        model.addAttribute("weiNo", "weiNo");
//        model.addAttribute("weiName", "weiName");
//        return new ModelAndView("/check", "userModel", model);
//    }


    @RequestMapping("/wordShow")
    public String wordShow(Model model) {
        Jedis jedis = RedisUtil.getJedis();
        String timestamp = jedis.lindex("timestamp", 0);

        List<Hot> hotList = new ArrayList<>();
        for (int i = 1; i <= CrawlTask.HOT_LIST_SIZE; i++) {
            String hot_key = timestamp + ":hot:" + i;
            List<String> fields = jedis.hmget(hot_key, "desc", "scheme", "status", "perNum");
            Hot hot = new Hot(fields.get(0), fields.get(1));
            if (fields.get(2) != null && fields.get(3) != null) {
                hot.setStatus(Double.valueOf(fields.get(2)));
                hot.setPerNum(Integer.valueOf(fields.get(3)));
            }

            List<Weibo> weiboList = new ArrayList<>();
            for (int j = 1; j <= CrawlTask.WB_LIST_SIZE; j++) {
                String wb_key = hot_key + ":wb:" + j;
                fields = jedis.hmget(wb_key, "id", "user", "pic", "time", "url", "content", "statuses_count", "follow_count", "followers_count", "reposts_count", "comments_count", "attitudes_count", "verified_reason", "posCount", "negCount", "otherCount", "female", "male", "noVerified", "orVerified", "blVerified");
                Weibo weibo = new Weibo(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5), fields.get(6), fields.get(7), fields.get(8), fields.get(9), fields.get(10), fields.get(11), fields.get(12));
                weibo.setKey(wb_key);
                if (fields.get(13) != null && fields.get(14) != null && fields.get(15) != null && fields.get(16) != null && fields.get(17) != null && fields.get(18) != null && fields.get(19) != null && fields.get(20) != null) {
                    weibo.setPosCount(Integer.valueOf(fields.get(13)));
                    weibo.setNegCount(Integer.valueOf(fields.get(14)));
                    weibo.setOtherCount(Integer.valueOf(fields.get(15)));
                    weibo.setFemale(Integer.valueOf(fields.get(16)));
                    weibo.setMale(Integer.valueOf(fields.get(17)));
                    weibo.setNoVerified(Integer.valueOf(fields.get(18)));
                    weibo.setOrVerified(Integer.valueOf(fields.get(19)));
                    weibo.setBlVerified(Integer.valueOf(fields.get(20)));
                }
                String cm_key = wb_key + ":cm";
                List<String> cms = jedis.lrange(cm_key, 0, 10);
                List<Comment> comments = new ArrayList<>();
                ObjectMapper mapper = new ObjectMapper();
                try {
                    for (String c : cms) {
                        Comment comment = mapper.readValue(c, Comment.class);
                        comments.add(comment);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                weibo.setComments(comments);
                weiboList.add(weibo);
            }
            hot.setWeiboList(weiboList);
            hotList.add(hot);
        }

        RedisUtil.returnResource(jedis);
        model.addAttribute("hotList", hotList);
        return "wordShow";
    }

    @RequestMapping("/wordNeg")
    public String wordShow1(Model model) {
        Jedis jedis = RedisUtil.getJedis();
        String timestamp = jedis.lindex("timestamp", 0);

        List<Hot> hotList = new ArrayList<>();
        for (int i = 1; i <= CrawlTask.HOT_LIST_SIZE; i++) {
            String hot_key = timestamp + ":hot:" + i;
            List<String> fields = jedis.hmget(hot_key, "desc", "scheme", "status", "perNum");
            Hot hot = new Hot(fields.get(0), fields.get(1));
            if (fields.get(2) != null && fields.get(3) != null) {
                hot.setStatus(Double.valueOf(fields.get(2)));
                hot.setPerNum(Integer.valueOf(fields.get(3)));
            }

            List<Weibo> weiboList = new ArrayList<>();
            for (int j = 1; j <= CrawlTask.WB_LIST_SIZE; j++) {
                String wb_key = hot_key + ":wb:" + j;
                fields = jedis.hmget(wb_key, "id", "user", "pic", "time", "url", "content", "statuses_count", "follow_count", "followers_count", "reposts_count", "comments_count", "attitudes_count", "verified_reason", "posCount", "negCount", "otherCount", "female", "male", "noVerified", "orVerified", "blVerified");
                Weibo weibo = new Weibo(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5), fields.get(6), fields.get(7), fields.get(8), fields.get(9), fields.get(10), fields.get(11), fields.get(12));
                weibo.setKey(wb_key);
                if (fields.get(13) != null && fields.get(14) != null && fields.get(15) != null && fields.get(16) != null && fields.get(17) != null && fields.get(18) != null && fields.get(19) != null && fields.get(20) != null) {
                    weibo.setPosCount(Integer.valueOf(fields.get(13)));
                    weibo.setNegCount(Integer.valueOf(fields.get(14)));
                    weibo.setOtherCount(Integer.valueOf(fields.get(15)));
                    weibo.setFemale(Integer.valueOf(fields.get(16)));
                    weibo.setMale(Integer.valueOf(fields.get(17)));
                    weibo.setNoVerified(Integer.valueOf(fields.get(18)));
                    weibo.setOrVerified(Integer.valueOf(fields.get(19)));
                    weibo.setBlVerified(Integer.valueOf(fields.get(20)));
                }

                String cm_key = wb_key + ":cm";
                List<String> cms = jedis.lrange(cm_key, 0, 10);
                List<Comment> comments = new ArrayList<>();
                ObjectMapper mapper = new ObjectMapper();
                try {
                    for (String c : cms) {
                        Comment comment = mapper.readValue(c, Comment.class);
                        comments.add(comment);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                weibo.setComments(comments);
                weiboList.add(weibo);
            }
            hot.setWeiboList(weiboList);
            hotList.add(hot);
        }

        RedisUtil.returnResource(jedis);
        model.addAttribute("hotList", hotList);
        return "wordNeg";
    }

    @RequestMapping(value = "resources/word")
    public String IoReadImage(String imgName, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletOutputStream out = null;
        FileInputStream ips = null;
        try {
            //获取图片存放路径
            String imgPath = imgName;
            ips = new FileInputStream(new File(imgPath));
            response.setContentType("multipart/form-data");
            out = response.getOutputStream();
            //读取文件流
            int len = 0;
            byte[] buffer = new byte[1024 * 10];
            while ((len = ips.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            out.close();
            ips.close();
        }
        return null;
    }

    @RequestMapping("/sex")
    public String sex(Model model) {
        Jedis jedis = RedisUtil.getJedis();
        String timestamp = jedis.lindex("timestamp", 0);

        List<Hot> hotList = new ArrayList<>();
        for (int i = 1; i <= CrawlTask.HOT_LIST_SIZE; i++) {
            String hot_key = timestamp + ":hot:" + i;
            List<String> fields = jedis.hmget(hot_key, "desc", "scheme", "status");
            Hot hot = new Hot(fields.get(0), fields.get(1));
            if (fields.get(2) != null) {
                hot.setStatus(Double.valueOf(fields.get(2)));
            }

            List<Weibo> weiboList = new ArrayList<>();
            for (int j = 1; j <= CrawlTask.WB_LIST_SIZE; j++) {
                String wb_key = hot_key + ":wb:" + 1;
                fields = jedis.hmget(wb_key, "id", "user", "pic", "time", "url", "content", "statuses_count", "follow_count", "followers_count", "reposts_count", "comments_count", "attitudes_count", "verified_reason", "posCount", "negCount", "otherCount", "female", "male", "noVerified", "orVerified", "blVerified");
                Weibo weibo = new Weibo(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5), fields.get(6), fields.get(7), fields.get(8), fields.get(9), fields.get(10), fields.get(11), fields.get(12));
                weibo.setKey(wb_key);
                if (fields.get(13) != null && fields.get(14) != null && fields.get(15) != null && fields.get(16) != null && fields.get(17) != null && fields.get(18) != null && fields.get(19) != null && fields.get(20) != null) {
                    weibo.setPosCount(Integer.valueOf(fields.get(13)));
                    weibo.setNegCount(Integer.valueOf(fields.get(14)));
                    weibo.setOtherCount(Integer.valueOf(fields.get(15)));
                    weibo.setFemale(Integer.valueOf(fields.get(16)));
                    weibo.setMale(Integer.valueOf(fields.get(17)));
                    weibo.setNoVerified(Integer.valueOf(fields.get(18)));
                    weibo.setOrVerified(Integer.valueOf(fields.get(19)));
                    weibo.setBlVerified(Integer.valueOf(fields.get(20)));
                }

                String cm_key = wb_key + ":cm";
                List<String> cms = jedis.lrange(cm_key, 0, 10);
                List<Comment> comments = new ArrayList<>();
                ObjectMapper mapper = new ObjectMapper();
                try {
                    for (String c : cms) {
                        Comment comment = mapper.readValue(c, Comment.class);
                        comments.add(comment);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                weibo.setComments(comments);
                weiboList.add(weibo);
            }
            hot.setWeiboList(weiboList);
            hotList.add(hot);
        }

        RedisUtil.returnResource(jedis);
        model.addAttribute("hotList", hotList);
        return "sex";
    }

    @RequestMapping("/verified")
    public String verified(Model model) {
        Jedis jedis = RedisUtil.getJedis();
        String timestamp = jedis.lindex("timestamp", 0);

        List<Hot> hotList = new ArrayList<>();
        for (int i = 1; i <= CrawlTask.HOT_LIST_SIZE; i++) {
            String hot_key = timestamp + ":hot:" + i;
            List<String> fields = jedis.hmget(hot_key, "desc", "scheme", "status");
            Hot hot = new Hot(fields.get(0), fields.get(1));
            if (fields.get(2) != null) {
                hot.setStatus(Double.valueOf(fields.get(2)));
            }

            List<Weibo> weiboList = new ArrayList<>();
            for (int j = 1; j <= CrawlTask.WB_LIST_SIZE; j++) {
                String wb_key = hot_key + ":wb:" + j;
                fields = jedis.hmget(wb_key, "id", "user", "pic", "time", "url", "content", "statuses_count", "follow_count", "followers_count", "reposts_count", "comments_count", "attitudes_count", "verified_reason", "posCount", "negCount", "otherCount", "female", "male", "noVerified", "orVerified", "blVerified");
                Weibo weibo = new Weibo(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5), fields.get(6), fields.get(7), fields.get(8), fields.get(9), fields.get(10), fields.get(11), fields.get(12));
                weibo.setKey(wb_key);
                if (fields.get(13) != null && fields.get(14) != null && fields.get(15) != null && fields.get(16) != null && fields.get(17) != null && fields.get(18) != null && fields.get(19) != null && fields.get(20) != null) {
                    weibo.setPosCount(Integer.valueOf(fields.get(13)));
                    weibo.setNegCount(Integer.valueOf(fields.get(14)));
                    weibo.setOtherCount(Integer.valueOf(fields.get(15)));
                    weibo.setFemale(Integer.valueOf(fields.get(16)));
                    weibo.setMale(Integer.valueOf(fields.get(17)));
                    weibo.setNoVerified(Integer.valueOf(fields.get(18)));
                    weibo.setOrVerified(Integer.valueOf(fields.get(19)));
                    weibo.setBlVerified(Integer.valueOf(fields.get(20)));
                }
                String cm_key = wb_key + ":cm";
                List<String> cms = jedis.lrange(cm_key, 0, 10);
                List<Comment> comments = new ArrayList<>();
                ObjectMapper mapper = new ObjectMapper();
                try {
                    for (String c : cms) {
                        Comment comment = mapper.readValue(c, Comment.class);
                        comments.add(comment);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                weibo.setComments(comments);
                weiboList.add(weibo);
            }
            hot.setWeiboList(weiboList);
            hotList.add(hot);
        }

        RedisUtil.returnResource(jedis);
        model.addAttribute("hotList", hotList);
        return "verified";
    }

    @RequestMapping("/user1")
    public String user(Model model) {
        Jedis jedis = RedisUtil.getJedis();
        String timestamp = jedis.lindex("timestamp", 0);
        List<Hot> hotList = new ArrayList<>();
        for (int i = 1; i <= CrawlTask.HOT_LIST_SIZE; i++) {
            String hot_key = timestamp + ":hot:" + i;
            List<String> fields = jedis.hmget(hot_key, "desc", "scheme", "status");
            Hot hot = new Hot(fields.get(0), fields.get(1));
            if (fields.get(2) != null) {
                hot.setStatus(Double.valueOf(fields.get(2)));
            }

            List<Weibo> weiboList = new ArrayList<>();
            for (int j = 1; j <= CrawlTask.WB_LIST_SIZE; j++) {
                String wb_key = hot_key + ":wb:" + j;
                fields = jedis.hmget(wb_key, "id", "user", "pic", "time", "url", "content", "statuses_count", "follow_count", "followers_count", "reposts_count", "comments_count", "attitudes_count", "verified_reason", "posCount", "negCount", "otherCount", "female", "male", "noVerified", "orVerified", "blVerified");
                Weibo weibo = new Weibo(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5), fields.get(6), fields.get(7), fields.get(8), fields.get(9), fields.get(10), fields.get(11), fields.get(12));
                weibo.setKey(wb_key);
                if (fields.get(13) != null && fields.get(14) != null && fields.get(15) != null && fields.get(16) != null && fields.get(17) != null && fields.get(18) != null && fields.get(19) != null && fields.get(20) != null) {
                    weibo.setPosCount(Integer.valueOf(fields.get(13)));
                    weibo.setNegCount(Integer.valueOf(fields.get(14)));
                    weibo.setOtherCount(Integer.valueOf(fields.get(15)));
                    weibo.setFemale(Integer.valueOf(fields.get(16)));
                    weibo.setMale(Integer.valueOf(fields.get(17)));
                    weibo.setNoVerified(Integer.valueOf(fields.get(18)));
                    weibo.setOrVerified(Integer.valueOf(fields.get(19)));
                    weibo.setBlVerified(Integer.valueOf(fields.get(20)));
                }

                String cm_key = wb_key + ":cm";
                List<String> cms = jedis.lrange(cm_key, 0, 10);
                List<Comment> comments = new ArrayList<>();
                ObjectMapper mapper = new ObjectMapper();
                try {
                    for (String c : cms) {
                        Comment comment = mapper.readValue(c, Comment.class);
                        comments.add(comment);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                weibo.setComments(comments);
                weiboList.add(weibo);
            }
            hot.setWeiboList(weiboList);
            hotList.add(hot);
        }

        RedisUtil.returnResource(jedis);
        model.addAttribute("hotList", hotList);
        return "user1";
    }

    @Autowired
    private CaculateService CaculateService;
    private WeiboService WeiboService;

    @PostMapping(value = "/exportExcelWeibo")
    public void exportExcelWeibo(@RequestBody PageRequest pageRequest, HttpServletResponse res) {
        File file = CaculateService.createWeiboExcelFile(pageRequest);
        FileUtils.downloadFile(res, file, file.getName());
    }

    @RequestMapping(value = "/downloadsExcelDown1", method = RequestMethod.GET)
    public void downloadsExcelDown1( HttpServletResponse response) throws IOException {
        Jedis jedis = RedisUtil.getJedis();
        String timestamp = jedis.lindex("timestamp", 0);
        List<Hot> hotList = new ArrayList<>();
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("获取excel测试表格");
        HSSFRow row = null;
        row = sheet.createRow(1);//创建第一个单元格
        row.setHeight((short) (26.25 * 20));
        row.createCell(0).setCellValue("用户信息列表");//为第一行单元格设值
        CellRangeAddress rowRegion = new CellRangeAddress(0, 0, 0, 2);
        sheet.addMergedRegion(rowRegion);
        row.createCell(0).setCellValue("statuses_count");
        row.createCell(1).setCellValue("follow_count");
        row.createCell(2).setCellValue("followers_count");
        row.createCell(3).setCellValue("reposts_count");
        row.createCell(4).setCellValue("comments_count");
        row.createCell(5).setCellValue("attitudes_count");
        String hot_key1 = timestamp + ":hot:" + 1;
        List<String> fields = jedis.hmget(hot_key1, "desc", "scheme", "status");
        Hot hot = new Hot(fields.get(0), fields.get(1));
        if (fields.get(2) != null) {
            hot.setStatus(Double.valueOf(fields.get(2)));
        }
        for (int j = 1; j <= CrawlTask.WB_LIST_SIZE; j++) {
            String wb_key = hot_key1 + ":wb:" + j;
            fields = jedis.hmget(wb_key, "id", "user", "pic", "time", "url", "content", "statuses_count", "follow_count", "followers_count", "reposts_count", "comments_count", "attitudes_count", "verified_reason", "posCount", "negCount", "otherCount", "female", "male", "noVerified", "orVerified", "blVerified");
            Weibo weibo = new Weibo(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5), fields.get(6), fields.get(7), fields.get(8), fields.get(9), fields.get(10), fields.get(11), fields.get(12));
            weibo.setKey(wb_key);
            if (fields.get(13) != null && fields.get(14) != null && fields.get(15) != null && fields.get(16) != null && fields.get(17) != null && fields.get(18) != null && fields.get(19) != null && fields.get(20) != null) {
                weibo.setPosCount(Integer.valueOf(fields.get(13)));
                weibo.setNegCount(Integer.valueOf(fields.get(14)));
                weibo.setOtherCount(Integer.valueOf(fields.get(15)));
                weibo.setFemale(Integer.valueOf(fields.get(16)));
                weibo.setMale(Integer.valueOf(fields.get(17)));
                weibo.setNoVerified(Integer.valueOf(fields.get(18)));
                weibo.setOrVerified(Integer.valueOf(fields.get(19)));
                weibo.setBlVerified(Integer.valueOf(fields.get(20)));
                row = sheet.createRow(j + 1);
                row.createCell(0).setCellValue(fields.get(6));
                row.createCell(1).setCellValue(fields.get(7));
                row.createCell(2).setCellValue(fields.get(8));
                row.createCell(3).setCellValue(fields.get(9));
                row.createCell(4).setCellValue(fields.get(10));
                row.createCell(5).setCellValue(fields.get(11));
                sheet.setDefaultRowHeight((short) (16.5 * 20));
                //列宽自适应
                for (int p = 0; p <= 16; p++) {
                    sheet.autoSizeColumn(p);
                }
            }
        }
        String hot_key = timestamp + ":hot:" + 2;
        List<String> fields1 = jedis.hmget(hot_key, "desc", "scheme", "status");
        Hot hot1 = new Hot(fields1.get(0), fields1.get(1));
        if (fields1.get(2) != null) {
            hot1.setStatus(Double.valueOf(fields1.get(2)));
        }
        for (int p = 1; p <= CrawlTask.WB_LIST_SIZE; p++) {
            String wb_key1 = hot_key + ":wb:" + p;
            fields = jedis.hmget(wb_key1, "id", "user", "pic", "time", "url", "content", "statuses_count", "follow_count", "followers_count", "reposts_count", "comments_count", "attitudes_count", "verified_reason", "posCount", "negCount", "otherCount", "female", "male", "noVerified", "orVerified", "blVerified");
            Weibo weibo1 = new Weibo(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5), fields.get(6), fields.get(7), fields.get(8), fields.get(9), fields.get(10), fields.get(11), fields.get(12));
            weibo1.setKey(wb_key1);
            if (fields.get(13) != null && fields.get(14) != null && fields.get(15) != null && fields.get(16) != null && fields.get(17) != null && fields.get(18) != null && fields.get(19) != null && fields.get(20) != null) {
                weibo1.setPosCount(Integer.valueOf(fields.get(13)));
                weibo1.setNegCount(Integer.valueOf(fields.get(14)));
                weibo1.setOtherCount(Integer.valueOf(fields.get(15)));
                weibo1.setFemale(Integer.valueOf(fields.get(16)));
                weibo1.setMale(Integer.valueOf(fields.get(17)));
                weibo1.setNoVerified(Integer.valueOf(fields.get(18)));
                weibo1.setOrVerified(Integer.valueOf(fields.get(19)));
                weibo1.setBlVerified(Integer.valueOf(fields.get(20)));
                row = sheet.createRow(p + 2);
                row.createCell(0).setCellValue(fields.get(6));
                row.createCell(1).setCellValue(fields.get(7));
                row.createCell(2).setCellValue(fields.get(8));
                row.createCell(3).setCellValue(fields.get(9));
                row.createCell(4).setCellValue(fields.get(10));
                row.createCell(5).setCellValue(fields.get(11));
                sheet.setDefaultRowHeight((short) (16.5 * 20));
                //列宽自适应
                for (int q = 0; q <= 16; q++) {
                    sheet.autoSizeColumn(q);
                }
            }
        }
        String hot_key2 = timestamp + ":hot:" + 3;
        List<String> fields2 = jedis.hmget(hot_key2, "desc", "scheme", "status");
        Hot hot2 = new Hot(fields2.get(0), fields2.get(1));
        if (fields2.get(2) != null) {
            hot2.setStatus(Double.valueOf(fields2.get(2)));
        }
        for (int h = 1; h <= CrawlTask.WB_LIST_SIZE; h++) {
            String wb_key2 = hot_key2 + ":wb:" + h;
            fields = jedis.hmget(wb_key2, "id", "user", "pic", "time", "url", "content", "statuses_count", "follow_count", "followers_count", "reposts_count", "comments_count", "attitudes_count", "verified_reason", "posCount", "negCount", "otherCount", "female", "male", "noVerified", "orVerified", "blVerified");
            Weibo weibo2 = new Weibo(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5), fields.get(6), fields.get(7), fields.get(8), fields.get(9), fields.get(10), fields.get(11), fields.get(12));
            weibo2.setKey(wb_key2);
            if (fields.get(13) != null && fields.get(14) != null && fields.get(15) != null && fields.get(16) != null && fields.get(17) != null && fields.get(18) != null && fields.get(19) != null && fields.get(20) != null) {
                weibo2.setPosCount(Integer.valueOf(fields.get(13)));
                weibo2.setNegCount(Integer.valueOf(fields.get(14)));
                weibo2.setOtherCount(Integer.valueOf(fields.get(15)));
                weibo2.setFemale(Integer.valueOf(fields.get(16)));
                weibo2.setMale(Integer.valueOf(fields.get(17)));
                weibo2.setNoVerified(Integer.valueOf(fields.get(18)));
                weibo2.setOrVerified(Integer.valueOf(fields.get(19)));
                weibo2.setBlVerified(Integer.valueOf(fields.get(20)));
                row = sheet.createRow(h + 3);
                row.createCell(0).setCellValue(fields.get(6));
                row.createCell(1).setCellValue(fields.get(7));
                row.createCell(2).setCellValue(fields.get(8));
                row.createCell(3).setCellValue(fields.get(9));
                row.createCell(4).setCellValue(fields.get(10));
                row.createCell(5).setCellValue(fields.get(11));
                sheet.setDefaultRowHeight((short) (16.5 * 20));
                //列宽自适应
                for (int q = 0; q <= 16; q++) {
                    sheet.autoSizeColumn(q);
                }
            }
        }
                String hot_key3 = timestamp + ":hot:" + 4;
                List<String> fields3 = jedis.hmget(hot_key3, "desc", "scheme", "status");
                Hot hot3 = new Hot(fields3.get(0), fields3.get(1));
                if (fields3.get(2) != null) {
                    hot3.setStatus(Double.valueOf(fields2.get(2)));
                }
                for (int k = 1; k <= CrawlTask.WB_LIST_SIZE; k++) {
                    String wb_key3 = hot_key3 + ":wb:" + k;
                    fields = jedis.hmget(wb_key3, "id", "user", "pic", "time", "url", "content", "statuses_count", "follow_count", "followers_count", "reposts_count", "comments_count", "attitudes_count", "verified_reason", "posCount", "negCount", "otherCount", "female", "male", "noVerified", "orVerified", "blVerified");
                    Weibo weibo3 = new Weibo(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5), fields.get(6), fields.get(7), fields.get(8), fields.get(9), fields.get(10), fields.get(11), fields.get(12));
                    weibo3.setKey(wb_key3);
                    if (fields.get(13) != null && fields.get(14) != null && fields.get(15) != null && fields.get(16) != null && fields.get(17) != null && fields.get(18) != null && fields.get(19) != null && fields.get(20) != null) {
                        weibo3.setPosCount(Integer.valueOf(fields.get(13)));
                        weibo3.setNegCount(Integer.valueOf(fields.get(14)));
                        weibo3.setOtherCount(Integer.valueOf(fields.get(15)));
                        weibo3.setFemale(Integer.valueOf(fields.get(16)));
                        weibo3.setMale(Integer.valueOf(fields.get(17)));
                        weibo3.setNoVerified(Integer.valueOf(fields.get(18)));
                        weibo3.setOrVerified(Integer.valueOf(fields.get(19)));
                        weibo3.setBlVerified(Integer.valueOf(fields.get(20)));
                        row = sheet.createRow(k + 4);
                        row.createCell(0).setCellValue(fields.get(6));
                        row.createCell(1).setCellValue(fields.get(7));
                        row.createCell(2).setCellValue(fields.get(8));
                        row.createCell(3).setCellValue(fields.get(9));
                        row.createCell(4).setCellValue(fields.get(10));
                        row.createCell(5).setCellValue(fields.get(11));
                        sheet.setDefaultRowHeight((short) (16.5 * 20));
                        //列宽自适应
                        for (int q = 0; q <= 16; q++) {
                            sheet.autoSizeColumn(q);
                        }
                    }
                }

                                        String hot_key4 = timestamp + ":hot:" + 5;
                                        List<String> fields4 = jedis.hmget(hot_key4, "desc", "scheme", "status");
                                        Hot hot4 = new Hot(fields4.get(0), fields4.get(1));
                                        if (fields4.get(2) != null) {
                                            hot4.setStatus(Double.valueOf(fields4.get(2)));
                                        }
                                        for (int t = 1; t <= CrawlTask.WB_LIST_SIZE; t++) {
                                            String wb_key4 = hot_key4 + ":wb:" + t;
                                            fields = jedis.hmget(wb_key4, "id", "user", "pic", "time", "url", "content", "statuses_count", "follow_count", "followers_count", "reposts_count", "comments_count", "attitudes_count", "verified_reason", "posCount", "negCount", "otherCount", "female", "male", "noVerified", "orVerified", "blVerified");
                                            Weibo weibo4 = new Weibo(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5), fields.get(6), fields.get(7), fields.get(8), fields.get(9), fields.get(10), fields.get(11), fields.get(12));
                                            weibo4.setKey(wb_key4);
                                            if (fields.get(13) != null && fields.get(14) != null && fields.get(15) != null && fields.get(16) != null && fields.get(17) != null && fields.get(18) != null && fields.get(19) != null && fields.get(20) != null) {
                                                weibo4.setPosCount(Integer.valueOf(fields.get(13)));
                                                weibo4.setNegCount(Integer.valueOf(fields.get(14)));
                                                weibo4.setOtherCount(Integer.valueOf(fields.get(15)));
                                                weibo4.setFemale(Integer.valueOf(fields.get(16)));
                                                weibo4.setMale(Integer.valueOf(fields.get(17)));
                                                weibo4.setNoVerified(Integer.valueOf(fields.get(18)));
                                                weibo4.setOrVerified(Integer.valueOf(fields.get(19)));
                                                weibo4.setBlVerified(Integer.valueOf(fields.get(20)));
                                                row = sheet.createRow(t + 5);
                                                row.createCell(0).setCellValue(fields.get(6));
                                                row.createCell(1).setCellValue(fields.get(7));
                                                row.createCell(2).setCellValue(fields.get(8));
                                                row.createCell(3).setCellValue(fields.get(9));
                                                row.createCell(4).setCellValue(fields.get(10));
                                                row.createCell(5).setCellValue(fields.get(11));
                                                sheet.setDefaultRowHeight((short) (16.5 * 20));
                                                //列宽自适应
                                                for (int q = 0; q <= 16; q++) {
                                                    sheet.autoSizeColumn(q);
                                                }
                                            }
                                        }
        String hot_key5 = timestamp + ":hot:" + 6;
        List<String> fields5 = jedis.hmget(hot_key5, "desc", "scheme", "status");
        Hot hot5 = new Hot(fields5.get(0), fields5.get(1));
        if (fields5.get(2) != null) {
            hot5.setStatus(Double.valueOf(fields5.get(2)));
        }
        for (int k = 1; k <= CrawlTask.WB_LIST_SIZE; k++) {
            String wb_key3 = hot_key5 + ":wb:" + k;
            fields = jedis.hmget(wb_key3, "id", "user", "pic", "time", "url", "content", "statuses_count", "follow_count", "followers_count", "reposts_count", "comments_count", "attitudes_count", "verified_reason", "posCount", "negCount", "otherCount", "female", "male", "noVerified", "orVerified", "blVerified");
            Weibo weibo3 = new Weibo(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5), fields.get(6), fields.get(7), fields.get(8), fields.get(9), fields.get(10), fields.get(11), fields.get(12));
            weibo3.setKey(wb_key3);
            if (fields.get(13) != null && fields.get(14) != null && fields.get(15) != null && fields.get(16) != null && fields.get(17) != null && fields.get(18) != null && fields.get(19) != null && fields.get(20) != null) {
                weibo3.setPosCount(Integer.valueOf(fields.get(13)));
                weibo3.setNegCount(Integer.valueOf(fields.get(14)));
                weibo3.setOtherCount(Integer.valueOf(fields.get(15)));
                weibo3.setFemale(Integer.valueOf(fields.get(16)));
                weibo3.setMale(Integer.valueOf(fields.get(17)));
                weibo3.setNoVerified(Integer.valueOf(fields.get(18)));
                weibo3.setOrVerified(Integer.valueOf(fields.get(19)));
                weibo3.setBlVerified(Integer.valueOf(fields.get(20)));
                row = sheet.createRow(k + 6);
                row.createCell(0).setCellValue(fields.get(6));
                row.createCell(1).setCellValue(fields.get(7));
                row.createCell(2).setCellValue(fields.get(8));
                row.createCell(3).setCellValue(fields.get(9));
                row.createCell(4).setCellValue(fields.get(10));
                row.createCell(5).setCellValue(fields.get(11));
                sheet.setDefaultRowHeight((short) (16.5 * 20));
                //列宽自适应
                for (int q = 0; q <= 16; q++) {
                    sheet.autoSizeColumn(q);
                }
            }
        }
        String hot_key6 = timestamp + ":hot:" + 7;
        List<String> fields6 = jedis.hmget(hot_key6, "desc", "scheme", "status");
        Hot hot6 = new Hot(fields6.get(0), fields6.get(1));
        if (fields6.get(2) != null) {
            hot6.setStatus(Double.valueOf(fields6.get(2)));
        }
        for (int s = 1; s <= CrawlTask.WB_LIST_SIZE; s++) {
            String wb_key3 = hot_key6 + ":wb:" + s;
            fields = jedis.hmget(wb_key3, "id", "user", "pic", "time", "url", "content", "statuses_count", "follow_count", "followers_count", "reposts_count", "comments_count", "attitudes_count", "verified_reason", "posCount", "negCount", "otherCount", "female", "male", "noVerified", "orVerified", "blVerified");
            Weibo weibo3 = new Weibo(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5), fields.get(6), fields.get(7), fields.get(8), fields.get(9), fields.get(10), fields.get(11), fields.get(12));
            weibo3.setKey(wb_key3);
            if (fields.get(13) != null && fields.get(14) != null && fields.get(15) != null && fields.get(16) != null && fields.get(17) != null && fields.get(18) != null && fields.get(19) != null && fields.get(20) != null) {
                weibo3.setPosCount(Integer.valueOf(fields.get(13)));
                weibo3.setNegCount(Integer.valueOf(fields.get(14)));
                weibo3.setOtherCount(Integer.valueOf(fields.get(15)));
                weibo3.setFemale(Integer.valueOf(fields.get(16)));
                weibo3.setMale(Integer.valueOf(fields.get(17)));
                weibo3.setNoVerified(Integer.valueOf(fields.get(18)));
                weibo3.setOrVerified(Integer.valueOf(fields.get(19)));
                weibo3.setBlVerified(Integer.valueOf(fields.get(20)));
                row = sheet.createRow(s + 7);
                row.createCell(0).setCellValue(fields.get(6));
                row.createCell(1).setCellValue(fields.get(7));
                row.createCell(2).setCellValue(fields.get(8));
                row.createCell(3).setCellValue(fields.get(9));
                row.createCell(4).setCellValue(fields.get(10));
                row.createCell(5).setCellValue(fields.get(11));
                sheet.setDefaultRowHeight((short) (16.5 * 20));
                //列宽自适应
                for (int q = 0; q <= 16; q++) {
                    sheet.autoSizeColumn(q);
                }
            }
        }

        String hot_key7 = timestamp + ":hot:" + 8;
        List<String> fields7 = jedis.hmget(hot_key7, "desc", "scheme", "status");
        Hot hot7 = new Hot(fields7.get(0), fields7.get(1));
        if (fields7.get(2) != null) {
            hot7.setStatus(Double.valueOf(fields7.get(2)));
        }
        for (int k = 1; k <= CrawlTask.WB_LIST_SIZE; k++) {
            String wb_key3 = hot_key7 + ":wb:" + k;
            fields = jedis.hmget(wb_key3, "id", "user", "pic", "time", "url", "content", "statuses_count", "follow_count", "followers_count", "reposts_count", "comments_count", "attitudes_count", "verified_reason", "posCount", "negCount", "otherCount", "female", "male", "noVerified", "orVerified", "blVerified");
            Weibo weibo3 = new Weibo(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5), fields.get(6), fields.get(7), fields.get(8), fields.get(9), fields.get(10), fields.get(11), fields.get(12));
            weibo3.setKey(wb_key3);
            if (fields.get(13) != null && fields.get(14) != null && fields.get(15) != null && fields.get(16) != null && fields.get(17) != null && fields.get(18) != null && fields.get(19) != null && fields.get(20) != null) {
                weibo3.setPosCount(Integer.valueOf(fields.get(13)));
                weibo3.setNegCount(Integer.valueOf(fields.get(14)));
                weibo3.setOtherCount(Integer.valueOf(fields.get(15)));
                weibo3.setFemale(Integer.valueOf(fields.get(16)));
                weibo3.setMale(Integer.valueOf(fields.get(17)));
                weibo3.setNoVerified(Integer.valueOf(fields.get(18)));
                weibo3.setOrVerified(Integer.valueOf(fields.get(19)));
                weibo3.setBlVerified(Integer.valueOf(fields.get(20)));
                row = sheet.createRow(k + 8);
                row.createCell(0).setCellValue(fields.get(6));
                row.createCell(1).setCellValue(fields.get(7));
                row.createCell(2).setCellValue(fields.get(8));
                row.createCell(3).setCellValue(fields.get(9));
                row.createCell(4).setCellValue(fields.get(10));
                row.createCell(5).setCellValue(fields.get(11));
                sheet.setDefaultRowHeight((short) (16.5 * 20));
                //列宽自适应
                for (int q = 0; q <= 16; q++) {
                    sheet.autoSizeColumn(q);
                }
            }
        }

        String hot_key8 = timestamp + ":hot:" + 9;
        List<String> fields8 = jedis.hmget(hot_key8, "desc", "scheme", "status");
        Hot hot8 = new Hot(fields8.get(0), fields8.get(1));
        if (fields8.get(2) != null) {
            hot8.setStatus(Double.valueOf(fields8.get(2)));
        }
        for (int k = 1; k <= CrawlTask.WB_LIST_SIZE; k++) {
            String wb_key3 = hot_key8 + ":wb:" + k;
            fields = jedis.hmget(wb_key3, "id", "user", "pic", "time", "url", "content", "statuses_count", "follow_count", "followers_count", "reposts_count", "comments_count", "attitudes_count", "verified_reason", "posCount", "negCount", "otherCount", "female", "male", "noVerified", "orVerified", "blVerified");
            Weibo weibo3 = new Weibo(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5), fields.get(6), fields.get(7), fields.get(8), fields.get(9), fields.get(10), fields.get(11), fields.get(12));
            weibo3.setKey(wb_key3);
            if (fields.get(13) != null && fields.get(14) != null && fields.get(15) != null && fields.get(16) != null && fields.get(17) != null && fields.get(18) != null && fields.get(19) != null && fields.get(20) != null) {
                weibo3.setPosCount(Integer.valueOf(fields.get(13)));
                weibo3.setNegCount(Integer.valueOf(fields.get(14)));
                weibo3.setOtherCount(Integer.valueOf(fields.get(15)));
                weibo3.setFemale(Integer.valueOf(fields.get(16)));
                weibo3.setMale(Integer.valueOf(fields.get(17)));
                weibo3.setNoVerified(Integer.valueOf(fields.get(18)));
                weibo3.setOrVerified(Integer.valueOf(fields.get(19)));
                weibo3.setBlVerified(Integer.valueOf(fields.get(20)));
                row = sheet.createRow(k + 9);
                row.createCell(0).setCellValue(fields.get(6));
                row.createCell(1).setCellValue(fields.get(7));
                row.createCell(2).setCellValue(fields.get(8));
                row.createCell(3).setCellValue(fields.get(9));
                row.createCell(4).setCellValue(fields.get(10));
                row.createCell(5).setCellValue(fields.get(11));
                sheet.setDefaultRowHeight((short) (16.5 * 20));
                //列宽自适应
                for (int q = 0; q <= 16; q++) {
                    sheet.autoSizeColumn(q);
                }
            }
        }
        String hot_key9 = timestamp + ":hot:" + 10;
        List<String> fields9 = jedis.hmget(hot_key9, "desc", "scheme", "status");
        Hot hot9 = new Hot(fields9.get(0), fields9.get(1));
        if (fields9.get(2) != null) {
            hot9.setStatus(Double.valueOf(fields9.get(2)));
        }
        for (int k = 1; k <= CrawlTask.WB_LIST_SIZE; k++) {
            String wb_key3 = hot_key9 + ":wb:" + k;
            fields = jedis.hmget(wb_key3, "id", "user", "pic", "time", "url", "content", "statuses_count", "follow_count", "followers_count", "reposts_count", "comments_count", "attitudes_count", "verified_reason", "posCount", "negCount", "otherCount", "female", "male", "noVerified", "orVerified", "blVerified");
            Weibo weibo3 = new Weibo(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5), fields.get(6), fields.get(7), fields.get(8), fields.get(9), fields.get(10), fields.get(11), fields.get(12));
            weibo3.setKey(wb_key3);
            if (fields.get(13) != null && fields.get(14) != null && fields.get(15) != null && fields.get(16) != null && fields.get(17) != null && fields.get(18) != null && fields.get(19) != null && fields.get(20) != null) {
                weibo3.setPosCount(Integer.valueOf(fields.get(13)));
                weibo3.setNegCount(Integer.valueOf(fields.get(14)));
                weibo3.setOtherCount(Integer.valueOf(fields.get(15)));
                weibo3.setFemale(Integer.valueOf(fields.get(16)));
                weibo3.setMale(Integer.valueOf(fields.get(17)));
                weibo3.setNoVerified(Integer.valueOf(fields.get(18)));
                weibo3.setOrVerified(Integer.valueOf(fields.get(19)));
                weibo3.setBlVerified(Integer.valueOf(fields.get(20)));
                row = sheet.createRow(k + 10);
                row.createCell(0).setCellValue(fields.get(6));
                row.createCell(1).setCellValue(fields.get(7));
                row.createCell(2).setCellValue(fields.get(8));
                row.createCell(3).setCellValue(fields.get(9));
                row.createCell(4).setCellValue(fields.get(10));
                row.createCell(5).setCellValue(fields.get(11));
                sheet.setDefaultRowHeight((short) (16.5 * 20));
                //列宽自适应
                for (int q = 0; q <= 16; q++) {
                    sheet.autoSizeColumn(q);
                }
            }
        }
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                OutputStream os = response.getOutputStream();
                response.setHeader("Content-disposition", "attachment;filename=BGM.xls");//默认Excel名称
                wb.write(os);
                os.flush();
                os.close();
            }




    @RequestMapping("/mood")
    public String mood(Model model) {
        Jedis jedis = RedisUtil.getJedis();
        String timestamp = jedis.lindex("timestamp", 0);
        List<Hot> hotList = new ArrayList<>();
        for (int i = 1; i <= CrawlTask.HOT_LIST_SIZE; i++) {
            String hot_key = timestamp + ":hot:" + i;
            List<String> fields = jedis.hmget(hot_key, "desc", "scheme", "status");
            Hot hot = new Hot(fields.get(0), fields.get(1));
            if (fields.get(2) != null) {
                hot.setStatus(Double.valueOf(fields.get(2)));
            }

            List<Weibo> weiboList = new ArrayList<>();
            for (int j = 1; j <= CrawlTask.WB_LIST_SIZE; j++) {
                String wb_key = hot_key + ":wb:" + 1;
                fields = jedis.hmget(wb_key, "id", "user", "pic", "time", "url", "content", "statuses_count", "follow_count", "followers_count", "reposts_count", "comments_count", "attitudes_count","verified_reason", "posCount", "negCount", "otherCount", "female", "male", "noVerified", "orVerified", "blVerified");
                Weibo weibo = new Weibo(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4), fields.get(5), fields.get(6), fields.get(7), fields.get(8), fields.get(9), fields.get(10), fields.get(11),fields.get(12));
                weibo.setKey(wb_key);
                if (fields.get(13) != null && fields.get(14) != null && fields.get(15) != null && fields.get(16) != null && fields.get(17) != null && fields.get(18) != null && fields.get(19) != null && fields.get(20) != null) {
                    weibo.setPosCount(Integer.valueOf(fields.get(13)));
                    weibo.setNegCount(Integer.valueOf(fields.get(14)));
                    weibo.setOtherCount(Integer.valueOf(fields.get(15)));
                    weibo.setFemale(Integer.valueOf(fields.get(16)));
                    weibo.setMale(Integer.valueOf(fields.get(17)));
                    weibo.setNoVerified(Integer.valueOf(fields.get(18)));
                    weibo.setOrVerified(Integer.valueOf(fields.get(19)));
                    weibo.setBlVerified(Integer.valueOf(fields.get(20)));
                }

                String cm_key = wb_key + ":cm";
                List<String> cms = jedis.lrange(cm_key, 0, 10);
                List<Comment> comments = new ArrayList<>();
                ObjectMapper mapper = new ObjectMapper();
                try {
                    for (String c : cms) {
                        Comment comment = mapper.readValue(c, Comment.class);
                        comments.add(comment);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                weibo.setComments(comments);
                weiboList.add(weibo);
            }
            hot.setWeiboList(weiboList);
            hotList.add(hot);
        }

        RedisUtil.returnResource(jedis);
        model.addAttribute("hotList", hotList);
        return "mood";
    }

    @Autowired
    private TestService TestService;

    @PostMapping(value = "/exportExcelUser")
    public void exportExcelUser(@RequestBody PageRequest pageRequest, HttpServletResponse res) {
        File file = TestService.createUserExcelFile(pageRequest);
        FileUtils.downloadFile(res, file, file.getName());
    }

    @RestController
    @RequestMapping(value = "/user")
    public class UserRestController {
        @Autowired
        private UserService userService;

        @RequestMapping("/userList")
        public List<User> userList(Model model) {
            List<User> userList = userService.getUserList();
            model.addAttribute("userList", userList);
            return userService.getUserList();
        }

        @RequestMapping(value = "/downloadsExcelDown", method = RequestMethod.GET)
        public void downloadsExcelDown(HttpServletResponse response) throws IOException {
            List<User> bgmExcelDownloads = userService.getUserList();
            System.out.printf("------------" + bgmExcelDownloads.toString());
            HSSFWorkbook wb = new HSSFWorkbook();

            HSSFSheet sheet = wb.createSheet("获取excel测试表格");

            HSSFRow row = null;

            row = sheet.createRow(0);//创建第一个单元格
            row.setHeight((short) (26.25 * 20));
            row.createCell(0).setCellValue("用户信息列表");//为第一行单元格设值

            /*为标题设计空间
             * firstRow从第1行开始
             * lastRow从第0行结束
             *
             *从第1个单元格开始
             * 从第3个单元格结束
             */
            CellRangeAddress rowRegion = new CellRangeAddress(0, 0, 0, 2);
            sheet.addMergedRegion(rowRegion);

      /*CellRangeAddress columnRegion = new CellRangeAddress(1,4,0,0);
      sheet.addMergedRegion(columnRegion);*/

            row = sheet.createRow(1);
            row.setHeight((short) (22.50 * 20));//设置行高
            row.createCell(0).setCellValue("Id");//为第一个单元格设值
            row.createCell(1).setCellValue("员工工号");//为第二个单元格设值
            row.createCell(2).setCellValue("微博名");//为第三个单元格设值
            row.createCell(3).setCellValue("微博热搜排名");//为第四个单元格设值
            row.createCell(4).setCellValue("部门");
            row.createCell(5).setCellValue("情绪告警");
            row.createCell(6).setCellValue("员工名");
            //遍历所获取的数据
            for (int i = 0; i < bgmExcelDownloads.size(); i++) {
                row = sheet.createRow(i + 2);
                User user = bgmExcelDownloads.get(i);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getUserName());
                row.createCell(2).setCellValue(user.getWeiName());
                row.createCell(3).setCellValue(user.getWeiNo());
                row.createCell(4).setCellValue(user.getUserId());
                row.createCell(5).setCellValue(user.getUserAge());
                row.createCell(6).setCellValue(user.getAge());
            }
            sheet.setDefaultRowHeight((short) (16.5 * 20));
            //列宽自适应
            for (int i = 0; i <= 13; i++) {
                sheet.autoSizeColumn(i);
            }
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            OutputStream os = response.getOutputStream();
            response.setHeader("Content-disposition", "attachment;filename=BGM.xls");//默认Excel名称
            wb.write(os);
            os.flush();
            os.close();
        }

        @RequestMapping(value = "/addUser", method = RequestMethod.POST)
        public boolean addUser(User user) {//你这个对象的参数 传递的 值有问题 id地不需要
            System.out.println("开始新增...");
            System.out.println(user);
            return userService.addUser(user);
        }

        @RequestMapping(value = "/updateUser", method = RequestMethod.PUT)
        public boolean updateUser(User user) {
            System.out.println("开始更新...");
            return userService.updateUser(user);
        }

        @RequestMapping(value = "/deleteUser", method = RequestMethod.DELETE)
        public boolean delete(@RequestParam(value = "userName", required = true) int userId) {
            System.out.println("开始删除...");
            return userService.deleteUser(userId);
        }


//        @RequestMapping("/showUser")
//        public List<User> showUser(@RequestParam List<User> user){
//            System.out.println("开始展示...");
//            System.out.println(user);
//            return userService.showUser(user);
//
//        }

        @RequestMapping(value = "/userId", method = RequestMethod.GET)
        public User findByUserId(@RequestParam(value = "userId", required = true) int userId) {
            System.out.println("开始查询...");
            return userService.findUserById(userId);
        }

        @RequestMapping(value = "/userAge", method = RequestMethod.GET)
        public User findByUserAge(@RequestParam(value = "userAge", required = true) int userAge) {
            System.out.println("开始查询...");
            return userService.findUserByAge(userAge);
        }

    }
}

