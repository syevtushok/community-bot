package org.esadev.leetcodersbot.repository;

import org.esadev.leetcodersbot.entity.OnlineCoffeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OnlineCoffeeRepository extends JpaRepository<OnlineCoffeeEntity, Long> {

	Optional<OnlineCoffeeEntity> getFirstOnlineCoffeeEntityByIsActiveTrueOrderByDateDesc();
}
