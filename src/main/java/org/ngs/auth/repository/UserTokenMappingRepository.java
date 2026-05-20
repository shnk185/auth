package org.ngs.auth.repository;

import org.ngs.auth.entity.UserTokenMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTokenMappingRepository extends JpaRepository<UserTokenMappingEntity, Long> {
    UserTokenMappingEntity findByUserIdAndRevokedAtNull(Long userId);
}
