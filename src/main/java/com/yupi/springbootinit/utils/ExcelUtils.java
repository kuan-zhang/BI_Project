package com.yupi.springbootinit.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.support.ExcelTypeEnum;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ClassName: ExcelUtils
 * Package: com.yupi.springbootinit.utils
 * Description:
 *
 * @Author 张宽
 * @Create 2024/5/20 20:04
 * @Version 1.0
 */
@Slf4j
public class ExcelUtils {
    public static String excelToCsv(MultipartFile multipartFile){
//        File file = null;
//        try {
//            file = ResourceUtils.getFile("classpath:1.xlsx");
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        List<Map<Integer,String>> list = EasyExcel.read(file)
//                .excelType(ExcelTypeEnum.XLSX)
//                .sheet()
//                .headRowNumber(0)
//                .doReadSync();
//        System.out.println(list);
//        return "";
        List<Map<Integer,String>> list =null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.info("文件处理错误");
            throw new RuntimeException(e);
        }
        if(CollUtil.isEmpty(list)){return "";}
        StringBuilder stringBuilder=new StringBuilder();
        Map<Integer, String> headMap = list.get(0);
        List<String> headList = headMap.values().stream().filter(ObjectUtil::isNotEmpty).collect(Collectors.toList());
        stringBuilder.append(StringUtils.join(headList,",")).append("\n");
        for(int i=1;i<list.size();i++){
            LinkedHashMap<Integer,String> dataMap=(LinkedHashMap) list.get(i);
            List<String> dataList = dataMap.values().stream().filter(ObjectUtil::isNotEmpty).collect(Collectors.toList());
            stringBuilder.append(StringUtils.join(dataList,",")).append("\n");
        }
        System.out.println(list);
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        excelToCsv(null);
    }
}
