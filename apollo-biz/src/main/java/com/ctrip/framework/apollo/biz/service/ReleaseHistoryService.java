/*
 * Copyright 2022 Apollo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.ctrip.framework.apollo.biz.service;

import com.ctrip.framework.apollo.biz.entity.Audit;
import com.ctrip.framework.apollo.biz.entity.ReleaseHistory;
import com.ctrip.framework.apollo.biz.repository.ReleaseHistoryRepository;
import com.google.gson.Gson;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
@Service
public class ReleaseHistoryService {
  private static final Gson GSON = new Gson();

  private final ReleaseHistoryRepository releaseHistoryRepository;
  private final AuditService auditService;

  public ReleaseHistoryService(
      final ReleaseHistoryRepository releaseHistoryRepository, final AuditService auditService) {
    this.releaseHistoryRepository = releaseHistoryRepository;
    this.auditService = auditService;
  }

  public Page<ReleaseHistory> findReleaseHistoriesByNamespace(
      String appId, String clusterName, String namespaceName, Pageable pageable) {
    return releaseHistoryRepository.findByAppIdAndClusterNameAndNamespaceNameOrderByIdDesc(
        appId, clusterName,
        namespaceName, pageable);
  }

  public Page<ReleaseHistory> findByReleaseIdAndOperation(
      long releaseId, int operation, Pageable page) {
    return releaseHistoryRepository.findByReleaseIdAndOperationOrderByIdDesc(
        releaseId, operation, page);
  }

  public Page<ReleaseHistory> findByPreviousReleaseIdAndOperation(
      long previousReleaseId, int operation, Pageable page) {
    return releaseHistoryRepository.findByPreviousReleaseIdAndOperationOrderByIdDesc(
        previousReleaseId, operation, page);
  }

  public Page<ReleaseHistory> findByReleaseIdAndOperationInOrderByIdDesc(
      long releaseId, Set<Integer> operations, Pageable page) {
    return releaseHistoryRepository.findByReleaseIdAndOperationInOrderByIdDesc(
        releaseId, operations, page);
  }

  @Transactional
  public ReleaseHistory createReleaseHistory(
      String appId,
      String clusterName,
      String namespaceName,
      String branchName,
      long releaseId,
      long previousReleaseId,
      int operation,
      Map<String, Object> operationContext,
      String operator) {
    ReleaseHistory releaseHistory = new ReleaseHistory();
    releaseHistory.setAppId(appId);
    releaseHistory.setClusterName(clusterName);
    releaseHistory.setNamespaceName(namespaceName);
    releaseHistory.setBranchName(branchName);
    releaseHistory.setReleaseId(releaseId);
    releaseHistory.setPreviousReleaseId(previousReleaseId);
    releaseHistory.setOperation(operation);
    if (operationContext == null) {
      releaseHistory.setOperationContext("{}"); // default empty object
    } else {
      releaseHistory.setOperationContext(GSON.toJson(operationContext));
    }
    releaseHistory.setDataChangeCreatedTime(new Date());
    releaseHistory.setDataChangeCreatedBy(operator);
    releaseHistory.setDataChangeLastModifiedBy(operator);

    releaseHistoryRepository.save(releaseHistory);

    auditService.audit(
        ReleaseHistory.class.getSimpleName(),
        releaseHistory.getId(),
        Audit.OP.INSERT,
        releaseHistory.getDataChangeCreatedBy());

    return releaseHistory;
  }

  @Transactional
  public int batchDelete(String appId, String clusterName, String namespaceName, String operator) {
    return releaseHistoryRepository.batchDelete(appId, clusterName, namespaceName, operator);
  }
}
