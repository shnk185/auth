package org.ngs.auth.repository;

import org.ngs.auth.entity.ServiceAuthConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceAuthConfigRepository extends JpaRepository<ServiceAuthConfigEntity, Long> {
}
