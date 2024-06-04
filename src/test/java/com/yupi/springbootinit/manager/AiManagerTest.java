package com.yupi.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClassName: AiManagerTest
 * Package: com.yupi.springbootinit.manager
 * Description:
 *
 * @Author 张宽
 * @Create 2024/5/20 22:37
 * @Version 1.0
 */
@SpringBootTest
class AiManagerTest {
    @Autowired
    private AiManager aiManager;

    @Test
    void dochat() {
        String answer = aiManager.dochat(1788763745376915458L,"分析需求:\n"+
                "分许网站用户的增长情况\n"+
                "原始数据:\n"+
                "日期,用户数\n"+
                "1号,10\n"+
                "2号,20\n"+
                "3号,30\n");
        System.out.println(answer);
    }
}