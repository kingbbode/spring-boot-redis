package com.kingbbode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootRedisApplication.class)
public class SpringBootRedisApplicationTests {

    @Resource(name="redisTemplate")
    private ValueOperations<String, String> valueOperations;

    @Resource(name = "redisTemplate")
    private ListOperations<String, String> listOperations;

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOperations;

    @Resource(name = "redisTemplate")
    private SetOperations<String, String> setOperations;

    @Resource(name="redisTemplate")
    private ZSetOperations<String, String> zSetOperations;

    @Before
    public void init() {
        //list put
        valueOperations.set("test:line", "-------");
        listOperations.rightPush("test:task", "자기소개");
        listOperations.rightPush("test:task", "취미소개");
        listOperations.rightPush("test:task", "소망소개");
        listOperations.rightPush("test:task", "선임이직");
        //hash put
        hashOperations.put("test:user:kingbbode", "name", "권뽀대");
        hashOperations.put("test:user:kingbbode", "age", "28");
        //set put
        setOperations.add("test:user:kingbbode:hobby", "개발");
        setOperations.add("test:user:kingbbode:hobby", "잠");
        setOperations.add("test:user:kingbbode:hobby", "옷 구경");
        //zset
        zSetOperations.add("test:user:kingbbode:wish", "배포한 것에 장애없길", 1);
        zSetOperations.add("test:user:kingbbode:wish", "배포한거 아니여도 장애없길", 2);
        zSetOperations.add("test:user:kingbbode:wish", "경력직 채용", 3);
    }

    @Test
    public void redisTest1() {
        String task = listOperations.leftPop("test:task");
        StringBuilder stringBuilder = new StringBuilder();
        while (task != null) {
            valueOperations.get("test:line");
            switch (task) {
                case "자기소개":
                    Map<String, String> intro = hashOperations.entries("test:user:kingbbode");
                    stringBuilder.append("\n******자기소개********");
                    stringBuilder.append("\n이름은 ");
                    stringBuilder.append(intro.get("name"));
                    stringBuilder.append("\n나이는 ");
                    stringBuilder.append(intro.get("age"));
                    break;
                case "취미소개":
                    Set<String> hobbys = setOperations.members("test:user:kingbbode:hobby");
                    stringBuilder.append("\n******취미소개******");
                    stringBuilder.append("취미는");
                    for (String hobby : hobbys) {
                        stringBuilder.append("\n");
                        stringBuilder.append(hobby);
                    }
                    break;
                case "소망소개":
                    Set<String> wishes = zSetOperations.range("test:user:kingbbode:wish", 0, 2);
                    stringBuilder.append("\n******소망소개******");
                    int rank = 1;
                    for (String wish : wishes){
                        stringBuilder.append("\n");
                        stringBuilder.append(rank);
                        stringBuilder.append("등 ");
                        stringBuilder.append(wish);
                        rank++;
                    }
                    break;
                case "선임이직":
                    stringBuilder.append("\n!!! 믿었던 선임 이직");
                    zSetOperations.incrementScore("test:user:kingbbode:wish", "경력직 채용", -1);
                    listOperations.rightPush("test:task", "소망소개");
                    break;
                default:
                    stringBuilder.append("nonone");

            }
            task = listOperations.leftPop("test:task");
        }
        System.out.println(stringBuilder.toString());
    }
}