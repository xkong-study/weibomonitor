package cn.marwin.crawler;

import cn.marwin.classifier.MyClassifier;
import cn.marwin.entity.Weibo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.marwin.entity.Comment;
import cn.marwin.entity.Hot;
import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.nlp.tokenizers.ChineseWordTokenizer;
import com.kennycason.kumo.palette.LinearGradientColorPalette;

import redis.clients.jedis.Jedis;
import cn.marwin.util.RedisUtil;

import java.awt.*;
import java.io.IOException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class CrawlTask extends TimerTask {
    public static final int HOT_LIST_SIZE = 10;          // 从热搜榜上获取的热搜数量
    public static final int WB_LIST_SIZE = 1;            // 每条热搜获取的weibo数量
    public static final int CM_LIST_SIZE = 5;            // 每条weibo获取的评论页数，一页最多20条评论
    public static final int KEY_EXPIRE_TIME = 60 * 60;   // redis里key的过期时间，单位为秒

    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            System.out.println("### 开始爬取微博 "  + formatter.format(LocalDateTime.now()) + " ###");
            crawl();
            word();
            word1();
            long end = System.currentTimeMillis();
            System.out.println("### 爬取微博结束，耗时：" + ((end - start) / 1000.0) + "s ###");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }


    public void word() throws IOException {

        //建立词频分析器，设置词频，以及词语最短长度，此处的参数配置视情况而定即可
        FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
        frequencyAnalyzer.setWordFrequenciesToReturn(600);
        frequencyAnalyzer.setMinWordLength(2);
        //引入中文解析器
        frequencyAnalyzer.setWordTokenizer(new ChineseWordTokenizer());
        final List<WordFrequency> wordFrequencies = frequencyAnalyzer.load("/Users/kong/Desktop/pubsenti-finder/src/main/resources/train/weibo/neg.txt");
//        final List<WordFrequency> wordFrequencies1 = frequencyAnalyzer.load("/Users/kong/Desktop/pubsenti-finder/src/main/resources/train/weibo/pos.txt");
        Dimension dimension = new Dimension(520, 520);
        WordCloud wordCloud = new WordCloud(dimension, CollisionMode.RECTANGLE);
        wordCloud.setPadding(0);
        java.awt.Font font = new java.awt.Font("STSong-Light", 2, 40);
        wordCloud.setBackgroundColor(new Color(255, 255, 255));
        wordCloud.setKumoFont(new KumoFont(font));
//        wordCloud.setBackground(new RectangleBackground(dimension));
        wordCloud.setBackground(new CircleBackground(255));
//        wordCloud.setBackground(new PixelBoundryBackground("D:\\lufei.jpg"));
        wordCloud.setColorPalette(new LinearGradientColorPalette(Color.RED, Color.BLUE, Color.GREEN, 30, 30));
        wordCloud.setFontScalar(new LinearFontScalar(10, 40));
        wordCloud.build(wordFrequencies);
        wordCloud.writeToFile("/Users/kong/Desktop/pubsenti-finder/src/main/resources/word/wordcloud.png");
//        wordCloud.writeToFile("/Users/kong/Desktop/pubsenti-finder/src/main/resources/word/wordcloud1.png");
    }
    public void word1() throws IOException {
        //建立词频分析器，设置词频，以及词语最短长度，此处的参数配置视情况而定即可
        FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
        frequencyAnalyzer.setWordFrequenciesToReturn(600);
        frequencyAnalyzer.setMinWordLength(2);
        //引入中文解析器
        frequencyAnalyzer.setWordTokenizer(new ChineseWordTokenizer());
        final List<WordFrequency> wordFrequencies = frequencyAnalyzer.load("/Users/kong/Desktop/pubsenti-finder/src/main/resources/train/weibo/pos.txt");
        Dimension dimension = new Dimension(520, 520);
        WordCloud wordCloud = new WordCloud(dimension, CollisionMode.RECTANGLE);
        wordCloud.setPadding(0);
        java.awt.Font font = new java.awt.Font("STSong-Light", 2, 40);
        wordCloud.setBackgroundColor(new Color(255, 255, 255));
        wordCloud.setKumoFont(new KumoFont(font));
//        wordCloud.setBackground(new RectangleBackground(dimension));
        wordCloud.setBackground(new CircleBackground(255));
//        wordCloud.setBackground(new PixelBoundryBackground("D:\\lufei.jpg"));
        wordCloud.setColorPalette(new LinearGradientColorPalette(Color.RED, Color.BLUE, Color.GREEN, 30, 30));
        wordCloud.setFontScalar(new LinearFontScalar(10, 40));
        wordCloud.build(wordFrequencies);

    }

    public void crawl() throws InterruptedException{
        long timestamp = System.currentTimeMillis();
        Jedis jedis = RedisUtil.getJedis();

        // 从热搜榜的请求里获取xhr的链接
        String url = "https://m.weibo.cn/api/container/getIndex?containerid=106003type%3D25%26t%3D3%26disable_hot%3D1%26filter_type%3Drealtimehot&title=%E5%BE%AE%E5%8D%9A%E7%83%AD%E6%90%9C";
        List<Hot> hotList = WeiboParser.getHotList(url, HOT_LIST_SIZE);
        int i = 1;
        for (Hot hot: hotList) {
            String hot_key = timestamp + ":hot:" + (i++); // timestamp:hot:{index}
            insertHot(jedis, hot_key, hot);

            List<Weibo> weiboList = WeiboParser.getWeiboList(hot, WB_LIST_SIZE);
            // 汇总该热搜下所有weibo的评论情况
            int allPosCount = 0;
            int allNegCount = 0;
            int j = 1;
            for (Weibo weibo : weiboList) {
                String wb_key = hot_key + ":wb:" + (j++); // timestamp:hot:{index}:wb:{index}
                insertWeibo(jedis, wb_key, weibo);

                List<Comment> commentList = WeiboParser.getCommentList(weibo, CM_LIST_SIZE);
                String cm_key = wb_key + ":cm";           // timestamp:hot:{index}:wb:{index}:cm
                // 统计该weibo下评论的情况
                int female=0;
                int male=0;
                int posCount = 0;
                int negCount = 0;
                int otherCount = 0;
                int hotscore=0;
                int noVerified=0;
                int orVerified=0;
                int blVerified=0;
                for (Comment comment: commentList) {
                    // 使用分类器评估评论的得分
                    double score = MyClassifier.getScore(comment.getText());
                    comment.setScore(score);
                    insertComment(jedis, cm_key, comment);

                    if (score > 0) {
                        posCount++;
                        allPosCount++;
                        hotscore++;
                    } else if (score < 0) {
                        negCount++;
                        allNegCount++;
                        hotscore++;
                    } else {
                        otherCount++;
                        hotscore++;
                    }
                    String gender=comment.getGender();
                    comment.setGender(gender);
                    if(Objects.equals(gender, "f")){
                        female++;
                    }
                    else if(Objects.equals(gender, "m")){
                        male++;
                    }
                    Integer verified= comment.getVerified();
                    comment.setVerified(verified);
                    if(verified>=2){
                        blVerified++;
                    }
                    else if(verified==0){
                        orVerified++;
                    }
                    else{
                        noVerified++;
                    }
                }
                jedis.expire(cm_key, KEY_EXPIRE_TIME);

                // 追加设置weibo的加工信息
                jedis.hsetnx(wb_key, "posCount", "" + posCount);
                jedis.hsetnx(wb_key, "negCount", "" + negCount);
                jedis.hsetnx(wb_key, "otherCount", "" + otherCount);
                jedis.hsetnx(wb_key, "hotscore", "" + hotscore);
                jedis.hsetnx(wb_key, "female", "" + female);
                jedis.hsetnx(wb_key, "male", "" + male);
                jedis.hsetnx(wb_key, "noVerified", "" + noVerified);
                jedis.hsetnx(wb_key, "orVerified", "" + orVerified);
                jedis.hsetnx(wb_key, "blVerified", "" + blVerified);
                //为了防止爬取过快导致403或触发反爬，每个微博爬完暂停3s
                Thread.sleep(5000);
            }
            // 追加设置hot的加工信息
            double status = (allPosCount + allNegCount) == 0 ? 0 : 1.0 * (allPosCount - allNegCount) / (allPosCount + allNegCount);
            int perNum=(allPosCount + allNegCount);
            jedis.hsetnx(hot_key, "status", "" + status);
            jedis.hsetnx(hot_key, "perNum", "" + perNum);
        }
        jedis.lpush("timestamp", "" + timestamp);// 逆序存储，最新的时间在最左侧
        RedisUtil.returnResource(jedis);
    }

    private void insertHot(Jedis jedis, String key, Hot hot) {
        Map<String, String> hot_value = new HashMap<>();
        hot_value.put("desc", hot.getDesc());
        hot_value.put("scheme", hot.getScheme());
        jedis.hmset(key, hot_value);
        jedis.expire(key, KEY_EXPIRE_TIME);
        System.out.println("Hot Insert: " + hot.getDesc());
    }

    private void insertWeibo(Jedis jedis, String key, Weibo weibo) {
        Map<String, String> wb_value = new HashMap<>();
        wb_value.put("id", weibo.getId());
        wb_value.put("url", weibo.getUrl());
        wb_value.put("user", weibo.getUser());
        wb_value.put("pic", weibo.getPic());
        wb_value.put("time", weibo.getTime());
        wb_value.put("content", weibo.getContent());
        wb_value.put("statuses_count", weibo.getStatuses_count());
        wb_value.put("follow_count", weibo.getFollow_count());
        wb_value.put("followers_count", weibo.getFollowers_count());
        wb_value.put("reposts_count", weibo.getReposts_count());
        wb_value.put("attitudes_count", weibo.getAttitudes_count());
        wb_value.put("comments_count", weibo.getComments_count());
        wb_value.put("verified_reason", weibo.getVerified_reason());
        jedis.hmset(key, wb_value);
        jedis.expire(key, KEY_EXPIRE_TIME);
    }

    private void insertComment(Jedis jedis, String key, Comment comment) {
        // 序列化为Json存储
        ObjectMapper mapper = new ObjectMapper();
        String cm_json = null;
        try {
            cm_json = mapper.writeValueAsString(comment);
            jedis.rpush(key, cm_json);
        } catch (JsonProcessingException e) {
            System.out.println("Comment序列化失败！");
            e.printStackTrace();
        }
    }


}
