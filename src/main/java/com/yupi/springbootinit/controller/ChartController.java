package com.yupi.springbootinit.controller;
import java.util.Arrays;
import java.util.Date;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.bizmq.BiMessageProducer;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.CommonConstant;
import com.yupi.springbootinit.constant.FileConstant;
import com.yupi.springbootinit.constant.UserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.manager.AiManager;
import com.yupi.springbootinit.manager.RedisLimiterManager;
import com.yupi.springbootinit.model.dto.chart.*;
import com.yupi.springbootinit.model.dto.file.UploadFileRequest;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.enums.FileUploadBizEnum;
import com.yupi.springbootinit.model.vo.BiResponse;
import com.yupi.springbootinit.service.ChartService;


import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.yupi.springbootinit.service.UserService;
import com.yupi.springbootinit.utils.ExcelUtils;
import com.yupi.springbootinit.utils.SqlUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/chart")
@Slf4j
@CrossOrigin
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;
    @Resource
    private AiManager aiManager;
    @Resource
    private RedisLimiterManager redisLimiterManager;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private BiMessageProducer biMessageProducer;

    // region 增删改查


    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                             GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR,"目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length()>100, ErrorCode.PARAMS_ERROR,"目标过长");
        //校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        final long ONE_MB=1024*1024L;
        ThrowUtils.throwIf(size>ONE_MB,ErrorCode.PARAMS_ERROR,"文件超过1M");
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffix= Arrays.asList("xlsx","xls");
        ThrowUtils.throwIf(validFileSuffix.contains(suffix), ErrorCode.PARAMS_ERROR,"文件后缀非法");



        User loginUser = userService.getLoginUser(request);
        redisLimiterManager.doRateLimit("genChartByAi_"+loginUser.getId());
//        final String prompt="你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容:\n"+
//                "分析需求:\n"+
//                "{数据分析的需求或者目标}\n"+
//                "原始数据:\n"+
//                "{csv格式的原始数据，用,作为分隔符}\n"+
//                "请根据这两部分内容，按照以下指定格式生成内容(此外不要输出任何多余的开头、结尾、注释)\n"+
//                "【【【【【\n"+
//                "{前端 Echarts V5 的 option配置对象js代码，合理地将数据进行可视化，不要生成任何多余地内容，比如注释}\n"+
//                "【【【【【\n"+
//                "{明确的数据分析结论、越详细越好、不要生成多余地注释}";
        //用户输入
        long biModelId=1766393070611812354L;
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("分析需求:").append("\n");
        String userGoal=goal;
        if(StringUtils.isNotBlank(chartType)){
            userGoal+=",请使用"+chartType;
        }
        stringBuilder.append(userGoal).append("\n");
        stringBuilder.append("原始数据:").append("\n");
        String result= ExcelUtils.excelToCsv(multipartFile);
        stringBuilder.append(result).append("\n");
        Chart chart=new Chart();
        chart.setUserId(loginUser.getId());
        String res = aiManager.dochat(biModelId, stringBuilder.toString());
        String[] split = res.split("【【【【【");
        if(split.length<3){
            handleChartUpdateError(chart.getId(), "AI 生成错误");
        }
        String genChart=split[1].trim();
        String genResult=split[2].trim();

        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(result);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chartService.save(chart);

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return  ResultUtils.success(biResponse);
    }
    @PostMapping("/gen/async")
    public BaseResponse<BiResponse> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR,"目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length()>100, ErrorCode.PARAMS_ERROR,"目标过长");
        //校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        final long ONE_MB=1024*1024L;
        ThrowUtils.throwIf(size>ONE_MB,ErrorCode.PARAMS_ERROR,"文件超过1M");
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffix= Arrays.asList("xlsx","xls");
        ThrowUtils.throwIf(!validFileSuffix.contains(suffix), ErrorCode.PARAMS_ERROR,"文件后缀非法");



        User loginUser = userService.getLoginUser(request);
        redisLimiterManager.doRateLimit("genChartByAi_"+loginUser.getId());
