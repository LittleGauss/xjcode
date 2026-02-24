package org.xj_service.oa.service;

import org.xj_service.oa.common.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface ISupervisionProcessService {
    Result createAndStart(Map<String, Object> payload, List<Long> uploadIds, HttpServletRequest request);

    Result completeFlowableTask(String flowableTaskId, Map<String, Object> vars, HttpServletRequest request);

    Result listMyProcessTasks(HttpServletRequest request);
}
