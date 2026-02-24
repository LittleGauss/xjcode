package org.xj_service.oa.controller;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.dto.FlowNodeDTO;
import org.xj_service.oa.dto.ProcessDiagramDTO;
import org.xj_service.oa.dto.ProcessInstanceDTO;
import org.xj_service.oa.utils.FlowableDiagramService;
import org.xj_service.oa.utils.FlowableInspectDiagramService;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/flowable")
public class FlowableDiagramController {

    @Resource
    private FlowableDiagramService flowableDiagramService;
    @Resource
    private FlowableInspectDiagramService flowableInspectDiagramService;


    @Resource
    private RuntimeService runtimeService;

    @Resource
    private HistoryService historyService;

    @Resource
    private RepositoryService repositoryService;

    /**
     * 获取流程图和当前节点信息 png一定会生成的
     */
    @GetMapping("/diagram/{processInstanceId}")
    public Result getProcessDiagram(
            @PathVariable String processInstanceId) {
        System.out.println("前端传递的流程实例ID"+processInstanceId);
        // 确保流程实例存在
        HistoricProcessInstance instance = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (instance == null) {
            throw new RuntimeException("流程实例不存在");
        }

        // 获取流程定义
        BpmnModel bpmnModel = repositoryService.getBpmnModel(instance.getProcessDefinitionId());
        if (bpmnModel == null) {
            throw new RuntimeException("BPMN模型为空");
        }

        ProcessDiagramDTO dto = new ProcessDiagramDTO();
        dto.setProcessInstanceId(processInstanceId);

        // 获取流程图
        String diagramBase64 = flowableDiagramService
                .generatePngProcessDiagram(processInstanceId);
        dto.setDiagramImage(diagramBase64);
        System.out.println("生成的流程图"+diagramBase64);


        // 获取流程名称
        ProcessInstance processInstance = runtimeService
                .createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        if (processInstance != null) {
            dto.setProcessName(processInstance.getProcessDefinitionName());
        }

        return Result.success(dto);
    }

    /**
     * 接口1：获取高亮 SVG 流程图
     * 示例：GET /api/flow/diagram/svg/123456（123456 是流程实例ID）
     */
    @GetMapping("/diagram/svg/{processInstanceId}")
    public Result getFlowSvg(@PathVariable String processInstanceId) {
        try {
            System.out.println("前端传递的流程实例ID"+processInstanceId);
            // 确保流程实例存在
            HistoricProcessInstance instance = historyService
                    .createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();

            if (instance == null) {
                throw new RuntimeException("流程实例不存在");
            }

            // 获取流程定义
            BpmnModel bpmnModel = repositoryService.getBpmnModel(instance.getProcessDefinitionId());
            if (bpmnModel == null) {
                throw new RuntimeException("BPMN模型为空");
            }

            ProcessDiagramDTO dto = new ProcessDiagramDTO();
            dto.setProcessInstanceId(processInstanceId);
            // 获取流程图
            String svg ;
            // 由于日常监督检查有子流程 需要单独生成svg
            if(instance.getProcessDefinitionId().contains("inspectionTaskProcess")) {
                svg = flowableInspectDiagramService.generateInspectionSvg(processInstanceId);
            } else {
                svg = flowableDiagramService.generateHighlightedSvg(processInstanceId);
            }
            dto.setDiagramImage(svg);

            // 获取流程名称
            ProcessInstance processInstance = runtimeService
                    .createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            if (processInstance != null) {
                dto.setProcessName(processInstance.getProcessDefinitionName());
            }

            return Result.success(dto);
        } catch (Exception e) {
            return Result.error("生成流程图失败");
        }
    }
}