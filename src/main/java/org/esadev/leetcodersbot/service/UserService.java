package org.esadev.leetcodersbot.service;

import lombok.RequiredArgsConstructor;
import org.esadev.leetcodersbot.entity.UserEntity;
import org.esadev.leetcodersbot.model.TelegramUser;
import org.esadev.leetcodersbot.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;

	public List<TelegramUser> getAllUsers() {
		return userRepository.findAllUsers();
	}

	@Transactional
	public void updateUser(TelegramUser user) {
		userRepository.findById(user.getUserId()).ifPresent(userEntity -> {
			userEntity.setFirstName(user.getFirstName());
			userEntity.setLastName(user.getLastName());
			userEntity.setUsername(user.getLogin());
		});
	}

	public void saveUser(TelegramUser telegramUser) {
		UserEntity userEntity = new UserEntity();
		userEntity.setId(telegramUser.getUserId());
		userEntity.setUsername(telegramUser.getLogin());
		userEntity.setFirstName(telegramUser.getFirstName());
		userEntity.setLastName(telegramUser.getLastName());
		userEntity.setUsername(telegramUser.getLogin());
		userRepository.save(userEntity);
	}
}
