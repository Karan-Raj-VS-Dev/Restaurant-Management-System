package com.restaurant.eventgateway.persistence.repository;

import com.restaurant.eventgateway.persistence.entity.EventDispatchEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventDispatchRepository extends JpaRepository<EventDispatchEntity, String> {

    List<EventDispatchEntity> findByDispatchStatus(String dispatchStatus);

    List<EventDispatchEntity> findByEventId(String eventId);
}
