package com.yupi.springbootinit.mapper;

import com.yupi.springbootinit.model.entity.Chart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author 501ZZ
* @description 针对表【chart(图表信息表)】的数据库操作Mapper
* @createDate 2024-05-19 20:46:35
* @Entity com.yupi.springbootinit.model.entity.Chart
*/
@Mapper
public interface ChartMapper extends BaseMapper<Chart> {
    List<Map<String,Object>> queryChartData(@Param("sql") String sql);

}




