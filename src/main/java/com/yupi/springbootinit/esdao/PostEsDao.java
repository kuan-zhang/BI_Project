package com.yupi.springbootinit.esdao;

import com.yupi.springbootinit.model.dto.chart.ChartEsDTO;
import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 帖子 ES 操作
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface PostEsDao extends ElasticsearchRepository<ChartEsDTO, Long> {

    List<ChartEsDTO> findByUserId(Long userId);
}