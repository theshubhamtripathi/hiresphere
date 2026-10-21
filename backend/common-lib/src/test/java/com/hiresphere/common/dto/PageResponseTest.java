package com.hiresphere.common.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    @Test
    void mapsPagingMetadataFromSpringPage() {
        var page = new PageImpl<>(List.of("a", "b"), PageRequest.of(0, 2), 5);

        PageResponse<String> response = PageResponse.from(page);

        assertThat(response.content()).containsExactly("a", "b");
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.totalElements()).isEqualTo(5);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isFalse();
    }

    @Test
    void mapsContentWithMapper() {
        var page = new PageImpl<>(List.of(1, 2, 3), PageRequest.of(1, 3), 6);

        PageResponse<String> response = PageResponse.from(page, i -> "n" + i);

        assertThat(response.content()).containsExactly("n1", "n2", "n3");
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.last()).isTrue();
    }
}
