package org.xj_service.oa.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xj_service.oa.entity.ConsumableInOutRecord;
import org.xj_service.oa.entity.ConsumableNotice;
import org.xj_service.oa.mapper.ConsumableInOutRecordMapper;
import org.xj_service.oa.mapper.ConsumableNoticeMapper;
import org.xj_service.oa.service.ConsumableNoticeService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConsumableNoticeServiceImpl extends ServiceImpl<ConsumableNoticeMapper, ConsumableNotice> implements ConsumableNoticeService {

    @Autowired
    private ConsumableInOutRecordMapper consumableInOutRecordMapper;


    @Autowired
    private ConsumableNoticeMapper consumableNoticeMapper;
    /**
     *
     * 获取3条最新的有效的公示，展示在主页的，不足3条是多少就获取多少
     * @return
     */
    @Override
    public List<ConsumableNotice> getValidNotices() {
        LambdaQueryWrapper<ConsumableNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(ConsumableNotice::getExpireTime, LocalDateTime.now())
                .eq(ConsumableNotice::getStatus, "APPROVED")  // 只获取已批准的公示
                .orderByDesc(ConsumableNotice::getNoticeTime)
                .last("LIMIT 3");
        return this.list(wrapper);
    }

    @Override
    public List<ConsumableNotice> getValidNoticesWithConditions(String noticeType,
                                                                String title,
                                                                String status,
                                                                int pageNum,
                                                                int pageSize) {
        Page<ConsumableNotice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ConsumableNotice> wrapper = new LambdaQueryWrapper<>();

        // 添加公示类型条件（如果提供了类型参数）
        if (noticeType != null && !noticeType.trim().isEmpty()) {
            wrapper.eq(ConsumableNotice::getNoticeType, noticeType);
        }

        // 添加标题模糊查询条件（如果提供了标题参数）
        if (title != null && !title.trim().isEmpty()) {
            wrapper.like(ConsumableNotice::getTitle, title);
        }

        // 添加状态条件（如果提供了状态参数）
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(ConsumableNotice::getStatus, status);
        }

        // 按公示时间降序排列
        wrapper.orderByDesc(ConsumableNotice::getNoticeTime);

        return this.page(page, wrapper).getRecords();
    }

    @Override
    @Transactional
    public void createInboundNotice(String title, String content, Integer creatorId, String creatorName) {
        ConsumableNotice notice = new ConsumableNotice();
        notice.setNoticeType("IN");
        notice.setTitle(title);
        notice.setContent(content);
        notice.setCreatorId(creatorId);
        notice.setCreatorName(creatorName);
        notice.setNoticeTime(LocalDateTime.now());
        notice.setExpireTime(LocalDateTime.now().plusDays(30));
        notice.setStatus("UNAPPROVED"); // 默认状态为待审批

        this.save(notice);
    }

    @Override
    @Transactional
    public void createScrapNotice(String title, String content, Integer creatorId, String creatorName) {
        ConsumableNotice notice = new ConsumableNotice();
        notice.setNoticeType("SCRAP");
        notice.setTitle(title);
        notice.setContent(content);
        notice.setCreatorId(creatorId);
        notice.setCreatorName(creatorName);
        notice.setNoticeTime(LocalDateTime.now());
        notice.setExpireTime(LocalDateTime.now().plusDays(30));
        notice.setStatus("UNAPPROVED"); // 默认状态为待审批

        this.save(notice);
    }

    @Override
    @Transactional
    /**
     * 创建统计公示 - 根据部门和时间段生成领用统计数据并保存为公示
     * 
     * 功能说明：
     * 1. 根据部门名称和指定的时间段查询领用记录
     * 2. 对查询到的记录进行多维度统计分析（汇总数据、按物品统计、按人员统计、时间分布统计等）
     * 3. 将统计结果保存为一条新的公示记录
     * 
     * 参数说明：
     * - title: 公示标题
     * - deptName: 部门名称，用于筛选特定部门的领用记录
     * - creatorId: 创建者ID
     * - creatorName: 创建者姓名
     * - startDate: 统计开始时间
     * - endDate: 统计结束时间
     * 
     * 返回值：成功返回true，失败抛出异常
     */
    public boolean createStatisticalNotice(String title,
                                        String deptName, Integer creatorId,
                                        String creatorName,
                                        LocalDateTime startDate,
                                        LocalDateTime endDate) {
        try {
            // 1. 查询指定时间段内的领用记录
            // 调用queryOutRecords方法，获取指定部门在指定时间范围内的所有出库(OUT)记录
            List<ConsumableInOutRecord> records = queryOutRecords(deptName, startDate, endDate);
            System.out.println("查询的部门名称为：" + deptName + "，开始时间为：" + startDate + "，结束时间为：" + endDate+ "记录数为：" + records.size());
            // 检查是否有符合条件的记录，如果没有则直接返回无记录
            if (records.isEmpty()) {
                return false;
            }

            // 2. 生成统计内容
            // 将原始记录转换为包含各种统计数据的Map结构
            Map<String, Object> statisticalContent = generateStatisticalContent(records);

            // 3. 创建公示记录
            // 根据统计内容构建ConsumableNotice实体对象
            ConsumableNotice notice = buildNoticeEntity(title, statisticalContent,
                    deptName, creatorId, creatorName);

            // 执行数据库插入操作
            int result = consumableNoticeMapper.insert(notice);
            System.out.println("创建统计公示成功，ID为：" + notice.getId());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            // 捕获异常并重新包装抛出，便于上层调用方处理
            System.out.println("创建统计公示失败异常"+e.toString());
            return false;
        }
    }
    
    /**
     * 生成统计内容JSON - 将原始记录转换为多维度统计信息
     * 
     * 包含以下统计维度：
     * - 概要统计(summary): 总记录数、总领用数量、总金额等
     * - 物品统计(itemStatistics): 按物品分组的领用情况
     * - 人员统计(operatorStatistics): 按领用人分组的领用情况
     * - 时间分布(timeDistribution): 按日期统计的领用趋势
     * 
     * 参数说明：
     * - records: 原始领用记录列表
     *
     * 返回值：包含所有统计信息的Map对象
     */
    private Map<String, Object> generateStatisticalContent(List<ConsumableInOutRecord> records) {
        Map<String, Object> content = new LinkedHashMap<>();

        // 1. 统计概要 - 获取总体数据指标
        Map<String, Object> summary = calculateSummary(records);
        content.put("统计概要", summary);

        // 2. 按物品统计 - 统计每种物品的领用情况
        List<Map<String, Object>> itemStats = calculateItemStatistics(records);
        content.put("按物品统计", itemStats);

        // 3. 按领用人统计 - 统计每个人员的领用情况
        List<Map<String, Object>> operatorStats = calculateOperatorStatistics(records);
        content.put("按领用人统计", operatorStats);

        // 4. 按分类统计 - 统计各分类的领用情况
        List<Map<String, Object>> categoryStats = calculateCategoryStatistics(records);
        content.put("按分类统计", categoryStats);

        // 5. 按时间分布统计（日/周/月）- 统计每天的领用趋势
        List<Map<String, Object>> timeDistribution = calculateTimeDistribution(records);
        content.put("按时间分布统计", timeDistribution);

        // 7. 统计生成时间 - 记录本次统计的生成时间
        content.put("统计生成时间", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return content;
    }
    
    /**
     * 计算概要信息 - 提取总体统计数据
     * 
     * 统计指标包括：
     * - 总记录数: 符合条件的领用记录总数
     * - 总领用数量: 所有记录的领用数量之和
     * - 总金额: 所有记录的金额之和
     * - 涉及物品种类数: 不同物品的数量
     * - 涉及分类数: 不同分类的数量
     * - 涉及领用人数: 不同领用人的数量
     */
    private Map<String, Object> calculateSummary(List<ConsumableInOutRecord> records) {
        Map<String, Object> summary = new HashMap<>();

        // 总记录数 - 计算符合要求的记录总数
        summary.put("总记录数", records.size());

        // 总领用数量 - 累加所有记录的领用数量，过滤掉空值
        Integer totalQuantity = records.stream()
                .filter(record -> record.getQuantityChange() != null)  // 过滤掉数量变更为空的记录
                .mapToInt(ConsumableInOutRecord::getQuantityChange)   // 提取数量变更字段
                .sum();                                               // 求和
        summary.put("总领用数量", -totalQuantity);

        // 总金额 - 累加所有记录的金额，保留两位小数
        BigDecimal totalAmount = records.stream()
                .filter(record -> record.getTotalPrice() != null)     // 过滤掉金额为空的记录
                .map(ConsumableInOutRecord::getTotalPrice)            // 提取金额字段
                .reduce(BigDecimal.ZERO, BigDecimal::add);           // 累加求和
        summary.put("总金额", totalAmount.setScale(2, RoundingMode.HALF_UP).toString()); // 保留两位小数

        // 涉及物品种类数 - 通过物品ID和名称组合去重计算不同物品数量
        long itemTypes = records.stream()
                .map(record -> record.getGoodsId() + "-" + record.getItemName()) // 组合物品ID和名称作为唯一标识
                .distinct()                                                      // 去重
                .count();                                                        // 计数
        summary.put("涉及物品种类数", itemTypes);

        // 涉及分类数 - 通过分类名称去重计算不同分类数量
        long categoryTypes = records.stream()
                .filter(record -> StringUtils.isNotBlank(record.getCategoryName())) // 过滤掉分类名称为空的记录
                .map(ConsumableInOutRecord::getCategoryName)                       // 提取分类名称
                .distinct()                                                        // 去重
                .count();                                                          // 计数
        summary.put("涉及类别数", categoryTypes);

        // 涉及领用人数 - 通过领用人姓名去重计算不同人员数量
        long operatorCount = records.stream()
                .filter(record -> StringUtils.isNotBlank(record.getOperatorName())) // 过滤掉领用人姓名为空的记录
                .map(ConsumableInOutRecord::getOperatorName)                       // 提取领用人姓名
                .distinct()                                                        // 去重
                .count();                                                          // 计数
        summary.put("涉及领用人数", operatorCount);

        return summary;
    }

    /**
     * 按物品统计 - 统计每种物品的领用情况
     * 
     * 统计内容包括：
     * - 物品基本信息(goodsId, itemName, categoryName)
     * - 总领用量和总金额
     * - 记录数量
     * - 各领用人对该物品的领用详情
     */
    private List<Map<String, Object>> calculateItemStatistics(List<ConsumableInOutRecord> records) {
        // 按物品ID和名称分组 - 使用"|"连接符组合物品ID和名称作为分组键
        Map<String, List<ConsumableInOutRecord>> groupedByItem = records.stream()
                .collect(Collectors.groupingBy(
                        record -> record.getGoodsId() + "|" + record.getItemName() // 以物品ID+名称作为分组键
                ));

        List<Map<String, Object>> itemStats = new ArrayList<>();

        // 遍历每个物品分组
        for (Map.Entry<String, List<ConsumableInOutRecord>> entry : groupedByItem.entrySet()) {
            List<ConsumableInOutRecord> itemRecords = entry.getValue();
            if (itemRecords.isEmpty()) continue; // 跳过空分组

            // 取第一条记录获取物品的基本信息
            ConsumableInOutRecord firstRecord = itemRecords.get(0);

            // 计算该物品的总领用量和金额
            Integer itemQuantity = itemRecords.stream()
                    .filter(record -> record.getQuantityChange() != null)      // 过滤掉数量变更为空的记录
                    .mapToInt(ConsumableInOutRecord::getQuantityChange)       // 提取数量变更字段
                    .sum();                                                   // 求和

            BigDecimal itemAmount = itemRecords.stream()
                    .filter(record -> record.getTotalPrice() != null)         // 过滤掉金额为空的记录
                    .map(ConsumableInOutRecord::getTotalPrice)                // 提取金额字段
                    .reduce(BigDecimal.ZERO, BigDecimal::add);               // 累加求和


            // 构建该物品的统计信息Map
            Map<String, Object> itemStat = new HashMap<>();
            itemStat.put("物品ID", firstRecord.getGoodsId());        // 物品ID
            itemStat.put("物品名称", firstRecord.getItemName());      // 物品名称
            itemStat.put("分类名称", firstRecord.getCategoryName()); // 分类名称
            itemStat.put("该物品总领用量", -itemQuantity);              // 该物品总领用量
            itemStat.put("总金额", itemAmount.setScale(2, RoundingMode.HALF_UP).toString()); // 该物品总金额
            itemStat.put("领用总记录数", itemRecords.size());          // 该物品相关记录数

            itemStats.add(itemStat);
        }

        // 按领用数量排序 - 按总领用量降序排列
        itemStats.sort((a, b) -> {
            Integer qtyA = (Integer) a.get("该物品总领用量"); // 获取第一个元素的总领用量
            Integer qtyB = (Integer) b.get("该物品总领用量"); // 获取第二个元素的总领用量
            return qtyB.compareTo(qtyA); // 降序排列（大的在前）
        });

        return itemStats;
    }

    /**
     * 按领用人统计 - 统计每个人的领用情况
     * 
     * 统计内容包括：
     * - 领用人基本信息(operatorName, operatorId)
     * - 该人员总领用量和总金额
     * - 该人员相关的记录数量
     * - 该人员领用的各种物品详情
     */
    private List<Map<String, Object>> calculateOperatorStatistics(List<ConsumableInOutRecord> records) {
        // 按领用人分组 - 只处理领用人姓名不为空的记录
        Map<String, List<ConsumableInOutRecord>> groupedByOperator = records.stream()
                .filter(record -> StringUtils.isNotBlank(record.getOperatorName())) // 过滤掉领用人姓名为空的记录
                .collect(Collectors.groupingBy(ConsumableInOutRecord::getOperatorName)); // 按领用人姓名分组

        List<Map<String, Object>> operatorStats = new ArrayList<>();

        // 遍历每个领用人分组
        for (Map.Entry<String, List<ConsumableInOutRecord>> entry : groupedByOperator.entrySet()) {
            List<ConsumableInOutRecord> operatorRecords = entry.getValue();
            if (operatorRecords.isEmpty()) continue; // 跳过空分组

            // 计算该领用人的总领用数量和金额
            Integer operatorQuantity = operatorRecords.stream()
                    .filter(record -> record.getQuantityChange() != null)      // 过滤掉数量变更为空的记录
                    .mapToInt(ConsumableInOutRecord::getQuantityChange)       // 提取数量变更字段
                    .sum();                                                   // 求和

            BigDecimal operatorAmount = operatorRecords.stream()
                    .filter(record -> record.getTotalPrice() != null)         // 过滤掉金额为空的记录
                    .map(ConsumableInOutRecord::getTotalPrice)                // 提取金额字段
                    .reduce(BigDecimal.ZERO, BigDecimal::add);               // 累加求和

            // 按物品统计 - 统计该领用人领用的各物品数量
            Map<String, Integer> itemQuantities = operatorRecords.stream()
                    .collect(Collectors.groupingBy(                          // 按物品名称分组
                            ConsumableInOutRecord::getItemName,             // 分组键：物品名称
                            Collectors.summingInt(ConsumableInOutRecord::getQuantityChange) // 分组值：数量变更的总和
                    ));

            // 构建该领用人的统计信息Map
            Map<String, Object> operatorStat = new HashMap<>();
            operatorStat.put("领用人姓名", entry.getKey());                  // 领用人姓名
//            operatorStat.put("领用人ID", operatorRecords.get(0).getOperatorId()); // 领用人ID（取第一条记录）
            operatorStat.put("该人员总领用量", -operatorQuantity);              // 该人员总领用量
            operatorStat.put("该人员总金额", operatorAmount.setScale(2, RoundingMode.HALF_UP).toString()); // 该人员总金额
            operatorStat.put("该人员相关记录数", operatorRecords.size());          // 该人员相关记录数

            operatorStats.add(operatorStat);
        }

        // 按领用数量排序 - 按总领用量降序排列
        operatorStats.sort((a, b) -> {
            Integer qtyA = (Integer) a.get("该人员总领用量"); // 获取第一个元素的总领用量
            Integer qtyB = (Integer) b.get("该人员总领用量"); // 获取第二个元素的总领用量
            return qtyB.compareTo(qtyA); // 降序排列（大的在前）
        });

        return operatorStats;
    }

    /**
     * 按分类统计 - 统计各分类的领用情况
     * 
     * 统计内容包括：
     * - 分类基本信息(categoryName, categoryId)
     * - 该分类总领用量和总金额
     * - 该分类相关记录数
     * - 该分类下的物品种类数和物品列表
     */
    private List<Map<String, Object>> calculateCategoryStatistics(List<ConsumableInOutRecord> records) {
        // 按分类分组 - 只处理分类名称不为空的记录
        Map<String, List<ConsumableInOutRecord>> groupedByCategory = records.stream()
                .filter(record -> StringUtils.isNotBlank(record.getCategoryName())) // 过滤掉分类名称为空的记录
                .collect(Collectors.groupingBy(ConsumableInOutRecord::getCategoryName)); // 按分类名称分组

        List<Map<String, Object>> categoryStats = new ArrayList<>();

        // 遍历每个分类分组
        for (Map.Entry<String, List<ConsumableInOutRecord>> entry : groupedByCategory.entrySet()) {
            List<ConsumableInOutRecord> categoryRecords = entry.getValue();
            if (categoryRecords.isEmpty()) continue; // 跳过空分组

            // 计算该分类的总领用量和金额
            Integer categoryQuantity = categoryRecords.stream()
                    .filter(record -> record.getQuantityChange() != null)      // 过滤掉数量变更为空的记录
                    .mapToInt(ConsumableInOutRecord::getQuantityChange)       // 提取数量变更字段
                    .sum();                                                   // 求和

            BigDecimal categoryAmount = categoryRecords.stream()
                    .filter(record -> record.getTotalPrice() != null)         // 过滤掉金额为空的记录
                    .map(ConsumableInOutRecord::getTotalPrice)                // 提取金额字段
                    .reduce(BigDecimal.ZERO, BigDecimal::add);               // 累加求和

            // 获取该分类下的物品列表 - 去重后的物品名称
            List<String> items = categoryRecords.stream()
                    .map(ConsumableInOutRecord::getItemName)                  // 提取物品名称
                    .distinct()                                               // 去重
                    .collect(Collectors.toList());                           // 收集为列表

            // 构建该分类的统计信息Map
            Map<String, Object> categoryStat = new HashMap<>();
            categoryStat.put("分类名称", entry.getKey());                 // 分类名称
//            categoryStat.put("分类ID", categoryRecords.get(0).getCategoryId()); // 分类ID（取第一条记录）
            categoryStat.put("该分类总领用量", -categoryQuantity);              // 该分类总领用量
            categoryStat.put("该分类总金额", categoryAmount.setScale(2, RoundingMode.HALF_UP).toString()); // 该分类总金额
            categoryStat.put("该分类相关记录数", categoryRecords.size());          // 该分类相关记录数
            categoryStat.put("该分类下的物品种类数", items.size());                      // 该分类下的物品种类数
            categoryStat.put("该分类下的物品列表", items);                                 // 该分类下的物品列表

            categoryStats.add(categoryStat);
        }

        // 按领用数量排序 - 按总领用量降序排列
        categoryStats.sort((a, b) -> {
            Integer qtyA = (Integer) a.get("该分类总领用量"); // 获取第一个元素的总领用量
            Integer qtyB = (Integer) b.get("该分类总领用量"); // 获取第二个元素的总领用量
            return qtyB.compareTo(qtyA); // 降序排列（大的在前）
        });

        return categoryStats;
    }

    /**
     * 按时间分布统计 - 统计每天的领用趋势
     * 
     * 统计内容包括：
     * - 日期信息(date, weekDay)
     * - 当日记录数
     * - 当日总领用量和总金额
     */
    private List<Map<String, Object>> calculateTimeDistribution(List<ConsumableInOutRecord> records) {
        // 按天分组 - 将记录按操作日期分组
        Map<String, List<ConsumableInOutRecord>> groupedByDay = records.stream()
                .collect(Collectors.groupingBy(
                        record -> record.getOperationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) // 提取日期部分作为分组键
                ));

        List<Map<String, Object>> timeDistribution = new ArrayList<>();

        // 获取日期范围 - 确定统计的起止日期
        LocalDateTime minDate = records.stream()
                .map(ConsumableInOutRecord::getOperationTime)              // 提取操作时间
                .min(LocalDateTime::compareTo)                             // 获取最小时间
                .orElse(LocalDateTime.now());                              // 如果没有记录则使用当前时间

        LocalDateTime maxDate = records.stream()
                .map(ConsumableInOutRecord::getOperationTime)              // 提取操作时间
                .max(LocalDateTime::compareTo)                             // 获取最大时间
                .orElse(LocalDateTime.now());                              // 如果没有记录则使用当前时间

        // 生成连续的日期列表 - 即使某天没有记录也会显示
        List<LocalDate> dateRange = new ArrayList<>();
        LocalDate currentDate = minDate.toLocalDate();                    // 从最小日期开始
        LocalDate endDate = maxDate.toLocalDate();                        // 到最大日期结束

        // 循环添加日期到列表中，确保覆盖整个时间范围
        while (!currentDate.isAfter(endDate)) {
            dateRange.add(currentDate);
            currentDate = currentDate.plusDays(1);                        // 逐天递增
        }

        // 为每个日期生成统计 - 包括没有记录的日期
        for (LocalDate date : dateRange) {
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // 格式化日期字符串
            List<ConsumableInOutRecord> dayRecords = groupedByDay.getOrDefault(dateStr, Collections.emptyList()); // 获取当日记录，不存在则为空列表

            // 计算当日的总领用量和金额
            Integer dayQuantity = dayRecords.stream()
                    .filter(record -> record.getQuantityChange() != null)      // 过滤掉数量变更为空的记录
                    .mapToInt(ConsumableInOutRecord::getQuantityChange)       // 提取数量变更字段
                    .sum();                                                   // 求和

            BigDecimal dayAmount = dayRecords.stream()
                    .filter(record -> record.getTotalPrice() != null)         // 过滤掉金额为空的记录
                    .map(ConsumableInOutRecord::getTotalPrice)                // 提取金额字段
                    .reduce(BigDecimal.ZERO, BigDecimal::add);               // 累加求和

            // 构建当日的统计信息Map
            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("日期", dateStr);                                   // 日期
            dayStat.put("当日记录数", dayRecords.size());                  // 当日记录数
            dayStat.put("当日总领用量", -dayQuantity);                           // 当日总领用量
            dayStat.put("当日总金额", dayAmount.setScale(2, RoundingMode.HALF_UP).toString()); // 当日总金额
            dayStat.put("星期", date.getDayOfWeek().getDisplayName(      // 星期几（简写）
                    TextStyle.SHORT, Locale.CHINA));

            timeDistribution.add(dayStat);
        }

        return timeDistribution;
    }
    
    /**
     * 构建公示实体 - 根据输入参数创建ConsumableNotice对象
     * 
     * 功能说明：
     * - 设置公示类型为"STAT"(统计)
     * - 将统计内容转为JSON字符串存储
     * - 设置公示时间（当前时间）和过期时间（30天后）
     * - 设置初始状态为"UNAPPROVED"(待审批)
     */
    private ConsumableNotice buildNoticeEntity(String title, Map<String, Object> statisticalContent,
                                                 String deptName, Integer creatorId, String creatorName) {
        ConsumableNotice notice = new ConsumableNotice();
        notice.setNoticeType("STAT");                               // 设置公示类型为统计
        notice.setTitle(title);                                     // 设置标题
        notice.setContent(JSON.toJSONString(statisticalContent));   // 将统计内容转为JSON字符串存储
        notice.setDeptName(deptName);                               // 设置部门名称
        notice.setCreatorId(creatorId);                             // 设置创建者ID
        notice.setCreatorName(creatorName);                         // 设置创建者姓名

        // 设置公示时间和过期时间（30天后）
        LocalDateTime now = LocalDateTime.now();                    // 获取当前时间
        notice.setNoticeTime(now);                                  // 设置公示时间为当前时间
        notice.setExpireTime(now.plusDays(30));                     // 设置过期时间为30天后
        notice.setStatus("UNAPPROVED");                             // 设置初始状态为待审批

        return notice;
    }
    /**
     * 查询领用记录
     */
    private List<ConsumableInOutRecord> queryOutRecords(String deptName,
                                                        LocalDateTime startDate,
                                                        LocalDateTime endDate) {
        // 构建查询条件
        QueryWrapper<ConsumableInOutRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("operation_type", "OUT") // 只查询出库记录
                .eq("operator_dept", deptName) // 按部门查询
                .ge("operation_time", startDate) // 开始时间
                .le("operation_time", endDate) // 结束时间
                .orderByAsc("operation_time"); // 按时间排序

        return consumableInOutRecordMapper.selectList(queryWrapper);
    }

    @Override
    public Long deleteExpiredNotices() {
        LambdaQueryWrapper<ConsumableNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(ConsumableNotice::getExpireTime, LocalDateTime.now());
        // 先获取待删除的记录数量
        Long count = this.count(wrapper);

        // 再执行删除操作
        boolean isRemoved = this.remove(wrapper);

        // 返回实际删除的数量
        return isRemoved ? count : 0;
    }

    @Override
    @Transactional
    public boolean approveNoticeWithNewTitle(Integer id, String newTitle) {
        ConsumableNotice notice = this.getById(id);
        if (notice == null) {
            return false;
        }

        // 检查是否已过期
        if (notice.getExpireTime().isBefore(LocalDateTime.now())) {
            notice.setStatus("EXPIRED");
            this.updateById(notice);
            return false;
        }

        // 如果已经批准且标题没有变化，直接返回true
        if ("APPROVED".equals(notice.getStatus()) &&
                (newTitle == null || newTitle.equals(notice.getTitle()))) {
            return true;
        }

        // 更新状态和标题  公示时间就是当前 过期时间是30天后
        notice.setStatus("APPROVED");
        notice.setNoticeTime(LocalDateTime.now());
        // 设置过期时间为30天后
        notice.setExpireTime(LocalDateTime.now().plusDays(30));
        if (newTitle != null && !newTitle.trim().isEmpty()) {
            notice.setTitle(newTitle);
        }

        return this.updateById(notice);
    }


    @Override
    @Transactional
    public boolean deleteNotice(Integer id) {
        return this.removeById(id);
    }

    @Override
    @Transactional
    public int batchDeleteNotices(List<Integer> ids) {
        return this.removeBatchByIds(ids) ? ids.size() : 0;
    }

    @Override
    @Transactional
    public Long updateExpiredNoticesStatus() {
        // 构建查询条件：已过期且状态不是EXPIRED的公示
        LambdaQueryWrapper<ConsumableNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(ConsumableNotice::getExpireTime, LocalDateTime.now())
                .ne(ConsumableNotice::getStatus, "EXPIRED");

        // 获取符合条件的记录数量
        long count = this.count(wrapper);

        // 如果有需要更新的记录，则执行更新
        if (count > 0) {
            ConsumableNotice updateEntity = new ConsumableNotice();
            updateEntity.setStatus("EXPIRED");

            this.update(updateEntity, wrapper);
        }

        return count;
    }

}
