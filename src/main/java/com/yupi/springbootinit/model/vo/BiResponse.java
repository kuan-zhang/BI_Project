package com.yupi.springbootinit.model.vo;

import lombok.Data;

/**
 * ClassName: BiResponse
 * Package: com.yupi.springbootinit.model.vo
 * Description:
 *
 * @Author 张宽
 * @Create 2024/5/20 23:02
 * @Version 1.0
 */
@Data
public class BiResponse {
    private String genChart;
    private String genResult;
    private Long chartId;
}
