package com.restaurant.property;

import java.util.List;

public record PropertySettingsModuleResponse(
        String moduleId,
        String title,
        String description,
        String ownerService,
        List<String> highlights,
        boolean placeholder
) {
}
