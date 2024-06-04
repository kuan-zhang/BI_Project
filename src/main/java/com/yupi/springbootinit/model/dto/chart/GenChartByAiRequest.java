package com.yupi.springbootinit.model.dto.chart;

import lombok.Data;

/**
 * ClassName: GenChartByAiRequest
 * Package: com.yupi.springbootinit.model.dto.chart
 * Description:
 *
 * @Author 张宽
 * @Create 2024/5/20 19:33
 * @Version 1.0
 */
@Data
public class GenChartByAiRequest {
    private String name;
    private String goal;
    /**
     * 图表类型
     */
    private String chartType;
    private static final long serialVersionUID = 1L;
}
