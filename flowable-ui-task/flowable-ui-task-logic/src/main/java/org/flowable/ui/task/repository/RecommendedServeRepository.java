package org.flowable.ui.task.repository;

import org.flowable.ui.task.model.recommendation.RecommendedServe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecommendedServeRepository extends JpaRepository<RecommendedServe, Integer> {
    @Query(value = "select ID_,relevance, SERVICE_ID_, SERVICE_NAME_, ACCESS_POINT_ from act_proc where ACT_NAME_= ?1 and IS_PROCESS_=0 order by relevance desc", nativeQuery = true)
    List<RecommendedServe> findByActIdOrderByRelevanceDesc(String actName);

    @Query(value = "select ID_, relevance, SERVICE_ID_, SERVICE_NAME_, ACCESS_POINT_ from act_proc where SERVICE_NAME_= ?1", nativeQuery = true)
    RecommendedServe findByServiceName(String serviceName);
}