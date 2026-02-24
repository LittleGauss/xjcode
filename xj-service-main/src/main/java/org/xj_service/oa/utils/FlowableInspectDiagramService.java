package org.xj_service.oa.utils;

import org.apache.commons.io.IOUtils;
import org.flowable.bpmn.model.*;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class FlowableInspectDiagramService {
    @Resource
    private RepositoryService repositoryService;

    @Resource
    private RuntimeService runtimeService;

    @Autowired
    private ProcessEngineConfiguration processEngineConfiguration;

    @Autowired
    private ProcessEngine processEngine;

    @Resource
    private HistoryService historyService;

    /**
     * 生成高亮当前节点的 SVG 流程图
     * @param processInstanceId 流程实例ID（前端传递）
     * @return SVG 字符串（前端可直接渲染）
     */
    public String generateInspectionSvg(String processInstanceId) {
        try {
            // 1. 校验流程实例是否存在
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            if (processInstance == null) {
                // 补充：检查历史流程实例（已结束的流程）
                HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(processInstanceId)
                        .singleResult();
                if (historicInstance == null) {
                    throw new RuntimeException("流程实例不存在：" + processInstanceId);
                }
                processInstance = runtimeService.createProcessInstanceQuery()
                        .processDefinitionId(historicInstance.getProcessDefinitionId())
                        .processInstanceId(processInstanceId)
                        .singleResult(); // 兼容已结束流程
            }

            // 2. 获取 BPMN 模型（流程图的元数据）
            String processDefinitionId = processInstance.getProcessDefinitionId();
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
            if (bpmnModel == null || bpmnModel.getMainProcess() == null) {
                throw new RuntimeException("BPMN 模型为空或不完整：" + processDefinitionId);
            }
            // 3. 校验流程定义是否有效（是否部署成功）
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(processDefinitionId)
                    .singleResult();
            if (processDefinition == null || processDefinition.isSuspended()) {
                throw new RuntimeException("流程定义未部署或已禁用：" + processDefinitionId);
            }

            // 4. 处理活跃节点 ID（兼容已结束流程）
            List<String> activeActivityIds = Collections.emptyList();
            try {
                activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
            } catch (Exception e) {
                // 流程已结束时，getActiveActivityIds 会报错，手动获取已完成节点
                List<HistoricActivityInstance> historicActivities = historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(processInstanceId)
                        .finished()
                        .orderByHistoricActivityInstanceEndTime().desc()
                        .list();
                if (!historicActivities.isEmpty()) {
                    activeActivityIds = Collections.singletonList(historicActivities.get(0).getActivityId());
                }
                System.out.println("流程实例已结束，使用最后完成节点作为高亮：{}"+processInstanceId);
            }

            // 5. 校验活跃节点 ID 是否存在于 BPMN 模型中
            if (!activeActivityIds.isEmpty()) {
                for (String activityId : activeActivityIds) {
                    FlowElement flowElement = findFlowElementRecursive(bpmnModel.getMainProcess(), activityId);
                    if (flowElement == null) {
                        throw new RuntimeException("BPMN 模型中不存在节点 ID：" + activityId);
                    }
                }
            }

            // 4. 初始化流程图生成器（Flowable 自带）
            ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator() ;

            // 5. 生成 SVG 流（关键：传入活跃节点 ID 实现高亮）
            InputStream svgStream = diagramGenerator.generateDiagram(
                    bpmnModel,                // BPMN 模型
                    "svg",                    // 输出格式（SVG 矢量图）
                    activeActivityIds,        // 要高亮的活跃节点 ID
                    Collections.emptyList(),
                    "zh-CN",                  // 语言（中文）
                    "Microsoft YaHei",        // 字体（解决中文乱码）
                    "Microsoft YaHei",        // 注解字体
                    null,                     // ClassLoader（无需指定）
                    1.0,                      // 缩放比例
                    true                      // 是否高亮活跃节点（必须为 true）
            );

            // 6. 流转 SVG 字符串（前端直接渲染）
            byte[] svgBytes = IOUtils.toByteArray(svgStream);
            svgStream.close();
            if(svgBytes == null || svgBytes.length == 0){
                return generateFallbackDiagram(bpmnModel, activeActivityIds);
            }
            return new String(svgBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("生成流程图失败：" + e.getMessage(), e);
        }
    }

    /**
     * 【核心递归方法】查找节点（兼容Process/SubProcess，无接口依赖）
     * @param parent 父容器（主流程Process / 子流程SubProcess）
     * @param activityId 要查找的节点ID
     * @return 找到的节点（null=不存在）
     */
    private FlowElement findFlowElementRecursive(Object parent, String activityId) {
        // 1. 处理主流程（Process）
        if (parent instanceof org.flowable.bpmn.model.Process) {
            org.flowable.bpmn.model.Process process = (org.flowable.bpmn.model.Process) parent;
            // 先查当前流程的直接节点
            FlowElement flowElement = process.getFlowElement(activityId);
            if (flowElement != null) {
                return flowElement;
            }
            // 遍历流程内的所有元素，递归查找子流程
            for (FlowElement element : process.getFlowElements()) {
                FlowElement found = findFlowElementRecursive(element, activityId);
                if (found != null) {
                    return found;
                }
            }
        }

        // 2. 处理子流程（SubProcess）
        else if (parent instanceof SubProcess) {
            SubProcess subProcess = (SubProcess) parent;
            // 先查当前子流程的直接节点
            FlowElement flowElement = subProcess.getFlowElement(activityId);
            if (flowElement != null) {
                return flowElement;
            }
            // 遍历子流程内的所有元素，递归查找嵌套子流程
            for (FlowElement element : subProcess.getFlowElements()) {
                FlowElement found = findFlowElementRecursive(element, activityId);
                if (found != null) {
                    return found;
                }
            }
        }

        // 3. 非容器类型（如UserTask/Gateway），直接返回null
        return null;
    }

    private String generateFallbackDiagram(BpmnModel bpmnModel, List<String> activeActivityIds) {
        try {
            org.flowable.bpmn.model.Process process = bpmnModel.getProcesses().get(0);
            Map<String, GraphicInfo> locationMap = bpmnModel.getLocationMap();
            Map<String, List<GraphicInfo>> flowLocationMap = bpmnModel.getFlowLocationMap();

            // 🟢 原参数 → 修改后参数（缩小整体比例、减少节点额外空间、增加间距）
            final double LAYOUT_SCALE_FACTOR = 0.7; // 原1.0 → 缩小整体元素比例
            final double NODE_WIDTH_PADDING = 15.0; // 原25.0 → 减少节点额外宽度
            final double NODE_HEIGHT_PADDING = 5.0;  // 原10.0 → 减少节点额外高度
            final double MARGIN = 30.0;              // 原40.0 → 减少边距
            final double NODE_SPACING = 80.0;        // 原60.0 → 增加节点/子流程之间的间距
            final double TEXT_FONT_SIZE = 10.0;      // 原12.0 → 缩小文字尺寸（避免子流程内文字撑大容器）

            // 1. 收集所有需要绘制的元素（包括子流程内部元素）
            List<FlowElement> allElements = new ArrayList<>();
            Map<String, GraphicInfo> allLocationMap = new HashMap<>();
            Map<String, List<GraphicInfo>> allFlowLocationMap = new HashMap<>();

            // 收集顶层流程元素
            collectFlowElements(process, locationMap, flowLocationMap,
                    allElements, allLocationMap, allFlowLocationMap);

            // 如果没有收集到任何元素，返回错误
            if (allElements.isEmpty()) {
                return getErrorSvg("未找到任何流程元素");
            }

            // 2. 计算边界
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double maxY = Double.MIN_VALUE;

            Map<String, Double[]> nodeFinalPositions = new HashMap<>();

            for (FlowElement element : allElements) {
                if (element instanceof FlowNode) {
                    GraphicInfo gi = allLocationMap.get(element.getId());
                    if (gi != null) {
                        double scaledX = gi.getX() * LAYOUT_SCALE_FACTOR;
                        double scaledY = gi.getY() * LAYOUT_SCALE_FACTOR;
                        double scaledWidth = Math.max(gi.getWidth() * 0.8, 80);
                        double scaledHeight = Math.max(gi.getHeight() * 0.8, 40);

                        double adjustedX = scaledX;
                        double adjustedY = scaledY;

                        // 检查并调整节点位置，避免重叠
                        for (Map.Entry<String, Double[]> existingNode : nodeFinalPositions.entrySet()) {
                            Double[] pos = existingNode.getValue();
                            double existingX = pos[0];
                            double existingY = pos[1];
                            double existingWidth = pos[2];
                            double existingHeight = pos[3];

                            if (Math.abs(scaledX - existingX) < NODE_SPACING &&
                                    Math.abs(scaledY - existingY) < NODE_SPACING) {
                                adjustedX = existingX + existingWidth + NODE_SPACING;
                            }
                        }

                        nodeFinalPositions.put(element.getId(),
                                new Double[]{adjustedX, adjustedY, scaledWidth, scaledHeight});

                        minX = Math.min(minX, adjustedX);
                        minY = Math.min(minY, adjustedY);
                        maxX = Math.max(maxX, adjustedX + scaledWidth);
                        maxY = Math.max(maxY, adjustedY + scaledHeight);
                    }
                } else if (element instanceof SequenceFlow) {
                    List<GraphicInfo> waypoints = allFlowLocationMap.get(element.getId());
                    if (waypoints != null) {
                        for (GraphicInfo gi : waypoints) {
                            double scaledX = gi.getX() * LAYOUT_SCALE_FACTOR;
                            double scaledY = gi.getY() * LAYOUT_SCALE_FACTOR;
                            minX = Math.min(minX, scaledX);
                            minY = Math.min(minY, scaledY);
                            maxX = Math.max(maxX, scaledX);
                            maxY = Math.max(maxY, scaledY);
                        }
                    }
                }
            }

            if (minX == Double.MAX_VALUE) {
                return getErrorSvg("未找到流程元素");
            }

            // 计算全局偏移量
            double globalOffsetX = MARGIN - minX;
            double globalOffsetY = MARGIN - minY;

            // 计算ViewBox尺寸
            double viewBoxWidth = (maxX - minX) + 2 * MARGIN;
            double viewBoxHeight = (maxY - minY) + 2 * MARGIN;

            // 3. 绘制SVG头部
            StringBuilder svg = new StringBuilder();
            svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
            svg.append(String.format(
                    "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100%%\" height=\"100%%\" " +
                            "viewBox=\"0 0 %.1f %.1f\">\n",
                    viewBoxWidth, viewBoxHeight
            ));

            svg.append("<defs>\n");
            svg.append("<style type=\"text/css\">\n");
            svg.append("    text {\n");
            svg.append("        font-family: 'Microsoft YaHei', 'SimSun', 'PingFang SC', sans-serif;\n");
            svg.append("        font-weight: normal;\n");
            svg.append("        fill: #333333;\n");
            svg.append("    }\n");
            svg.append("    .node-rect {\n");
            svg.append("        stroke-width: 2;\n");
            svg.append("        rx: 6;\n");
            svg.append("        ry: 6;\n");
            svg.append("    }\n");
            svg.append("    .subprocess-rect {\n");
            svg.append("        stroke-width: 3;\n");
            svg.append("        rx: 8;\n");
            svg.append("        ry: 8;\n");
            svg.append("        stroke-dasharray: 8, 4;\n"); // 虚线边框表示子流程
            svg.append("    }\n");
            svg.append("    .active-node {\n");
            svg.append("        fill: #FF9800 !important;\n");
            svg.append("        stroke: #F57C00 !important;\n");
            svg.append("        stroke-width: 3 !important;\n");
            svg.append("        filter: drop-shadow(0 0 8px rgba(255, 152, 0, 0.5));\n");
            svg.append("        animation: pulse 1.5s infinite alternate;\n");
            svg.append("    }\n");
            svg.append("    @keyframes pulse {\n");
            svg.append("        from { opacity: 0.9; }\n");
            svg.append("        to { opacity: 1; }\n");
            svg.append("    }\n");
            svg.append("    .node-text {\n");
            svg.append("        text-anchor: middle;\n");
            svg.append("        dominant-baseline: middle;\n");
            svg.append("        pointer-events: none;\n");
            svg.append("        font-weight: 500;\n");
            svg.append("    }\n");
            svg.append("    .subprocess-text {\n");
            svg.append("        font-weight: bold;\n");
            svg.append("        font-size: 13px;\n");
            svg.append("    }\n");
            svg.append("</style>\n");

            // 箭头定义
            svg.append("<marker id=\"arrow\" markerWidth=\"8\" markerHeight=\"8\" " +
                    "refX=\"7\" refY=\"3\" orient=\"auto\" markerUnits=\"strokeWidth\">\n");
            svg.append("<path d=\"M0,0 L0,6 L9,3 z\" fill=\"#666666\" />\n");
            svg.append("</marker>\n");
            svg.append("</defs>\n");

            // 背景
            svg.append("<rect x=\"0\" y=\"0\" width=\"100%\" height=\"100%\" fill=\"#f5f7fa\"/>\n");

            // 4. 绘制连线（放在节点下面）
            List<FlowElement> normalNodes = new ArrayList<>(); // 存储普通节点
            for (FlowElement element : allElements) {
                if (!(element instanceof FlowNode)) continue;
                // 先筛选并绘制所有子流程
                if (element instanceof SubProcess) {
                    drawSubProcess(element, allLocationMap, nodeFinalPositions, activeActivityIds,
                            globalOffsetX, globalOffsetY, NODE_WIDTH_PADDING, NODE_HEIGHT_PADDING,
                            TEXT_FONT_SIZE, svg);
                } else {
                    normalNodes.add(element); // 普通节点暂存
                }
            }
            for (SequenceFlow flow : getAllSequenceFlows(process)) {
                List<GraphicInfo> waypoints = allFlowLocationMap.get(flow.getId());
                if (waypoints == null || waypoints.size() < 2) continue;

                StringBuilder points = new StringBuilder();
                for (GraphicInfo gi : waypoints) {
                    double finalX = gi.getX() * LAYOUT_SCALE_FACTOR + globalOffsetX;
                    double finalY = gi.getY() * LAYOUT_SCALE_FACTOR + globalOffsetY;
                    points.append(finalX).append(",").append(finalY).append(" ");
                }

                svg.append(String.format(
                        "<polyline points=\"%s\" style=\"fill:none;stroke:#666666;stroke-width:1.5\" " +
                                "marker-end=\"url(#arrow)\"/>\n",
                        points.toString().trim()
                ));
            }


            // 🟢 再绘制普通节点（显示在子流程上方）
            for (FlowElement element : normalNodes) {
                drawFlowNode(element, allLocationMap, nodeFinalPositions, activeActivityIds,
                        globalOffsetX, globalOffsetY, NODE_WIDTH_PADDING, NODE_HEIGHT_PADDING,
                        TEXT_FONT_SIZE, svg);
            }

            svg.append("</svg>");
            System.out.println("优化版流程图生成成功，长度: " + svg.length() + " 字符");
            return svg.toString();

        } catch (Exception e) {
            System.err.println("流程图生成失败: " + e.getMessage());
            e.printStackTrace();
            return getErrorSvg("生成流程图失败: " + e.getMessage());
        }
    }

    // 辅助方法：收集所有流元素（递归处理子流程）
    private void collectFlowElements(org.flowable.bpmn.model.Process process,
                                     Map<String, GraphicInfo> locationMap,
                                     Map<String, List<GraphicInfo>> flowLocationMap,
                                     List<FlowElement> allElements,
                                     Map<String, GraphicInfo> allLocationMap,
                                     Map<String, List<GraphicInfo>> allFlowLocationMap) {

        // 收集当前流程的所有元素
        for (FlowElement element : process.getFlowElements()) {
            allElements.add(element);

            // 收集位置信息
            if (locationMap.containsKey(element.getId())) {
                allLocationMap.put(element.getId(), locationMap.get(element.getId()));
            }

            // 如果是子流程，递归收集内部元素
            if (element instanceof SubProcess) {
                SubProcess subProcess = (SubProcess) element;
                collectFlowElements(subProcess, locationMap, flowLocationMap,
                        allElements, allLocationMap, allFlowLocationMap);
            }
        }

        // 🟢 替换：移除findFlowElementsOfType，手动遍历筛选SequenceFlow
        List<SequenceFlow> processFlows = new ArrayList<>();
        for (FlowElement element : process.getFlowElements()) {
            if (element instanceof SequenceFlow) {
                processFlows.add((SequenceFlow) element);
            }
        }
        // 收集所有连线
        for (SequenceFlow flow : processFlows) {
            if (flowLocationMap.containsKey(flow.getId())) {
                allFlowLocationMap.put(flow.getId(), flowLocationMap.get(flow.getId()));
            }
        }
    }

    // 辅助方法：收集子流程内部元素
    private void collectFlowElements(SubProcess subProcess,
                                     Map<String, GraphicInfo> locationMap,
                                     Map<String, List<GraphicInfo>> flowLocationMap,
                                     List<FlowElement> allElements,
                                     Map<String, GraphicInfo> allLocationMap,
                                     Map<String, List<GraphicInfo>> allFlowLocationMap) {

        for (FlowElement element : subProcess.getFlowElements()) {
            allElements.add(element);

            if (locationMap.containsKey(element.getId())) {
                allLocationMap.put(element.getId(), locationMap.get(element.getId()));
            }

            // 递归处理嵌套子流程
            if (element instanceof SubProcess) {
                collectFlowElements((SubProcess) element, locationMap, flowLocationMap,
                        allElements, allLocationMap, allFlowLocationMap);
            }
        }

        // 🟢 替换：移除findFlowElementsOfType，手动遍历筛选SequenceFlow
        List<SequenceFlow> subProcessFlows = new ArrayList<>();
        for (FlowElement element : subProcess.getFlowElements()) {
            if (element instanceof SequenceFlow) {
                subProcessFlows.add((SequenceFlow) element);
            }
        }
        // 收集子流程内部的连线
        for (SequenceFlow flow : subProcessFlows) {
            if (flowLocationMap.containsKey(flow.getId())) {
                allFlowLocationMap.put(flow.getId(), flowLocationMap.get(flow.getId()));
            }
        }
    }

    // 辅助方法：获取所有序列流
    private List<SequenceFlow> getAllSequenceFlows(org.flowable.bpmn.model.Process process) {
        List<SequenceFlow> allFlows = new ArrayList<>();
        collectSequenceFlows(process, allFlows);
        return allFlows;
    }

    // 用工具方法 - 手动遍历筛选指定类型的FlowElement（替代findFlowElementsOfType）
    private <T extends FlowElement> List<T> findFlowElementsByType(FlowElementsContainer container, Class<T> type) {
        List<T> result = new ArrayList<>();
        for (FlowElement element : container.getFlowElements()) {
            if (type.isInstance(element)) {
                result.add(type.cast(element));
            }
            // 递归处理子流程
            if (element instanceof SubProcess) {
                result.addAll(findFlowElementsByType((SubProcess) element, type));
            }
        }
        return result;
    }

    // 🟢 替换：重写collectSequenceFlows，使用自定义工具方法替代findFlowElementsOfType
    private void collectSequenceFlows(FlowElementsContainer container, List<SequenceFlow> allFlows) {
        // 调用自定义工具方法筛选SequenceFlow
        allFlows.addAll(findFlowElementsByType(container, SequenceFlow.class));

        // 递归收集子流程中的序列流
        for (FlowElement element : container.getFlowElements()) {
            if (element instanceof SubProcess) {
                collectSequenceFlows((SubProcess) element, allFlows);
            }
        }
    }

    // 辅助方法：绘制普通流程节点
    private void drawFlowNode(FlowElement element, Map<String, GraphicInfo> locationMap,
                              Map<String, Double[]> nodeFinalPositions, List<String> activeActivityIds,
                              double globalOffsetX, double globalOffsetY,
                              double nodeWidthPadding, double nodeHeightPadding,
                              double textFontSize, StringBuilder svg) {

        GraphicInfo gi = locationMap.get(element.getId());
        if (gi == null) return;

        String elementId = element.getId();
        String elementName = element.getName() != null ? element.getName() : element.getId();

        Double[] nodeData = nodeFinalPositions.get(elementId);
        if (nodeData == null) return;

        double finalX = nodeData[0] + globalOffsetX;
        double finalY = nodeData[1] + globalOffsetY;
        double adjustedWidth = nodeData[2] + nodeWidthPadding;
        double adjustedHeight = nodeData[3] + nodeHeightPadding;

        // 设置节点颜色
        String fillColor = "#F8F9FA";
        String strokeColor = "#DEE2E6";
        double strokeWidth = 2.0;

        if (element instanceof StartEvent) {
            fillColor = "#E8F5E9";
            strokeColor = "#81C784";
        } else if (element instanceof EndEvent) {
            fillColor = "#FFEBEE";
            strokeColor = "#E57373";
        } else if (element instanceof UserTask) {
            fillColor = "#E3F2FD";
            strokeColor = "#64B5F6";
        } else if (element instanceof ExclusiveGateway) {
            fillColor = "#F3E5F5";
            strokeColor = "#BA68C8";
            adjustedWidth = Math.min(adjustedWidth, adjustedHeight);
            adjustedHeight = adjustedWidth;
        } else if (element instanceof ServiceTask) {
            fillColor = "#E0F2F1";
            strokeColor = "#4DB6AC";
        } else if (element instanceof CallActivity) {
            fillColor = "#FFF3E0";
            strokeColor = "#FFB74D";
        }

        boolean isActive = activeActivityIds.contains(elementId);
        if (isActive) {
            fillColor = "#FFF3E0";
            strokeColor = "#FF9800";
            strokeWidth = 3.0;
        }

        String rectClass = "node-rect" + (isActive ? " active-node" : "");

        // 绘制节点形状
        if (element instanceof ExclusiveGateway) {
            double centerX = finalX + adjustedWidth / 2;
            double centerY = finalY + adjustedHeight / 2;
            double halfWidth = adjustedWidth / 2;
            double halfHeight = adjustedHeight / 2;

            svg.append(String.format(
                    "<polygon points=\"%.1f,%.1f %.1f,%.1f %.1f,%.1f %.1f,%.1f\" " +
                            "fill=\"%s\" stroke=\"%s\" stroke-width=\"%.1f\" class=\"%s\"/>\n",
                    centerX, finalY,
                    finalX + adjustedWidth, centerY,
                    centerX, finalY + adjustedHeight,
                    finalX, centerY,
                    fillColor, strokeColor, strokeWidth, rectClass
            ));
        } else {
            svg.append(String.format(
                    "<rect x=\"%.1f\" y=\"%.1f\" width=\"%.1f\" height=\"%.1f\" " +
                            "class=\"%s\" fill=\"%s\" stroke=\"%s\" stroke-width=\"%.1f\"/>\n",
                    finalX, finalY, adjustedWidth, adjustedHeight,
                    rectClass, fillColor, strokeColor, strokeWidth
            ));
        }

        // 绘制文本
        drawText(elementName, finalX, finalY, adjustedWidth, adjustedHeight, textFontSize, svg, false);
    }

    // 辅助方法：绘制子流程
    private void drawSubProcess(FlowElement element, Map<String, GraphicInfo> locationMap,
                                Map<String, Double[]> nodeFinalPositions, List<String> activeActivityIds,
                                double globalOffsetX, double globalOffsetY,
                                double nodeWidthPadding, double nodeHeightPadding,
                                double textFontSize, StringBuilder svg) {

        GraphicInfo gi = locationMap.get(element.getId());
        if (gi == null) return;

        String elementId = element.getId();
        String elementName = element.getName() != null ? element.getName() : element.getId();

        Double[] nodeData = nodeFinalPositions.get(elementId);
        if (nodeData == null) return;

        double finalX = nodeData[0] + globalOffsetX;
        double finalY = nodeData[1] + globalOffsetY;
        double adjustedWidth = (nodeData[2] + nodeWidthPadding)*0.9;
        double adjustedHeight = (nodeData[3] + nodeHeightPadding)*0.9;

        // 🟢 子流程背景改为半透明（用rgba设置透明度，0.3=30%不透明）
        String fillColor = "rgba(255, 243, 224, 0.3)";  // 原#FFF3E0 → 半透明浅橙色
        String strokeColor = "#FFB74D"; // 边框保持原颜色（清晰区分子流程范围）
        double strokeWidth = 3.0;

        boolean isActive = activeActivityIds.contains(elementId);
        if (isActive) {
            fillColor = "rgba(255, 224, 178, 0.4)"; // 原#FFE0B2 → 半透明深一点的橙色
            strokeColor = "#FF9800";
            strokeWidth = 4.0;
        }

        String rectClass = "subprocess-rect" + (isActive ? " active-node" : "");

        // 绘制子流程矩形（虚线边框）
        svg.append(String.format(
                "<rect x=\"%.1f\" y=\"%.1f\" width=\"%.1f\" height=\"%.1f\" " +
                        "class=\"%s\" fill=\"%s\" stroke=\"%s\" stroke-width=\"%.1f\"/>\n",
                finalX, finalY, adjustedWidth, adjustedHeight,
                rectClass, fillColor, strokeColor, strokeWidth
        ));

        // 绘制子流程文本
        drawText(elementName, finalX, finalY, adjustedWidth, adjustedHeight, textFontSize + 1, svg, true);
    }

    // 辅助方法：绘制文本
    private void drawText(String text, double x, double y, double width, double height,
                          double fontSize, StringBuilder svg, boolean isSubProcess) {

        String displayText = text;
        int maxCharsPerLine = (int)(width / (fontSize * 0.6));

        if (displayText.length() > maxCharsPerLine && maxCharsPerLine > 0) {
            List<String> lines = new ArrayList<>();
            StringBuilder currentLine = new StringBuilder();

            for (char c : displayText.toCharArray()) {
                currentLine.append(c);
                if (currentLine.length() >= maxCharsPerLine) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
            }
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }

            int maxLines = Math.min(3, lines.size());
            double lineHeight = fontSize * 1.2;
            double textStartY = y + height/2 - ((maxLines-1) * lineHeight)/2;

            String textClass = isSubProcess ? "subprocess-text" : "node-text";

            for (int i = 0; i < maxLines; i++) {
                String line = lines.get(i);
                if (i == maxLines - 1 && lines.size() > maxLines) {
                    line = line.substring(0, Math.max(0, line.length()-3)) + "...";
                }

                svg.append(String.format(
                        "<text x=\"%.1f\" y=\"%.1f\" class=\"%s\" " +
                                "font-size=\"%.1f\" fill=\"#333\">%s</text>\n",
                        x + width/2,
                        textStartY + (i * lineHeight),
                        textClass,
                        fontSize,
                        escapeXml(line)
                ));
            }
        } else {
            String textClass = isSubProcess ? "subprocess-text" : "node-text";
            svg.append(String.format(
                    "<text x=\"%.1f\" y=\"%.1f\" class=\"%s\" " +
                            "font-size=\"%.1f\" fill=\"#333\">%s</text>\n",
                    x + width/2,
                    y + height/2,
                    textClass,
                    fontSize,
                    escapeXml(displayText)
            ));
        }
    }

    // 错误 SVG
    private String getErrorSvg(String message) {
        return String.format(
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"500\" height=\"300\">" +
                        "<rect width=\"100%%\" height=\"100%%\" fill=\"#f8f9fa\"/>" +
                        "<text x=\"50%%\" y=\"50%%\" dominant-baseline=\"middle\" text-anchor=\"middle\" " +
                        "fill=\"#ff4444\" font-family=\"Arial\">%s</text></svg>",
                escapeXml(message)
        );
    }

    // XML 转义
    private String escapeXml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}