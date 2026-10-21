package com.hiresphere.job.dto;

import java.io.Serializable;

public record CategoryResponse(Long id, String name, String slug, String icon, String description)
        implements Serializable {
}
