package com.restaurant.audit;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class AuditTimelineStore {

    private final CopyOnWriteArrayList<AuditEntry> entries = new CopyOnWriteArrayList<>();

    public void append(AuditEntry entry) {
        entries.add(entry);
    }

    public List<AuditEntry> findByScope(String tenantId, String propertyId, String referenceId) {
        return entries.stream()
                .filter(entry -> tenantId.equals(entry.tenantId()))
                .filter(entry -> propertyId.equals(entry.propertyId()))
                .filter(entry -> referenceId == null || referenceId.isBlank() || referenceId.equals(entry.referenceId()))
                .sorted((left, right) -> left.occurredAt().compareTo(right.occurredAt()))
                .toList();
    }

}
