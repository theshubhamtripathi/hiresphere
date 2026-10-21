package com.hiresphere.job.dto;

import java.io.Serializable;

/** Response for a skill or tag. */
public record NamedResponse(Long id, String name) implements Serializable {
}
