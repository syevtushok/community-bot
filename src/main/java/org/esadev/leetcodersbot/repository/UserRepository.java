package org.esadev.leetcodersbot.repository;

import org.esadev.leetcodersbot.entity.UserEntity;
import org.esadev.leetcodersbot.model.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

	@Query("SELECT new org.esadev.leetcodersbot.model.TelegramUser(u.id, u.username, u.firstName, u.lastName) FROM UserEntity u")
	List<TelegramUser> findAllUsers();
}
