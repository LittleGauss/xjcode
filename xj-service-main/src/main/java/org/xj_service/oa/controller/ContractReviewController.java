package org.xj_service.oa.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.xj_service.oa.common.BusinessType;
import org.xj_service.oa.common.Constants;
import org.xj_service.oa.common.Log;
import org.xj_service.oa.common.Result;
import org.xj_service.oa.entity.ContractReview;
import org.xj_service.oa.entity.ApprovalComment;
import org.xj_service.oa.entity.ContractAttachment;
import org.xj_service.oa.service.IContractReviewService;
import org.xj_service.oa.service.IApprovalCommentService;
import org.xj_service.oa.service.IContractAttachmentService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.xj_service.oa.service.FileStorageService;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/contract-review")
public class ContractReviewController {

    @Resource
    private IContractReviewService contractReviewService;

    @Resource
    private IApprovalCommentService approvalCommentService;

    @Autowired
    private IContractAttachmentService attachmentService;

    @Autowired(required = false)
    private FileStorageService fileStorageService; // 可选注入（根据配置启用MinIO）

    // 原有接口保持不变...

    // 新增：提交审批意见
    @PostMapping("/approve")
    public Result submitApproval(@RequestBody ApprovalComment comment) {
        // 业务类型固定为"contract"
        comment.setBusinessType("contract");
        comment.setApprovedTime(java.time.LocalDateTime.now());
        return Result.success(approvalCommentService.saveComment(comment));
    }

    // 新增：查询合同的审批意见
    @GetMapping("/comments/{contractId}")
    public Result getComments(@PathVariable Integer contractId) {
        return Result.success(approvalCommentService.getByBusiness(contractId, "contract"));
    }

    /* ================= 合同 CRUD ================= */

