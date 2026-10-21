package com.hiresphere.company.service;

import com.hiresphere.common.exception.ConflictException;
import com.hiresphere.company.domain.Company;
import com.hiresphere.company.dto.CompanyResponse;
import com.hiresphere.company.dto.CreateCompanyRequest;
import com.hiresphere.company.mapper.CompanyMapper;
import com.hiresphere.company.repository.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    private final CompanyRepository repository = mock(CompanyRepository.class);
    private final CompanyMapper mapper = mock(CompanyMapper.class);
    private final CompanyService service = new CompanyService(repository, mapper);

    @Test
    void createRejectsSecondCompanyForSameOwner() {
        when(repository.existsByOwnerUserId(1L)).thenReturn(true);
        var request = new CreateCompanyRequest("Acme", null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> service.create(1L, request)).isInstanceOf(ConflictException.class);
    }

    @Test
    void createGeneratesUniqueSlug() {
        when(repository.existsByOwnerUserId(1L)).thenReturn(false);
        when(repository.existsBySlug("acme")).thenReturn(true);
        when(repository.existsBySlug("acme-2")).thenReturn(false);
        when(repository.save(any(Company.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Company.class))).thenAnswer(inv -> {
            Company c = inv.getArgument(0);
            return new CompanyResponse(null, c.getName(), c.getSlug(), null, null, null, null, null,
                    null, null, c.getSocialLinks(), c.isVerified(), c.getStatus(), c.getOwnerUserId(), null);
        });

        var request = new CreateCompanyRequest("Acme", null, null, null, null, null, null, null, null);
        CompanyResponse response = service.create(1L, request);

        assertThat(response.slug()).isEqualTo("acme-2");
        assertThat(response.ownerUserId()).isEqualTo(1L);
    }
}
