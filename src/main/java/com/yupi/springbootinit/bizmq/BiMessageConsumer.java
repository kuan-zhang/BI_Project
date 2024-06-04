package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.utils.ExcelUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * ClassName: BiConsumer
 * Package: com.yupi.springbootinit.bizmq
 * Description:
 *
 * @Author 张宽
 * @Create 2024/5/26 16:27
 * @Version 1.0
 */
@Component
@Slf4j
public class BiMessageConsumer {
    @Resource
    private ChartService chartService;
    @Resource
    private AiManager aiManager;

    @SneakyThrows
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        if (StringUtils.isBlank(message)) {
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        long chartId = Long.parseLong(message);
        Chart chart=chartService.getById(chartId);
        if(chart==null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"图表为空");
        }
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus("running");
        boolean b = chartService.updateById(updateChart);
        if (!b) {
            handleChartUpdateError(chart.getId(), "图表状态更改失败");
            return;
        }
        String res = aiManager.dochat(CommonConstant.BI_MODEL_ID, buildUserInput(chart));
        String[] split = res.split("【【【【【");
        if (split.length < 3) {
            channel.basicNack(deliveryTag,false,false);
            handleChartUpdateError(chart.getId(), "AI 生成错误");
            return;
        }
        String genChart = split[1].trim();
        String genResult = split[2].trim();
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setGenChart(genChart);
        updateChartResult.setGenResult(genResult);
        updateChartResult.setStatus("succeed");
        boolean updateResult = chartService.updateById(updateChartResult);
        if (!updateResult) {
            channel.basicNack(deliveryTag,false,false);
            handleChartUpdateError(chart.getId(), "图表状态更新失败");
            return;
        }
        log.info("receiveMessage message={}", message);
        channel.basicAck(deliveryTag, false);
    }

    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart chart = new Chart();
        chart.setId(chart.getId());
        chart.setStatus("failed");
        chart.setExecMessage("execMessage");
        boolean b = chartService.updateById(chart);
        if (!b) {
            log.error("更新图表失败状态失败" + "," + execMessage);
        }
    }
        private String buildUserInput (Chart chart){
            String goal = chart.getGoal();
            String chartType = chart.getChartType();
            String csvData = chart.getChartData();
            //用户输入
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("分析需求:").append("\n");
            String userGoal = goal;
            if (StringUtils.isNotBlank(chartType)) {
                userGoal += ",请使用" + chartType;
            }
            stringBuilder.append(userGoal).append("\n");
            stringBuilder.append("原始数据:").append("\n");
            stringBuilder.append(csvData).append("\n");
            return String.valueOf(stringBuilder);
        }
    }
