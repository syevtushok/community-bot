package org.esadev.leetcodersbot.service;

import lombok.RequiredArgsConstructor;
import org.esadev.leetcodersbot.entity.OnlineCoffeeEntity;
import org.esadev.leetcodersbot.entity.UserEntity;
import org.esadev.leetcodersbot.model.TelegramUser;
import org.esadev.leetcodersbot.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

import static org.esadev.leetcodersbot.utils.Utils.formatUserName;

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

	public void saveTelegramUser(TelegramUser telegramUser) {
		UserEntity userEntity = new UserEntity();
		userEntity.setId(telegramUser.getUserId());
		userEntity.setUsername(telegramUser.getLogin());
		userEntity.setFirstName(telegramUser.getFirstName());
		userEntity.setLastName(telegramUser.getLastName());
		userEntity.setUsername(telegramUser.getLogin());
		userRepository.save(userEntity);
	}

	public void saveNewUser(User fromUser, OnlineCoffeeEntity onlineCoffeeEntity) {
		userRepository.findById(fromUser.getId()).ifPresentOrElse(user -> onlineCoffeeEntity.getUsers().add(user),
				() -> {
					UserEntity user = new UserEntity();
					user.setId(fromUser.getId());
					user.setFirstName(fromUser.getFirstName());
					user.setLastName(fromUser.getLastName());
					user.setUsername(formatUserName(fromUser.getFirstName(), fromUser.getLastName(), fromUser.getUserName(), fromUser.getId().toString()));
					user.setOnlineCoffees(List.of(onlineCoffeeEntity));
					UserEntity save = userRepository.save(user);
					onlineCoffeeEntity.getUsers().add(save);
				});
	}
}
