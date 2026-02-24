package org.xj_service.oa.config;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.image.impl.DefaultProcessDiagramGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlowableConfig {

    /**
     * 配置流程图生成器
     * 注意：DefaultProcessDiagramGenerator是ProcessDiagramGenerator的唯一实现类
     */
    @Bean
    public ProcessDiagramGenerator processDiagramGenerator() {
        return new DefaultProcessDiagramGenerator() ;
    }
}
