package com.yupi.springbootinit.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClassName: ChartMapperTest
 * Package: com.yupi.springbootinit.mapper
 * Description:
 *
 * @Author 张宽
 * @Create 2024/5/24 18:24
 * @Version 1.0
 */
@SpringBootTest
class ChartMapperTest {
    @Resource
    private ChartMapper chartMapper;
    @Test
    void test(){
        String sql="select * from chart_1793923673787289602";
        List<Map<String, Object>> list = chartMapper.queryChartData(sql);
        System.out.println(list);
    }

}