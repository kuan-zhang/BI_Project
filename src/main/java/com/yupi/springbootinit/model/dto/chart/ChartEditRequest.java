package com.yupi.springbootinit.model.dto.chart;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * 编辑请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class ChartEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String goal;
    private String name;

    /**
     * 内容
     */
    private String chartData;

    /**
     * 标签列表
     */
    private String chartType;

    private static final long serialVersionUID = 1L;
}