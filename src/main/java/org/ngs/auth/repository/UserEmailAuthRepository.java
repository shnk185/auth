package org.ngs.auth.repository;

import org.ngs.auth.entity.UserEmailAuthEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEmailAuthRepository extends JpaRepository<UserEmailAuthEntity, Long> {
}
