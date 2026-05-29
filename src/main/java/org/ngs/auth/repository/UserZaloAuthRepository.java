package org.ngs.auth.repository;

import org.ngs.auth.entity.UserZaloAuthEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserZaloAuthRepository extends JpaRepository<UserZaloAuthEntity, Long> {

    UserZaloAuthEntity findByZaloUserId(String zaloUserId);
}
