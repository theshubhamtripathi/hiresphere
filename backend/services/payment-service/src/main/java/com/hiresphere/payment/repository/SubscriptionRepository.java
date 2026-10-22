package com.hiresphere.payment.repository;

import com.hiresphere.payment.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByCompanyId(Long companyId);
}