//        final String prompt="你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容:\n"+
//                "分析需求:\n"+
//                "{数据分析的需求或者目标}\n"+
//                "原始数据:\n"+
//                "{csv格式的原始数据，用,作为分隔符}\n"+
//                "请根据这两部分内容，按照以下指定格式生成内容(此外不要输出任何多余的开头、结尾、注释)\n"+
//                "【【【【【\n"+
//                "{前端 Echarts V5 的 option配置对象js代码，合理地将数据进行可视化，不要生成任何多余地内容，比如注释}\n"+
//                "【【【【【\n"+
//                "{明确的数据分析结论、越详细越好、不要生成多余地注释}";
        //用户输入
        long biModelId=1766393070611812354L;
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("分析需求:").append("\n");
        String userGoal=goal;
        if(StringUtils.isNotBlank(chartType)){
            userGoal+=",请使用"+chartType;
        }
        stringBuilder.append(userGoal).append("\n");
        stringBuilder.append("原始数据:").append("\n");
        String result= ExcelUtils.excelToCsv(multipartFile);
        stringBuilder.append(result).append("\n");

        Chart chart=new Chart();
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(result);
        chart.setChartType(chartType);
        chart.setStatus("wait");

        chart.setUserId(loginUser.getId());
        boolean save = chartService.save(chart);
        ThrowUtils.throwIf(!save,ErrorCode.SYSTEM_ERROR,"图表保存失败");

        CompletableFuture.runAsync(()->{
            //执行中
            Chart updateChart=new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus("running");
            boolean b = chartService.updateById(updateChart);
            if(!b){
                handleChartUpdateError(chart.getId(), "图表状态更改失败");
                return;
            }
            String res = aiManager.dochat(biModelId, stringBuilder.toString());
            String[] split = res.split("【【【【【");
            if(split.length<3){
                handleChartUpdateError(chart.getId(), "AI 生成错误");
                return;
            }
            String genChart=split[1].trim();
            String genResult=split[2].trim();
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setGenResult(genResult);
            updateChartResult.setStatus("succeed");
            boolean updateResult = chartService.updateById(updateChartResult);
            if(!updateResult){
                handleChartUpdateError(chart.getId(),"图表状态更新失败");
                return;
            }
        },threadPoolExecutor);

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return  ResultUtils.success(biResponse);
    }
    private void handleChartUpdateError(long chartId,String execMessage){
        Chart chart = new Chart();
        chart.setId(chart.getId());
        chart.setStatus("failed");
        chart.setExecMessage("execMessage");
        boolean b = chartService.updateById(chart);
        if(!b){
            log.error("更新图表失败状态失败"+","+execMessage);
        }
    }
    @PostMapping("/gen/async/mq")
    public BaseResponse<BiResponse> genChartByAiAsyncmq(@RequestPart("file") MultipartFile multipartFile,
                                                      GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR,"目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length()>100, ErrorCode.PARAMS_ERROR,"目标过长");
        //校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        final long ONE_MB=1024*1024L;
        ThrowUtils.throwIf(size>ONE_MB,ErrorCode.PARAMS_ERROR,"文件超过1M");
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffix= Arrays.asList("xlsx","xls");
        ThrowUtils.throwIf(!validFileSuffix.contains(suffix), ErrorCode.PARAMS_ERROR,"文件后缀非法");



        User loginUser = userService.getLoginUser(request);
        redisLimiterManager.doRateLimit("genChartByAi_"+loginUser.getId());
//        final String prompt="你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容:\n"+
//                "分析需求:\n"+
//                "{数据分析的需求或者目标}\n"+
//                "原始数据:\n"+
//                "{csv格式的原始数据，用,作为分隔符}\n"+
//                "请根据这两部分内容，按照以下指定格式生成内容(此外不要输出任何多余的开头、结尾、注释)\n"+
//                "【【【【【\n"+
//                "{前端 Echarts V5 的 option配置对象js代码，合理地将数据进行可视化，不要生成任何多余地内容，比如注释}\n"+
//                "【【【【【\n"+
//                "{明确的数据分析结论、越详细越好、不要生成多余地注释}";


        String result= ExcelUtils.excelToCsv(multipartFile);
        Chart chart=new Chart();
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(result);
        chart.setChartType(chartType);
        chart.setStatus("wait");

        chart.setUserId(loginUser.getId());
        boolean save = chartService.save(chart);
        ThrowUtils.throwIf(!save,ErrorCode.SYSTEM_ERROR,"图表保存失败");

       long newChartId=chart.getId();

        biMessageProducer.sendMessage(String.valueOf(newChartId));

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return  ResultUtils.success(biResponse);
    }

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addchart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newchartId = chart.getId();
        return ResultUtils.success(newchartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletechart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldchart = chartService.getById(id);
        ThrowUtils.throwIf(oldchart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldchart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatechart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        List<String> tags = chartUpdateRequest.getTags();
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldchart = chartService.getById(id);
        ThrowUtils.throwIf(oldchart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getchartVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param chartQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Chart>> listchartByPage(@RequestBody ChartQueryRequest chartQueryRequest) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMychartVOByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion

    /**
     * 分页搜索（从 ES 查询，封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
 
    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editchart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
      
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldchart = chartService.getById(id);
        ThrowUtils.throwIf(oldchart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldchart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
       

        Long id = chartQueryRequest.getId();
        String  goal = chartQueryRequest.getGoal();
        String  name = chartQueryRequest.getName();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        queryWrapper.eq(id!=null && id > 0,"id",id);
        queryWrapper.eq(StringUtils.isNotBlank(name),"name",name);
        queryWrapper.eq(StringUtils.isNotBlank(goal),"goal",goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType),"chartType",chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


}
