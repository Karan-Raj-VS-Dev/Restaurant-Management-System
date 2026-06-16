package com.restaurant.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditTimelineControllerTest {

    @Mock
    private AuditTimelineService auditTimelineService;

    @Test
    void listEventsUsesScopedPathValuesBeforeQueryValues() {
        AuditTimelineController controller = new AuditTimelineController(auditTimelineService);
        AuditEventResponse response = new AuditEventResponse(
                "event-001",
                "tenant-path",
                "property-path",
                "order.created",
                "Order created",
                Instant.parse("2026-06-15T04:30:00Z")
        );
        when(auditTimelineService.listEvents("tenant-path", "property-path", "ref-001"))
                .thenReturn(List.of(response));

        List<AuditEventResponse> results = controller.listEvents(
                "tenant-path",
                "property-path",
                "tenant-query",
                "property-query",
                "ref-001"
        );

        assertThat(results).containsExactly(response);
        verify(auditTimelineService).listEvents("tenant-path", "property-path", "ref-001");
    }

    @Test
    void listEventsFallsBackToDefaultScopeWhenNoValuesProvided() {
        AuditTimelineController controller = new AuditTimelineController(auditTimelineService);
        when(auditTimelineService.listEvents("bikini-bottom", "krusty-krab", null))
                .thenReturn(List.of());

        List<AuditEventResponse> results = controller.listEvents(null, null, null, null, null);

        assertThat(results).isEmpty();
        verify(auditTimelineService).listEvents("bikini-bottom", "krusty-krab", null);
    }
}
