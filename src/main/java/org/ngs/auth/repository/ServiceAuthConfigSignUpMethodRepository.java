package org.ngs.auth.repository;

import org.ngs.auth.entity.ServiceAuthConfigSignUpMethodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceAuthConfigSignUpMethodRepository extends JpaRepository<ServiceAuthConfigSignUpMethodEntity, Long> {
}
