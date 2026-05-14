package com.ruoyi.system.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.system.constants.BySelf;
import com.ruoyi.system.domain.*;
import com.ruoyi.system.domain.gaode.GaoDeRegeocodeResponse;
import com.ruoyi.system.domain.vo.BookResult;
import com.ruoyi.system.domain.vo.ChapterResult;
import com.ruoyi.system.domain.vo.GuideVo;
import com.ruoyi.system.mapper.*;
import com.ruoyi.system.service.AIService;
import com.ruoyi.system.service.StartNavigationService;
import com.ruoyi.system.utils.AtArea;
import com.ruoyi.system.utils.GainRedisUtil;
import com.ruoyi.system.utils.JsonUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ClassName: StartNavigationServiceImpl
 * Package: com.ruoyi.system.service.impl
 * Description:
 *
 * @Author 张鹏
 * @Create 2025/9/12 09:32
 * @Version 1.0
 */
@Service
public class StartNavigationServiceImpl implements StartNavigationService {

    @Autowired
    private AmapService amapService;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private AreaMapper areaMapper;

    @Autowired
    private TrajectoryMapper trajectoryMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private StoryMapper storyMapper;

    @Autowired
    private ChapterMapper chapterMapper;

    @Autowired
    private AtArea atArea;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private GuideMapper guideMapper;

    @Autowired
    private AIService aiService;