    // 分页列表 & 条件查询
    @GetMapping
    public Result list(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer size,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String keyword) {
        Page<ContractReview> p = new Page<>(page, size);
        LambdaQueryWrapper<ContractReview> qw = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            qw.eq(ContractReview::getStatus, cnStatusToEn(status));
        }
        if (keyword != null && !keyword.isEmpty()) {
            qw.like(ContractReview::getContractName, keyword);
        }
        contractReviewService.page(p, qw);
        Map<String, Object> data = new HashMap<>();
        data.put("records", p.getRecords());
        data.put("total", p.getTotal());
        return Result.success(data);
    }

    /**
     * 根据当前工作流的审批人返回分配给他的待办合同列表。
     * 优先使用请求参数 `user`（可为用户名或ID字符串），如未提供则尝试从 Spring Security 获取认证名。
     * 支持分页：`page`、`size`。
     */
    @GetMapping("/assigned")
    public Result assigned(@RequestParam(required = false) String user,
                           @RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "10") Integer size) {
        String current = user;
        if (current == null || current.trim().isEmpty()) {
            try {
                current = SecurityContextHolder.getContext().getAuthentication().getName();
            } catch (Exception ignored) {
            }
        }
        if (current == null || current.trim().isEmpty()) {
            return Result.error(Constants.CODE_401, "未指定当前审批人，请通过参数'user'或登录态提供");
        }

        Page<ContractReview> p = new Page<>(page, size);
        LambdaQueryWrapper<ContractReview> qw = new LambdaQueryWrapper<>();
        // 为 lambda 捕获使用不可变副本
        final String cur = current;
        // 支持精确匹配或包含匹配（兼容 currentApprover 存储为单值或逗号/JSON 列表）
        qw.and(wrapper -> wrapper.eq(ContractReview::getCurrentApprover, cur)
            .or().like(ContractReview::getCurrentApprover, cur));

        contractReviewService.page(p, qw);
        Map<String, Object> data = new HashMap<>();
        data.put("records", p.getRecords());
        data.put("total", p.getTotal());
        return Result.success(data);
    }

    // 详情
    @GetMapping("/{id}")
    public Result detail(@PathVariable Integer id) {
        ContractReview review = contractReviewService.getById(id);
        if (review == null) return Result.error(Constants.CODE_400, "合同不存在");
        List<ContractAttachment> attachments = attachmentService.listByContract(id);
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", review.getId());
        dto.put("contractName", review.getContractName());
        dto.put("applicantName", review.getApplicantName());
        dto.put("department", review.getDepartment());
        dto.put("contractType", review.getContractType());
        dto.put("submissionDate", review.getSubmissionDate());
        dto.put("status", review.getStatus());
        dto.put("reviewComments", review.getReviewComments());
        dto.put("createdAt", review.getCreatedAt());
        dto.put("attachments", attachments);
        // 示例审批步骤（静态，可后续接入流程引擎）
        dto.put("approvalSteps", buildApprovalSteps(review.getStatus()));
        return Result.success(dto);
    }

    // 新增合同
    @PostMapping
    public Result create(@RequestBody Map<String, Object> body) {
        ContractReview review = new ContractReview();
        review.setContractName(asString(body.get("contractName")));
        review.setApplicantName(asString(body.get("applicantName")));
        review.setDepartment(asString(body.get("department")));
        review.setContractType(asString(body.get("contractType")));
        review.setReviewComments(asString(body.get("reviewComments")));
        // 日期处理
        review.setSubmissionDate(parseDate(asString(body.get("submissionDate"))));
        if (review.getSubmissionDate() == null) {
            review.setSubmissionDate(LocalDate.now());
        }
        String statusRaw = asString(body.get("status"));
        review.setStatus(cnStatusToEn(statusRaw.isEmpty() ? "draft" : statusRaw));
        review.setCreatedAt(LocalDateTime.now());
        boolean saved = contractReviewService.save(review);
        return saved ? Result.success(Collections.singletonMap("id", review.getId()))
                : Result.error(Constants.CODE_500, "创建失败");
    }

    // 更新合同（用于退回修改等场景）
    @PutMapping("/{id}")
    public Result update(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        ContractReview review = contractReviewService.getById(id);
        if (review == null) {
            return Result.error(Constants.CODE_400, "合同不存在");
        }

        if (body.containsKey("contractName")) {
            review.setContractName(asString(body.get("contractName")));
        }
        if (body.containsKey("applicantName")) {
            review.setApplicantName(asString(body.get("applicantName")));
        }
        if (body.containsKey("department")) {
            review.setDepartment(asString(body.get("department")));
        }
        if (body.containsKey("contractType")) {
            review.setContractType(asString(body.get("contractType")));
        }
        if (body.containsKey("reviewComments")) {
            review.setReviewComments(asString(body.get("reviewComments")));
        }
        if (body.containsKey("submissionDate")) {
            LocalDate d = parseDate(asString(body.get("submissionDate")));
            if (d != null) {
                review.setSubmissionDate(d);
            }
        }
        if (body.containsKey("status")) {
            String status = cnStatusToEn(asString(body.get("status")));
            if (!status.isEmpty()) {
                review.setStatus(status);
            }
        }

        contractReviewService.updateById(review);
        return Result.success(true);
    }

    // 删除合同（级联删除附件与存储文件）
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        ContractReview review = contractReviewService.getById(id);
        if (review == null) {
            return Result.error(Constants.CODE_400, "合同不存在");
        }
        // 先查询附件，准备删除对象存储文件
        List<ContractAttachment> atts = attachmentService.listByContract(id);
        boolean ok = contractReviewService.removeById(id);
        if (!ok) {
            return Result.error(Constants.CODE_400, "删除失败");
        }
        // 删除数据库附件记录 & 存储文件
        if (atts != null) {
            for (ContractAttachment a : atts) {
                if (fileStorageService != null && a.getStorePath() != null && !a.getStorePath().isEmpty()) {
                    try { fileStorageService.delete(a.getStorePath()); } catch (Exception ignored) {}
                }
            }
        }
        attachmentService.deleteByContract(id);
        return Result.success(true);
    }

    // 更新状态
    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        ContractReview review = contractReviewService.getById(id);
        if (review == null) return Result.error(Constants.CODE_400, "合同不存在");
        String status = cnStatusToEn(body.getOrDefault("status", ""));
        if (status.isEmpty()) return Result.error(Constants.CODE_400, "状态不能为空");
        review.setStatus(status);
        contractReviewService.updateById(review);
        return Result.success(true);
    }

    /* ================= 统计 ================= */

    @GetMapping("/statistics")
    public Result statistics(@RequestParam(required = false) String department) {
        List<ContractReview> list;
        if (department == null || department.trim().isEmpty() || "all".equalsIgnoreCase(department)) {
            list = contractReviewService.list();
        } else {
            LambdaQueryWrapper<ContractReview> qw = new LambdaQueryWrapper<>();
            qw.eq(ContractReview::getDepartment, department.trim());
            list = contractReviewService.list(qw);
        }
        Map<String, Object> stats = buildStatistics(list);
        return Result.success(stats);
    }

    /* ================= 附件接口 ================= */
    @Log(title = "合同上传", businessType = BusinessType.UPLOAD, isSaveRequestData = false)
    @PostMapping("/{id}/attachment")
    public Result upload(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        ContractReview review = contractReviewService.getById(id);
        if (review == null) return Result.error(Constants.CODE_400, "合同不存在");
        if (file.isEmpty()) return Result.error(Constants.CODE_400, "文件不能为空");

        ContractAttachment att = new ContractAttachment();
        att.setContractId(id);
        att.setName(file.getOriginalFilename());
        att.setType(detectType(file.getOriginalFilename()));
        att.setSize(file.getSize());
        att.setUploadTime(LocalDateTime.now());

        // 如果启用了文件存储服务，则上传并记录路径
        if (fileStorageService != null) {
            try {
                // 使用默认桶（传null）与业务类型"contract"归档
                String storedPath = fileStorageService.upload(file, null, "contract");
                att.setStorePath(storedPath);
            } catch (Exception e) {
                return Result.error(Constants.CODE_500, "文件存储失败: " + e.getMessage());
            }
        }

        attachmentService.save(att);
        return Result.success(att);
    }

    @GetMapping("/{id}/attachments")
    public Result listAttachments(@PathVariable Integer id) {
        return Result.success(attachmentService.listByContract(id));
    }

    @DeleteMapping("/{id}/attachment/{attId}")
    public Result deleteAttachment(@PathVariable Integer id, @PathVariable Integer attId) {
        ContractAttachment att = attachmentService.getById(attId);
        if (att == null || !Objects.equals(att.getContractId(), id)) {
            return Result.error(Constants.CODE_400, "附件不存在");
        }
        // 同步删除存储文件
        if (fileStorageService != null && att.getStorePath() != null && !att.getStorePath().isEmpty()) {
            try { fileStorageService.delete(att.getStorePath()); } catch (Exception ignored) {}
        }
        attachmentService.removeById(attId);
        return Result.success(true);
    }

    // 新增：生成附件临时下载URL（或直接下载）
    @GetMapping("/{id}/attachment/{attId}/download")
    public Result downloadAttachment(@PathVariable Integer id, @PathVariable Integer attId,
                                     @RequestParam(value = "expire", required = false) Integer expireSeconds) {
        ContractAttachment att = attachmentService.getById(attId);
        if (att == null || !Objects.equals(att.getContractId(), id)) {
            return Result.error(Constants.CODE_400, "附件不存在");
        }
        if (fileStorageService == null) {
            return Result.error(Constants.CODE_500, "当前未启用文件存储服务，无法下载");
        }
        if (att.getStorePath() == null || att.getStorePath().isEmpty()) {
            return Result.error(Constants.CODE_500, "附件未记录存储路径，无法下载");
        }
        try {
            String url = fileStorageService.getTempUrl(att.getStorePath(), expireSeconds == null ? 600 : expireSeconds);
            Map<String, Object> data = new HashMap<>();
            data.put("id", att.getId());
            data.put("name", att.getName());
            data.put("url", url);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(Constants.CODE_500, "生成下载链接失败: " + e.getMessage());
        }
    }

    /* ================= 辅助方法 ================= */

    private String asString(Object o) { return o == null ? "" : String.valueOf(o).trim(); }

    private LocalDate parseDate(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return LocalDate.parse(s.substring(0, 10)); } catch (DateTimeParseException e) { return null; }
    }

    private String cnStatusToEn(String cn) {
        Map<String, String> map = new HashMap<>();
        map.put("待审批", "draft");
        map.put("审批中", "approved");
        map.put("已完成", "completed");
        return map.getOrDefault(cn, cn == null ? "" : cn);
    }

    private List<Map<String, Object>> buildApprovalSteps(String status) {
        // 根据当前状态标记步骤是否完成
        List<String> order = Arrays.asList("draft", "approved", "completed");
        int idx = order.indexOf(status);
        List<Map<String, Object>> steps = new ArrayList<>();
        steps.add(step("提交申请", "申请人", idx >= 0));
        steps.add(step("部门审批", "部门主管", idx >= 1));
        steps.add(step("已完成", "行政办", idx >= 2));
        return steps;
    }

    private Map<String, Object> step(String title, String role, boolean done) {
        Map<String, Object> m = new HashMap<>();
        m.put("title", title);
        m.put("role", role);
        m.put("status", done ? "completed" : "pending");
        return m;
    }

    private String detectType(String filename) {
        if (filename == null) return "other";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return "pdf";
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) return "word";
        return "other";
    }

    private Map<String, Object> buildStatistics(List<ContractReview> list) {
        // 月度
        int currentYear = LocalDate.now().getYear();
        int[] submitted = new int[12];
        int[] approved = new int[12];
        for (ContractReview r : list) {
            LocalDate d = r.getSubmissionDate();
            if (d != null && d.getYear() == currentYear) {
                int m = d.getMonthValue();
                submitted[m - 1]++;
                if ("approved".equalsIgnoreCase(r.getStatus()) || "completed".equalsIgnoreCase(r.getStatus())) {
                    approved[m - 1]++;
                }
            }
        }
        Map<String, Object> monthly = new HashMap<>();
        monthly.put("categories", Arrays.stream(new int[12]).mapToObj(i -> "" ).collect(Collectors.toList()));
        monthly.put("categories", Arrays.asList("1月","2月","3月","4月","5月","6月","7月","8月","9月","10月","11月","12月"));
        monthly.put("submitted", Arrays.stream(submitted).boxed().collect(Collectors.toList()));
        monthly.put("approved", Arrays.stream(approved).boxed().collect(Collectors.toList()));

        // 季度
        int[] qSubmitted = new int[4];
        int[] qApproved = new int[4];
        for (int i = 0; i < 12; i++) { qSubmitted[i/3] += submitted[i]; qApproved[i/3] += approved[i]; }
        Map<String, Object> quarter = new HashMap<>();
        quarter.put("categories", Arrays.asList("Q1","Q2","Q3","Q4"));
        quarter.put("submitted", Arrays.stream(qSubmitted).boxed().collect(Collectors.toList()));
        quarter.put("approved", Arrays.stream(qApproved).boxed().collect(Collectors.toList()));

        // 年度（最近5年）
        Map<Integer, int[]> yearMap = new TreeMap<>();
        for (ContractReview r : list) {
            LocalDate d = r.getSubmissionDate();
            if (d == null) continue;
            yearMap.putIfAbsent(d.getYear(), new int[]{0,0});
            yearMap.get(d.getYear())[0]++; // 提交
            if ("approved".equalsIgnoreCase(r.getStatus()) || "completed".equalsIgnoreCase(r.getStatus())) {
                yearMap.get(d.getYear())[1]++; // 完成
            }
        }
        List<Integer> years = new ArrayList<>(yearMap.keySet());
        if (years.size() > 5) years = years.subList(years.size()-5, years.size());
        List<Integer> ySubmitted = new ArrayList<>();
        List<Integer> yApproved = new ArrayList<>();
        for (Integer y : years) { ySubmitted.add(yearMap.get(y)[0]); yApproved.add(yearMap.get(y)[1]); }
        Map<String, Object> year = new HashMap<>();
        year.put("categories", years.stream().map(String::valueOf).collect(Collectors.toList()));
        year.put("submitted", ySubmitted);
        year.put("approved", yApproved);

        Map<String, Object> stats = new HashMap<>();
        stats.put("monthlyData", monthly);
        stats.put("quarterData", quarter);
        stats.put("yearData", year);
        return stats;
    }
}