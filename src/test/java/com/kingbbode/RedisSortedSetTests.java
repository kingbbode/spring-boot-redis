package com.kingbbode;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootRedisApplication.class)
public class RedisSortedSetTests {
    
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource(name = "redisTemplate")
    private ZSetOperations<String, String> zSetOperations;

    @Before
    public void init() {
        //init remove
        zSetOperations.remove("kingbbode:news:total", "77777777");
        zSetOperations.remove("kingbbode:news:total", "66666666");
        zSetOperations.remove("kingbbode:news:total", "55555555");
        zSetOperations.remove("kingbbode:news:total", "44444444");
        zSetOperations.remove("kingbbode:news:total", "3333333");
        zSetOperations.remove("kingbbode:news:total", "2222222");
        zSetOperations.remove("kingbbode:news:total", "11111111");
        zSetOperations.remove("kingbbode:news:total", "00000000");
        zSetOperations.remove("kingbbode:news:total:kingbbode", "11111111");
        zSetOperations.remove("kingbbode:news:total:kingbbode", "2222222");
        zSetOperations.remove("kingbbode:news:total:kingbbode", "3333333");
        zSetOperations.remove("kingbbode:news:total:kingbbode", "44444444");
        zSetOperations.remove("kingbbode:news:total:kingbbode", "55555555");
        zSetOperations.remove("kingbbode:news:total:kingbbode", "66666666");
        
        //total
        zSetOperations.incrementScore("kingbbode:news:total", "77777777", 10000);
        zSetOperations.incrementScore("kingbbode:news:total", "66666666", 9999);
        zSetOperations.incrementScore("kingbbode:news:total", "55555555", 9998);
        zSetOperations.incrementScore("kingbbode:news:total", "44444444", 9997);
        zSetOperations.incrementScore("kingbbode:news:total", "3333333", 9996);
        zSetOperations.incrementScore("kingbbode:news:total", "2222222", 9995);
        zSetOperations.incrementScore("kingbbode:news:total", "11111111", 9994);       
        zSetOperations.incrementScore("kingbbode:news:total", "00000000", 9993);
        
        //user1
        zSetOperations.incrementScore("kingbbode:news:total:kingbbode", "11111111", 1);
        zSetOperations.incrementScore("kingbbode:news:total:kingbbode", "2222222", 2);
        zSetOperations.incrementScore("kingbbode:news:total:kingbbode", "3333333", 3);
        zSetOperations.incrementScore("kingbbode:news:total:kingbbode", "44444444", 4);
        zSetOperations.incrementScore("kingbbode:news:total:kingbbode", "55555555", 5);
        zSetOperations.incrementScore("kingbbode:news:total:kingbbode", "66666666", 6);        
    }

    @Test
    @Ignore
    public void reverseRangeByScore_조회수가_큰_순으로_5개_가져오기() {
        Set<String> contents = zSetOperations.reverseRangeByScore("kingbbode:news:total", 0, Double.MAX_VALUE, 0, 5);
        Iterator<String> it = contents.iterator();
        assertEquals(contents.size(), 5);
        assertEquals(it.next(), "77777777");
        assertEquals(it.next(), "66666666");
        assertEquals(it.next(), "55555555");
        assertEquals(it.next(), "44444444");
        assertEquals(it.next(), "3333333");
        assertFalse(it.hasNext());
    }
    
    @Test
    @Ignore
    public void reverseRangeByScore_조회수가_변동할_때_다시_잘_가져오기(){
        for(int i=0;i<10;i++) {
            zSetOperations.incrementScore("kingbbode:news:total", "11111111", 1);
            zSetOperations.incrementScore("kingbbode:news:total", "00000000", 1);
        }
        assertEquals(zSetOperations.score("kingbbode:news:total", "11111111"), new Double(10004));
        assertEquals(zSetOperations.score("kingbbode:news:total", "00000000"), new Double(10003));
        
        Set<String> contents = zSetOperations.reverseRangeByScore("kingbbode:news:total", 0, Double.MAX_VALUE, 0, 5);
        
        Iterator<String> it = contents.iterator();
        assertEquals(contents.size(), 5);
        
        assertEquals(it.next(), "11111111");
        assertEquals(it.next(), "00000000");
        assertEquals(it.next(), "77777777");
        assertEquals(it.next(), "66666666");
        assertEquals(it.next(), "55555555");
        assertFalse(it.hasNext());
    }
    
    @Test
    @Ignore
    public void reverseRangeByScore_사용자_데이터_잘_가져오기(){
        Set<String> contents = zSetOperations.reverseRangeByScore("kingbbode:news:total:kingbbode", 0, Double.MAX_VALUE, 0, 2);
        Iterator<String> it = contents.iterator();
        assertEquals(contents.size(), 2);
        assertEquals(it.next(), "66666666");
        assertEquals(it.next(), "55555555");
        assertFalse(it.hasNext());
    }
    
    
    
    @Test
    public void reverseRangeByScore_사용자_데이터_페이징하기(){
        //페이지 요청시 기존 current 데이터 지우고 현재 실시간 데이터로 갱신
        Set<ZSetOperations.TypedTuple<String>> currentContents = zSetOperations.reverseRangeByScoreWithScores("kingbbode:news:total:kingbbode", 0, Double.MAX_VALUE, 0, 7);
        redisTemplate.delete("kingbbode:news:current:kingbbode");
        zSetOperations.add("kingbbode:news:current:kingbbode", currentContents);
        //페이지 요청시에도 최초 데이터는 줘야하니까 조회해서 리턴
        Set<String> contents = zSetOperations.reverseRangeByScore("kingbbode:news:total:kingbbode", 0, Double.MAX_VALUE, 0, 2);
        Iterator<String> it = contents.iterator();
        assertEquals(contents.size(), 2);
        assertEquals(it.next(), "66666666");
        assertEquals(it.next(), "55555555"); //is Last Contents ID
        assertFalse(it.hasNext());
        
        //Rest 요청시 (: 마지막 contents 번호는 프론트에서 받으면 될듯)
        //첫 Rest 요청
        Long nextRank = zSetOperations.reverseRank("kingbbode:news:current:kingbbode", "55555555") + 1;
        contents = zSetOperations.reverseRangeByScore("kingbbode:news:total:kingbbode", 0, Double.MAX_VALUE, nextRank , 2);
        it = contents.iterator();
        assertEquals(contents.size(), 2);
        assertEquals(it.next(), "44444444");
        assertEquals(it.next(), "3333333"); //is Last Contents ID
        assertFalse(it.hasNext());

        //두번째 Rest 요청
        nextRank = zSetOperations.reverseRank("kingbbode:news:current:kingbbode", "3333333") + 1;
        contents = zSetOperations.reverseRangeByScore("kingbbode:news:total:kingbbode", 0, Double.MAX_VALUE, nextRank , 2);
        it = contents.iterator();
        assertEquals(contents.size(), 2);
        assertEquals(it.next(), "2222222");
        assertEquals(it.next(), "11111111"); //is Last Contents ID
        assertFalse(it.hasNext());

        //더 이상 데이터 없을 때 요청
        nextRank = zSetOperations.reverseRank("kingbbode:news:current:kingbbode", "11111111") + 1;
        contents = zSetOperations.reverseRangeByScore("kingbbode:news:total:kingbbode", 0, Double.MAX_VALUE, nextRank , 2);
        it = contents.iterator();
        assertEquals(contents.size(), 0);
        assertFalse(it.hasNext());
    }
}