    @Override
    public Map<String, Long> getTextCloud(NavigationData origin, NavigationData destination, String  bookType, String wordsType, String byWay, Integer userId) {

        // 确保经纬度格式正确：经度在前，纬度在后，以英文逗号分隔，小数点后不超过6位
        String address = origin.getLongitude().setScale(6, RoundingMode.HALF_UP).toString()+","+origin.getLatitude().setScale(6, RoundingMode.HALF_UP).toString();

        String extensions = "all";

        String myOrigin = origin.getLongitude().setScale(6, RoundingMode.HALF_UP).toString()+","+origin.getLatitude().setScale(6, RoundingMode.HALF_UP).toString();
        String myDestination = destination.getLongitude().setScale(6, RoundingMode.HALF_UP).toString()+","+destination.getLatitude().setScale(6, RoundingMode.HALF_UP).toString();
        //根据经纬度得到地名
        GaoDeRegeocodeResponse ipLocation = amapService.getReverseGeo( address, 1000);

        String infocode = ipLocation.getRegeocode().getAddressComponent().getAdcode();

        WeatherResponse weather = amapService.getWeather(infocode,extensions);
        //同时得到导航数据
        RouteResponse  drivingRoute = amapService.getDrivingRoute(myOrigin,myDestination);
        List<Step> steps = drivingRoute.getRoute().getPaths().get(0).getSteps();
        List<Step> tempStemp = new ArrayList<>();
        Integer totalDuration = 0;
        for (Step step : steps) {
            totalDuration += Integer.parseInt(step.getDuration());
            if (totalDuration > 1200) {
                tempStemp.add(step);
                totalDuration = 0;
            }
        }

        Trajectory trajectory = Trajectory.builder()
                .createArea(drivingRoute.getRoute().getOrigin())
                .endArea(drivingRoute.getRoute().getDestination())
                .text(byWay)
                .createTime(new Date())
                .endTime(new Date())
                .build();
        trajectoryMapper.insert(trajectory);
        List<Area> areas = new ArrayList<>();
        tempStemp.forEach(step -> {
            Area area = Area.builder()
                    .trajectoryId(trajectory.getId())
                    .duration(step.getDuration())
                    .roadName(step.getRoad())
                    .text(step.getInstruction())
                    .status(1)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            areas.add(area);
        });
        for (Area area : areas) {
            areaMapper.insert(area);
        }
        String prompt = "你是小说家。严格依据以下模板生成JSON，禁止输出任何额外文字、注释或思考过程。只输出纯JSON格式.2.总结，具体经纬度->近似的文化景观->素材;3.不要输出任何思考过程、解释或额外文字;5.依据参考内容按json输出\n";
        String trajectoryData = "从"+trajectory.getCreateArea()+"到"+trajectory.getEndArea()+"通行方式是"+trajectory.getText();
        String bookLength = ",题材风格为"+bookType+"小说长度是"+wordsType;
        String bookOutlineData = "\"book\": {\n" +
                "    \"id\": 1,\n" +
                "    \"name\": \"小说名称\",//字符串\n" +
                "    \"type\": \"小说类型从（奇幻，玄幻，言情，恐怖，都市，科幻）择其一\",//字符串\n" +
                "    \"world\": \"世界观设定\",//字符串\n" +
                "    \"style\": \"文笔风格描述\",//字符串\n" +
                "    \"core\": \"小说主旨\",//字符串\n" +
                "    \"beginning\": \"开篇内容描述\",//字符串\n" +
                "    \"development\": \"发展部分描述\",//字符串\n" +
                "    \"turningPoint\": \"转折点描述\",//字符串\n" +
                "    \"climax\": \"高潮部分描述\",//字符串\n" +
                "    \"ending\": \"结尾描述\",//字符串\n" +
                "    \"wordsType\": \"长篇\",//字符串\n" +
                "    \"branchNum\": 支线设计条数,\n" +
                "    \"control\":\"开篇，发展，转折，高潮，结尾对应的篇幅\",//字符串\n" +
                "    \"chapterNum\": 本书发展在第某章，默认为0,\n" +
                "    \"totalChapter\": 本书总共多少章\n" +
                "  },\n" +
                "  \"trajectories\": [//此为服务对象的状态，格式不能更改\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"createArea\": \"" + trajectory.getCreateArea() + "\",//字符串\n" +
                "      \"endArea\": \"" + trajectory.getEndArea() + "\",//字符串\n" +
                "      \"text\": \"" + trajectory.getText() + "\",//字符串\n" +
                "      \"createTime\": \"" + DateUtils.dateTimeNow("yyyy-MM-dd'T'HH:mm:ss") + "\",//字符串\n" +
                "      \"endTime\": \"" + DateUtils.dateTimeNow("yyyy-MM-dd'T'HH:mm:ss") + "\"//字符串\n" +
                "    }\n" +
                "  ],\n" +
                "  \"chapters\": [//暂时不写\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"words\": 3000,\n" +
                "      \"text\": \"章节内容文本\",\n" +
                "      \"aword\": \"第一章：开端\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"stories\": [//支线剧情大纲,每一个都是一个支线剧情的开篇，以便后续可以续写\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"chapterId\": 1,\n" +
                "      \"text\": \"故事情节简短描述\",//字符串\n" +
                "      \"textType\": \"开篇\",//字符串\n" +
                "      \"status\": 1,//此处永远置为1\n" +
                "      \"summary\":\"整条支线梗概总结包括篇幅所占篇幅\",//字符串\n" +
                "      \"share\":10//已占篇幅数量\n" +
                "      \"used\":0//已占篇幅数量，默认为0\n" +
                "    }\n" +
                "  ],\n" +
                "\n" +
                "   \"chapterStories\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"chapterId\": 1,\n" +
                "      \"storyId\": 1,\n" +
                "      \"summary\": \"章节与故事部分关联摘要\"//字符串\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";

        String fullPrompt = prompt + trajectoryData + bookLength + bookOutlineData;

        String chatJson = aiService.generateContent(fullPrompt);
        BookResult bookResult = null;
        try {
            bookResult = JsonUtils.deserializeToBookResult(chatJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        //4.存储到表里面
        Book book = bookResult.getBook();
        book.setUserId(userId);
        bookMapper.insert(bookResult.getBook());

        List<Story> stories = bookResult.getStories();
        for (Story story : stories) {
            storyMapper.insertStory(story);
        }

        Map<String,Long> strings =new HashMap<>();

        strings.put("bookId",bookResult.getBook().getId().longValue());
        strings.put("trajectoryId",trajectory.getId());
        return strings;
    }

    @Override
    public String getText(NavigationData origin, NavigationData destination,String  bookType, String wordsType) {
        // 确保经纬度格式正确：经度在前，纬度在后，以英文逗号分隔，小数点后不超过6位
        String address = origin.getLongitude().setScale(6, RoundingMode.HALF_UP).toString()+","+origin.getLatitude().setScale(6, RoundingMode.HALF_UP).toString();
        String extensions = "all";
        String addressName;
        String myOrigin = origin.getLongitude().setScale(6, RoundingMode.HALF_UP).toString()+","+origin.getLatitude().setScale(6, RoundingMode.HALF_UP).toString();
        String myDestination = destination.getLongitude().setScale(6, RoundingMode.HALF_UP).toString()+","+destination.getLatitude().setScale(6, RoundingMode.HALF_UP).toString();
        //根据经纬度得到地名
        GaoDeRegeocodeResponse ipLocation = amapService.getReverseGeo( address, 1000);

        String infocode = "000000"; // 默认值
        if (ipLocation != null && ipLocation.getRegeocode() != null && 
            ipLocation.getRegeocode().getAddressComponent() != null &&
            ipLocation.getRegeocode().getAddressComponent().getAdcode() != null) {
            infocode = ipLocation.getRegeocode().getAddressComponent().getAdcode();
        } else {
            if (ipLocation != null) {
                // 记录日志：无法获取有效的地区码
            }
        }
        
        WeatherResponse weather = null;
        if (infocode != null && !infocode.equals("000000") && !infocode.isEmpty() && infocode.matches("\\d{6}")) {
            //通过城市编码得到城市天气
            weather = amapService.getWeather(infocode, extensions);
            // 如果无法获取有效地区码，创建一个默认的天气响应
            weather = new WeatherResponse();
            weather.setStatus("0");
            weather.setInfo("无法获取有效地区码，使用默认天气信息");
            weather.setInfocode("10000");
            weather.setForecasts(Collections.emptyList());
        }

        //5.生成一章内容
        //将导航数据拼串喂给ai
        //1.提取关键字
        Map<String,String> strings =new HashMap<>();

        List<Forecast> lives = weather.getForecasts();

        //2.拼串
        String prompt1 = lives.toString();

        String prompt3 = "你的朋友在路上开车，我想依据信息给他介绍一下，但是我希望的措辞是合适的" +
                "，我给你信息，你直接把介绍的信息给我发过来，五十字"+
                "我会给你他城市的所在信息";
        String promptResult = prompt3+prompt1;
        //返回数据到前端
        String chat = aiService.generateText(promptResult);
        strings.put("voiceContent",chat);
        return chat;
    }

    @Override
    public String getChapter(Long bookId,Integer trajectoryId,Integer length){
        Book book = bookMapper.selectOneById(bookId);
        List<Story> storyList = new ArrayList<>();
        Chapter chapter  = new Chapter();
        Area area = atArea.getArea(trajectoryId, length);
        if(book != null){
            Integer abookId = book.getId();
            List<Chapter> chapters = chapterMapper.selectByBookId(bookId);
            List<Story> stories = storyMapper.selectStoryList(bookId);
            if (stories != null){
                for (Story story : stories){
                    story.getId();
                    if (Objects.equals(abookId, story.getBookId())){
                        storyList.add(story);
                    }
                }
                if (!storyList.isEmpty()){
                    // 检查chapters列表是否为空，避免越界
                    if (!chapters.isEmpty()) {
                        chapter = chapters.get(chapters.size() - 1);
                    } else {
                        // 如果chapters为空，初始化一个默认值或抛出有意义的异常
                        chapter = new Chapter(); // 或适当的默认对象
                    }
                }else {
                    // 检查chapters列表是否为空，避免越界
                    if (!chapters.isEmpty()) {
                        chapter = chapters.get(0);
                    } else {
                        // 如果chapters为空，初始化一个默认值或抛出有意义的异常
                        chapter = new Chapter(); // 或适当的默认对象
                    }
                }
            }
        }
        if (book != null && book.getChapterNum() < book.getTotalChapter()) {
            book.setChapterNum(book.getChapterNum() + 1);
            String prompt = getChapterPrompt(book, chapter, storyList);
            String prompt3 = "";
            if (area != null){
                prompt3= "我给你的素材有:1.地点名称：" + area.getRoadName() + "你根据地点的名称去发散，然后从发散的内容种得到这一章的素材"+
                        "2.地点描述:"+ area.getText() + "你根据地点的描述去发散，然后从发散的内容中得到这一章的素材";
            }
            //设置封装类得到story和chapter
            String chatJson = aiService.generateContent(prompt+prompt3);
            ChapterResult chapterResult = new ChapterResult();
            try {
                chapterResult = JsonUtils.deserializeToChapterResult(chatJson);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            //把序列化数据进行更新
            Chapter chapterA = chapterResult.getChapter();
            chapterA.setBookId(bookId.intValue());
            Story story = chapterResult.getStory();
            chapterMapper.insert(chapterA);
            if (story != null) {
                storyMapper.updateItem(story);
            }
            Map<String, String> strings = new HashMap<>();
            bookMapper.updateById(book);
            strings.put("chapter", chapterResult.getChapter().getText());
            return chapterResult.getChapter().getText();
        }
        return "结束";
    }

    private static String getChapterPrompt(Book book, Chapter chapter, List<Story> storyList) {
        String prompt = "给你介绍一本书的背景" + book + "\n" +
                "请给我生成第" + book.getChapterNum() + "章节(请你对于整本书这个章节应有内容生成)" +
                "它的前一章内容是" + chapter.getAword() + "前一章章节名" + chapter.getName() + "输出格式仅仅为:"
                + "{\n" +
                "  \"story\": {\n" +
                "    \"chapterId\": 1,//不做改变\n" +
                "    \"bookId\": 1001,//不做改变\n" +
                "    \"text\": \"这是一个故事文本内容，负责在一章内同其他故事融合。\",//类型文本\n" +
                "    \"textType\": \"开篇\",//根据文本调整，'文本类型（开篇/中间/转折/高潮/结尾）'\n" +
                "    \"status\": 1,//如果情节结束置为0，'状态：0-未发布 1-已发布 2-已删除'\n" +
                "    \"summary\": \"支线大致总结，包括角色发展、情节推进等，共占篇幅10章。\",//不做改变\n" +
                "    \"share\": 10,\n" +
                "    \"used\": 3\n" +
                "  }, \n" +
                "  \"chapter\": {\n" +
                "    \"bookId\": 1001,\n" +
                "    \"name\": 传说之地,//章节名称,不用写第几章,值可改\n" +
                "    \"words\": 5000,\n" +
                "    \"text\": \"这里是章节的具体内容，包括各种情节发展和人物对话...\",\n" +
                "    \"aword\": \"这里是对于本章的总结，以便下一章可以连贯的书写下去\"\n" +
                "  }\n" +
                "}";
        if (!storyList.isEmpty()) {
            String promptResult = "接下来给你介绍这一章的支线内容" + storyList;
            prompt = promptResult + prompt;
        }
        return prompt;
    }

    @Override
    public Map<String, String> getArea(String longitude, String latitude, Integer userId , String cacheKey,String content) {
        String oneByUserID = GainRedisUtil.getOneByUserID(userId);

        // 确保经纬度格式正确
        String address = longitude + "," + latitude;
        // 尝试从缓存获取摘要信息
        String um = null;
        if (cacheKey != null) {
            um = redisCache.getCacheObject(cacheKey);
        }

        cacheKey = generateCacheKey(BySelf.NOT_SELF_B);

        redisCache.appendCacheList(oneByUserID,cacheKey);


        // 获取逆地理编码信息
        GaoDeRegeocodeResponse ipLocation = amapService.getReverseGeo(address, 1000);
        String tourGuideContent;
        // 构建导游解说词
        if (content == null||content.isEmpty()){
            tourGuideContent = aiService.generateText(buildTopicBasedPrompt(
                    ipLocation.getRegeocode().getAddressComponent().getDistrict(),
                    um != null ? um : ""
            ));
        }else {
            tourGuideContent = aiService.generateText(buildTopicBasedPrompt(
                            ipLocation.getRegeocode().getAddressComponent().getDistrict(),
                            content
                    ) + "额外要求为" + content
            );
        }


        // 提取摘要并缓存
        String summary = buildSummaryPrompt(tourGuideContent);
        redisCache.setCacheObject(cacheKey, summary);

        Map<String, String> result = new HashMap<>();

        result.put(cacheKey,tourGuideContent);

        return result;
    }

    @Override
    public Map<String, String> getPoi(String longitude, String latitude,  Integer userId ,String cacheKey,String content) {
        String oneByUserID = GainRedisUtil.getOneByUserID(userId);

        // 确保经纬度格式正确
        String address = longitude + "," + latitude;

        // 尝试从缓存获取摘要信息
        String um = null;
        if (cacheKey != null) {
            um = redisCache.getCacheObject(cacheKey);
        }

        // 如果缓存不存在，生成新的 cacheKey

        cacheKey = generateCacheKey(BySelf.SELF_B);
        redisCache.appendCacheList(oneByUserID, cacheKey);

        // 获取逆地理编码信息
        GaoDeRegeocodeResponse ipLocation = amapService.getReverseGeo(address, 1000);
        String poi;

        if (content == null || content.isEmpty()){
            poi = aiService.generateText(buildTopicBasedPrompt(
                    ipLocation.getRegeocode().getFormattedAddress(),
                    ""
            ));
        } else {
            poi = aiService.generateContent(buildTopicBasedPrompt(
                            ipLocation.getRegeocode().getFormattedAddress(),
                            ""
                    ) +
                            "额外要求为：\n" +
                            content
            );
        }

        // 提取摘要并缓存
        String summary = buildSummaryPrompt(poi);

        redisCache.setCacheObject(cacheKey, summary);

        Map<String, String> result = new HashMap<>();

        result.put(cacheKey,poi);

        return result;
    }

    @Override
    public Long getTextCloudNoBook(
            NavigationData origin,
            NavigationData destination,
            String byWay
    ){
        String myOrigin = origin.getLongitude().setScale(6, RoundingMode.HALF_UP).toString()+","+origin.getLatitude().setScale(6, RoundingMode.HALF_UP).toString();
        String myDestination = destination.getLongitude().setScale(6, RoundingMode.HALF_UP).toString()+","+destination.getLatitude().setScale(6, RoundingMode.HALF_UP).toString();

        RouteResponse  drivingRoute = amapService.getDrivingRoute(myOrigin,myDestination);
        List<Step> steps = drivingRoute.getRoute().getPaths().get(0).getSteps();
        List<Step> tempStemp = new ArrayList<>();
        Integer totalDuration = 0;
        for (Step step : steps) {
            totalDuration += Integer.parseInt(step.getDuration());
            if (totalDuration > 1200) {
                tempStemp.add(step);
                totalDuration = 0;
            }
        }
        Trajectory trajectory = Trajectory.builder()
                .createArea(drivingRoute.getRoute().getOrigin())
                .endArea(drivingRoute.getRoute().getDestination())
                .text(byWay)
                .createTime(new Date())
                .endTime(new Date())
                .build();
        trajectoryMapper.insert(trajectory);
        return trajectory.getId();
    }

    @Override
    public String isNavigation(Integer userId, Boolean isNavigation) {
        String oneByUserID = GainRedisUtil.getOneByUserID(userId);
        List<String> cacheKeys = redisCache.getCacheList(oneByUserID);

        Guide guide = Guide.builder()
                .name("导航_" + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()))
                .userId(userId)
                .build();

        guideMapper.insert(guide);

        List<Aiarea> aiareas = new ArrayList<>();
        if (isNavigation==true){
            cacheKeys.forEach(cache -> {
                String cacheContent = redisCache.getCacheObject(cache);
                Aiarea cacheAiArea = Aiarea.builder()
                        .guideId(guide.getId())
                        .contentAi(cacheContent)
                        .type(cache.substring(0, 7))
                        .build();
                aiareas.add(cacheAiArea);
            });
        }

        cacheKeys.forEach(cache -> redisCache.deleteObject(cache));
        redisCache.deleteObject(oneByUserID);
        guideMapper.batchInsertAiarea(aiareas,guide.getId());
        return "success";
    }

    @Override
    public String deleteRedis(Integer userId) {
        String oneByUserID = GainRedisUtil.getOneByUserID(userId);
        if (redisCache.hasKey(oneByUserID)) {
            List<String> cacheList = redisCache.getCacheList(oneByUserID);
            cacheList.forEach(cache -> {
                redisCache.deleteObject(cache);
            });
            redisCache.deleteObject(oneByUserID);
            return "success";
        }
        return "fail";
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(Boolean self) {
        if (self){
            return "IS_SELF"+System.currentTimeMillis() + "_" +
                    (RANDOM.nextInt(900000) + 100000);
        }
        return "NO_SELF"+System.currentTimeMillis() + "_" +
                (RANDOM.nextInt(900000) + 100000);
    }

    // 在类级别添加 RANDOM 常量
    private static final Random RANDOM = new Random();
    /**
     * 构建基于话题的导游提示词 (优化版：快速生成)
     */
    private String buildTopicBasedPrompt(String selectedTopic, String cache) {
      if (cache.equals("")){
          return String.format(
                  """
                  你是专业导游名字叫“说到了”，正在为游客介绍当前位置。
                  【位置】%s
                  【指令】围绕选定话题，结合位置和天气，立即生成一篇 300-500 字的导游解说词。
                  【要求】
                  - 以选定话题为核心展开介绍
                  - 融入地理位置和天气情况
                  - 语言生动有趣，适合口头讲解
                  - 去掉星号引用号等符号，便于机器朗读
                  - 直接输出解说词，不要标题、序号或分析过程
                  """,
                  selectedTopic
          );
      }
        return String.format(
                """
                你是有博学的人，给人介绍文化
                1.以“说到了...”开头，以内容"%s"的后半部分的次要内容生成300-500字的内容
                2.不要与内容重复，不要与内容重复，不要与内容重复
                3.去掉*|"等符号，便于机器朗读
                """,
                cache
        );
    }

    private String buildSummaryPrompt(String content) {
        return String.format(
            """
            请从以下内容，提取文化点。

            【导游内容】
            %s

            【指令】用 100-200 字概括主要内容。

            【要求】
            - 思维发散，有文化（地点、特色、推荐等）
            - 不要推理，直接出内容
            - 仅输出总结内容，不要其他文字
            - 采用机器朗读，去掉*"“换行符等字符
            """,
            content
        );
    }


}
