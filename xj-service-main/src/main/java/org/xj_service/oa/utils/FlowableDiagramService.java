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
import org.flowable.bpmn.model.*;
import javax.annotation.Resource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class FlowableDiagramService {

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private RuntimeService runtimeService;

    @Autowired
    private ProcessEngineConfiguration processEngineConfiguration;

    @Autowired
    private ProcessEngine processEngine;

    private HistoryService historyService;

    /**
     * 直接使用Flowable的png生成器（不进行复杂处理）
     */
    public String generatePngProcessDiagram(String processInstanceId){
        try {
            // 1. 获取流程实例
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();

            if (processInstance == null) {
                throw new RuntimeException("流程实例未找到: " + processInstanceId);
            }

            // 2. 获取 BPMN 模型
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());

            // 3. 获取当前活跃的节点 ID 列表（即需要高亮的节点）
            List<String> activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);

            // 4. 获取图形生成器
            ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();

            // 5. 生成图片流
            // 参数说明：bpmnModel, imageType, highLightedActivities, highLightedFlows,
            // activityFontName, labelFontName, annotationFontName, customClassLoader, scaleFactor, drawSequenceFlowNameWithNoLabelDI
            InputStream imageStream = diagramGenerator.generateDiagram(
                    bpmnModel,
                    "png",
                    activeActivityIds,
                    Collections.emptyList(), // 如果需要高亮走过的连线，可以在这里传入连线ID列表
                    "SansSerif",  // 节点字体，防止中文乱码
                    "SansSerif",  // 标签字体
                    "SansSerif",  // 注解字体
                    null,
                    1.3,
                    true
            );


            // 3. 将 InputStream 转换为 byte 数组
            byte[] bytes = IOUtils.toByteArray(imageStream);

            // 4. 将 byte 数组编码为 Base64 字符串
            String pngContent = Base64.getEncoder().encodeToString(bytes);
            imageStream.close();
            // 5. 拼接成 Data URI 格式并返回
            return "data:image/png;base64," + pngContent;
        } catch (Exception e) {
            e.printStackTrace();
            // 实际项目中建议使用全局异常处理
            return null;
        }
    }

    /**
     * 直接使用Flowable的SVG生成器（不进行复杂处理）
     */
    public String generateSvgProcessDiagram(String processInstanceId) {
        try {
            System.out.println("开始生成SVG流程图，流程实例ID: " + processInstanceId);

            // 1. 获取流程实例
            ProcessInstance processInstance = runtimeService
                    .createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();

            if (processInstance == null) {
                throw new RuntimeException("流程实例不存在: " + processInstanceId);
            }

            System.out.println("找到流程实例: " + processInstance.getProcessDefinitionName()
                    + ", 定义ID: " + processInstance.getProcessDefinitionId());

            // 2. 获取BPMN模型
            BpmnModel bpmnModel = repositoryService
                    .getBpmnModel(processInstance.getProcessDefinitionId());

            if (bpmnModel == null) {
                throw new RuntimeException("BPMN模型不存在");
            }

            System.out.println("BPMN模型获取成功，流程数: " + bpmnModel.getProcesses().size());

            // 3. 获取活动节点
            List<String> activeActivityIds = new ArrayList<>();
            if (!processInstance.isEnded()) {
                activeActivityIds = runtimeService.getActiveActivityIds(processInstanceId);
            }
            System.out.println("活动节点: " + activeActivityIds);

            String svgContent= generateFallbackDiagram(bpmnModel, activeActivityIds);

            // 修复中文乱码问题
            svgContent = fixSvgFonts(svgContent);

            return svgContent;
        } catch (Exception e) {
            System.err.println("生成SVG流程图失败: " + e.getMessage());
            e.printStackTrace();
            return generateErrorSvg(e.getMessage());
        }
    }

    /**
     * 修复SVG字体问题，确保中文正常显示
     */
    private String fixSvgFonts(String svgContent) {
        if (svgContent == null || svgContent.trim().isEmpty()) {
            return svgContent;
        }

        // 1. 确保有XML声明和UTF-8编码
        if (!svgContent.contains("<?xml")) {
            svgContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + svgContent;
        }

        // 2. 添加样式定义，设置中文字体
        if (!svgContent.contains("<style")) {
            String style = "<style>\n" +
                    "  text, tspan {\n" +
                    "    font-family: 'Microsoft YaHei', 'SimSun', 'PingFang SC', sans-serif;\n" +
                    "    font-size: 14px;\n" +
                    "    fill: #333333;\n" +
                    "  }\n" +
                    "</style>\n";

            // 在第一个<svg>标签后插入样式
            svgContent = svgContent.replaceFirst("<svg", "<svg> " + style);
        }

        return svgContent;
    }

    /**
     * 生成错误信息的SVG
     */
    private String generateErrorSvg(String errorMessage) {
        String safeMessage = errorMessage != null ?
                errorMessage.replace("\"", "'") : "未知错误";
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"800\" height=\"600\">\n" +
                "  <rect width=\"100%\" height=\"100%\" fill=\"#ffe6e6\"/>\n" +
                "  <text x=\"400\" y=\"250\" text-anchor=\"middle\" font-family=\"Arial\" font-size=\"20\" fill=\"#d32f2f\">\n" +
                "    流程图生成失败\n" +
                "  </text>\n" +
                "  <text x=\"400\" y=\"300\" text-anchor=\"middle\" font-family=\"Arial\" font-size=\"14\" fill=\"#666\">\n" +
                "    " + safeMessage + "\n" +
                "  </text>\n" +
                "</svg>";
    }


    /**
     * 生成高亮当前节点的 SVG 流程图
     * @param processInstanceId 流程实例ID（前端传递）
     * @return SVG 字符串（前端可直接渲染）
     */
    public String generateHighlightedSvg(String processInstanceId) {
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
                    FlowElement flowElement = bpmnModel.getMainProcess().getFlowElement(activityId);
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

    private String generateFallbackDiagram(BpmnModel bpmnModel, List<String> activeActivityIds) {
        try {
            org.flowable.bpmn.model.Process process = bpmnModel.getProcesses().get(0);
            Map<String, GraphicInfo> locationMap = bpmnModel.getLocationMap();
            Map<String, List<GraphicInfo>> flowLocationMap = bpmnModel.getFlowLocationMap();

            // 【优化点 A】：调整布局参数 - 让节点更紧凑但不拥挤
            final double LAYOUT_SCALE_FACTOR = 1.0; // 保持原始尺寸，不再放大
            final double NODE_WIDTH_PADDING = 25.0; // 减小文字留白，让节点更紧凑
            final double NODE_HEIGHT_PADDING = 10.0; // 增加高度留白，让文字显示更完整
            final double MARGIN = 40.0; // 减小四周留白

            // 【新增】：节点间距参数
            final double NODE_SPACING = 60.0; // 节点之间的最小间距
            final double TEXT_FONT_SIZE = 12.0; // 增大字体大小

            // 1. 计算边界
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double maxY = Double.MIN_VALUE;

            // 【优化点 B】：优化节点位置计算，避免重叠
            Map<String, Double[]> nodeFinalPositions = new HashMap<>();

            for (FlowElement element : process.getFlowElements()) {
                if (element instanceof FlowNode) {
                    GraphicInfo gi = locationMap.get(element.getId());
                    if (gi != null) {
                        // 使用原始坐标，不再放大
                        double scaledX = gi.getX() * LAYOUT_SCALE_FACTOR;
                        double scaledY = gi.getY() * LAYOUT_SCALE_FACTOR;

                        // 【优化点 C】：减小节点尺寸
                        double scaledWidth = Math.max(gi.getWidth() * 0.8, 80); // 最小宽度80
                        double scaledHeight = Math.max(gi.getHeight() * 0.8, 40); // 最小高度40

                        // 检查并调整节点位置，避免重叠
                        double adjustedX = scaledX;
                        double adjustedY = scaledY;

                        // 查找是否有重叠的节点
                        for (Map.Entry<String, Double[]> existingNode : nodeFinalPositions.entrySet()) {
                            Double[] pos = existingNode.getValue();
                            double existingX = pos[0];
                            double existingY = pos[1];
                            double existingWidth = pos[2];
                            double existingHeight = pos[3];

                            // 如果重叠，调整位置
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
                    List<GraphicInfo> waypoints = flowLocationMap.get(element.getId());
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
                return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"500\" height=\"300\">" +
                        "<text x=\"50%\" y=\"50%\" dominant-baseline=\"middle\" text-anchor=\"middle\">" +
                        "未找到流程元素</text></svg>";
            }

            // 计算全局偏移量
            double globalOffsetX = MARGIN - minX;
            double globalOffsetY = MARGIN - minY;

            // 计算ViewBox尺寸
            double viewBoxWidth = (maxX - minX) + 2 * MARGIN;
            double viewBoxHeight = (maxY - minY) + 2 * MARGIN;

            // 2. 绘制SVG头部
            StringBuilder svg = new StringBuilder();
            svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
            svg.append(String.format(
                    "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100%%\" height=\"100%%\" " +
                            "viewBox=\"0 0 %.1f %.1f\">\n",
                    viewBoxWidth, viewBoxHeight
            ));

            // 【优化点 D】：增强CSS样式，让显示更清晰
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
            svg.append("</style>\n");

            // 箭头定义
            svg.append("<marker id=\"arrow\" markerWidth=\"8\" markerHeight=\"8\" " +
                    "refX=\"7\" refY=\"3\" orient=\"auto\" markerUnits=\"strokeWidth\">\n");
            svg.append("<path d=\"M0,0 L0,6 L9,3 z\" fill=\"#666666\" />\n");
            svg.append("</marker>\n");
            svg.append("</defs>\n");

            // 背景
            svg.append("<rect x=\"0\" y=\"0\" width=\"100%\" height=\"100%\" fill=\"#f5f7fa\"/>\n");

            // 3. 绘制连线（放在节点下面，避免遮挡节点）
            for (SequenceFlow flow : process.findFlowElementsOfType(SequenceFlow.class)) {
                List<GraphicInfo> waypoints = flowLocationMap.get(flow.getId());
                if (waypoints == null || waypoints.size() < 2) continue;

                StringBuilder points = new StringBuilder();
                for (GraphicInfo gi : waypoints) {
                    double finalX = gi.getX() * LAYOUT_SCALE_FACTOR + globalOffsetX;
                    double finalY = gi.getY() * LAYOUT_SCALE_FACTOR + globalOffsetY;
                    points.append(finalX).append(",").append(finalY).append(" ");
                }

                // 【优化点 E】：使连线更细更清晰
                svg.append(String.format(
                        "<polyline points=\"%s\" style=\"fill:none;stroke:#666666;stroke-width:1.5\" " +
                                "marker-end=\"url(#arrow)\"/>\n",
                        points.toString().trim()
                ));
            }

            // 4. 绘制节点
            for (FlowElement element : process.getFlowElements()) {
                if (!(element instanceof FlowNode)) continue;

                GraphicInfo gi = locationMap.get(element.getId());
                if (gi == null) continue;

                String elementId = element.getId();
                String elementName = element.getName() != null ? element.getName() : element.getId();

                // 获取调整后的位置和尺寸
                Double[] nodeData = nodeFinalPositions.get(elementId);
                if (nodeData == null) continue;

                double finalX = nodeData[0] + globalOffsetX;
                double finalY = nodeData[1] + globalOffsetY;
                double adjustedWidth = nodeData[2] + NODE_WIDTH_PADDING;
                double adjustedHeight = nodeData[3] + NODE_HEIGHT_PADDING;

                // 设置节点颜色 - 淡色系配色方案
                String fillColor = "#F8F9FA";  // 非常淡的灰色背景
                String strokeColor = "#DEE2E6"; // 柔和的灰色边框
                double strokeWidth = 2.0;

                // 根据节点类型设置颜色
                if (element instanceof StartEvent) {
                    // 淡绿色
                    fillColor = "#E8F5E9";
                    strokeColor = "#81C784";
                } else if (element instanceof EndEvent) {
                    // 淡红色
                    fillColor = "#FFEBEE";
                    strokeColor = "#E57373";
                } else if (element instanceof UserTask) {
                    // 淡蓝色
                    fillColor = "#E3F2FD";
                    strokeColor = "#64B5F6";
                } else if (element instanceof ExclusiveGateway) {
                    // 淡紫色
                    fillColor = "#F3E5F5";
                    strokeColor = "#BA68C8";
                    // 特殊处理网关，使用菱形
                    adjustedWidth = Math.min(adjustedWidth, adjustedHeight);
                    adjustedHeight = adjustedWidth;
                } else if (element instanceof ServiceTask) {
                    // 淡青色
                    fillColor = "#E0F2F1";
                    strokeColor = "#4DB6AC";
                } else if (element instanceof CallActivity) {
                    // 淡橙色
                    fillColor = "#FFF3E0";
                    strokeColor = "#FFB74D";
                }

                // 如果是活动节点，使用淡橙色高亮
                boolean isActive = activeActivityIds.contains(elementId);
                if (isActive) {
                    // 当前节点高亮
                    fillColor = "#FFF3E0";
                    strokeColor = "#FF9800";
                    strokeWidth = 3.0;
                }

                // 绘制节点形状
                String rectClass = "node-rect" + (isActive ? " active-node" : "");

                if (element instanceof ExclusiveGateway) {
                    // 绘制菱形网关
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
                    // 绘制矩形节点
                    svg.append(String.format(
                            "<rect x=\"%.1f\" y=\"%.1f\" width=\"%.1f\" height=\"%.1f\" " +
                                    "class=\"%s\" fill=\"%s\" stroke=\"%s\" stroke-width=\"%.1f\"/>\n",
                            finalX, finalY, adjustedWidth, adjustedHeight,
                            rectClass, fillColor, strokeColor, strokeWidth
                    ));
                }
                // 【优化点 F】：优化文本显示，自动换行处理
                String displayText = elementName;

                // 根据节点宽度计算最大字符数
                int maxCharsPerLine = (int)(adjustedWidth / (TEXT_FONT_SIZE * 0.6)); // 估算字符宽度

                if (displayText.length() > maxCharsPerLine && maxCharsPerLine > 0) {
                    // 文本太长，进行换行处理
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

                    // 限制最多显示3行
                    int maxLines = Math.min(3, lines.size());
                    double lineHeight = TEXT_FONT_SIZE * 1.2;
                    double textStartY = finalY + adjustedHeight/2 - ((maxLines-1) * lineHeight)/2;

                    for (int i = 0; i < maxLines; i++) {
                        String line = lines.get(i);
                        // 如果是最后一行且有省略
                        if (i == maxLines - 1 && lines.size() > maxLines) {
                            line = line.substring(0, Math.max(0, line.length()-3)) + "...";
                        }

                        svg.append(String.format(
                                "<text x=\"%.1f\" y=\"%.1f\" class=\"node-text\" " +
                                        "font-size=\"%.1f\" fill=\"#333\">%s</text>\n",
                                finalX + adjustedWidth/2,
                                textStartY + (i * lineHeight),
                                TEXT_FONT_SIZE,
                                escapeXml(line)
                        ));
                    }
                } else {
                    // 单行文本
                    svg.append(String.format(
                            "<text x=\"%.1f\" y=\"%.1f\" class=\"node-text\" " +
                                    "font-size=\"%.1f\" fill=\"#333\">%s</text>\n",
                            finalX + adjustedWidth/2,
                            finalY + adjustedHeight/2,
                            TEXT_FONT_SIZE,
                            escapeXml(displayText)
                    ));
                }
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

    // 新增辅助方法：转义XML特殊字符
    private String escapeXml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    // 错误SVG生成方法
    private String getErrorSvg(String errorMessage) {
        return String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"400\" height=\"100\">" +
                        "<rect x=\"0\" y=\"0\" width=\"100%%\" height=\"100%%\" fill=\"#fff5f5\"/>" +
                        "<text x=\"50%%\" y=\"50%%\" text-anchor=\"middle\" dy=\".3em\" " +
                        "font-family=\"'Microsoft YaHei', sans-serif\" font-size=\"14\">%s</text>" +
                        "</svg>",
                escapeXml(errorMessage)
        );
    }
}