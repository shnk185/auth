package org.ngs.auth.repository;

import org.ngs.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByUserNameAndDeletedFalse(String userName);

    Optional<UserEntity> findByIdAndDeletedFalse(Long userId);
}
