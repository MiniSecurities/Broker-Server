package com.sangyunpark99.brokerserver.fill.repository;

import com.sangyunpark99.brokerserver.fill.Fill;
import com.sangyunpark99.brokerserver.fill.FillStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FillRepository extends JpaRepository<Fill, Long> {

    List<Fill> findTop100ByStatusAndCreatedAtBeforeOrderByIdAsc(FillStatus status, LocalDateTime createdAt);

}
