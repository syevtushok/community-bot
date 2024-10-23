package org.esadev.leetcodersbot.repository;

import org.esadev.leetcodersbot.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
