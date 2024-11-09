package org.esadev.leetcodersbot.repository;

import org.esadev.leetcodersbot.entity.PairingHistory;
import org.esadev.leetcodersbot.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PairingHistoryRepository extends JpaRepository<PairingHistory, Long> {
	List<PairingHistory> findByUser1OrUser2(UserEntity user1, UserEntity user2);
}
