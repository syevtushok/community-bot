package org.esadev.leetcodersbot.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.esadev.leetcodersbot.bot.LeetcodersFriendBot;
import org.esadev.leetcodersbot.creator.TelegramObjectCreator;
import org.esadev.leetcodersbot.model.AllByGroupRequest;
import org.esadev.leetcodersbot.model.TelegramUser;
import org.esadev.leetcodersbot.props.TelegramAdvancedProps;
import org.esadev.leetcodersbot.service.UserService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static org.esadev.leetcodersbot.utils.Const.MAX_MESSAGE_LENGTH;
import static org.esadev.leetcodersbot.utils.Utils.formatUserName;
import static org.esadev.leetcodersbot.utils.Utils.splitText;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserImportScheduler {
	private final TelegramAdvancedProps telegramAdvancedProps;
	private final UserService userService;
	private final LeetcodersFriendBot leetcodersFriendBot;

	private List<TelegramUser> filterUsers(List<TelegramUser> sourceUsers, List<TelegramUser> actualUserIds) {
		return sourceUsers.stream()
				.filter(sourceUser -> !actualUserIds.contains(sourceUser))
				.toList();
	}

	@Scheduled(cron = "0 0 5 * * MON")
	public void importUsers() throws TelegramApiException {
		log.info("Updating users");
		StringBuilder reportMessage = new StringBuilder("Statistics import users:\n");
		List<TelegramUser> actualUsers = RestClient.create(telegramAdvancedProps.allUsersInGroupUrl())
				.post()
				.body(AllByGroupRequest.builder().chat(telegramAdvancedProps.chatName()).build())
				.retrieve()
				.body(new ParameterizedTypeReference<>() {
				});

		if (actualUsers == null || actualUsers.isEmpty()) {
			reportMessage.append("No users found\n");
			leetcodersFriendBot.getTelegramClient().execute(TelegramObjectCreator.createSendMessage(telegramAdvancedProps.adminId(), reportMessage.toString()));
			log.error("No users found");
			return;
		}

		List<TelegramUser> existingUsers = userService.getAllUsers();

		handleDeleted(existingUsers, actualUsers, reportMessage);
		handleNewUsers(actualUsers, existingUsers, reportMessage);
		handleUpdatedUsers(reportMessage, actualUsers, existingUsers);
		sendReport(reportMessage.toString());
	}

	private void sendReport(String reportMessage) throws TelegramApiException {
		log.info(reportMessage);
		for (String text : splitText(reportMessage, MAX_MESSAGE_LENGTH)) {
			SendMessage sendMessage = TelegramObjectCreator.createSendMessage(telegramAdvancedProps.adminId(), text);
			sendMessage.enableMarkdown(false);
			leetcodersFriendBot.getTelegramClient().execute(sendMessage);
		}
	}

	private void handleUpdatedUsers(StringBuilder reportMessage, List<TelegramUser> actualUsers, List<TelegramUser> existingUsers) {
		reportMessage.append("\n");
		actualUsers.forEach(actualUser -> existingUsers.stream().filter(telegramUser -> telegramUser.getUserId().equals(actualUser.getUserId())).findFirst().ifPresent(existingUser -> {
			StringBuilder updatedUserMessage = new StringBuilder();
			boolean updated = false;
			if (existingUser.getFirstName() != null && !existingUser.getFirstName().equals(actualUser.getFirstName())) {
				updated = true;
				updatedUserMessage.append("Id: ").append(actualUser.getUserId()).append(". First name updated from ").append(existingUser.getFirstName()).append(" to ").append(actualUser.getFirstName()).append("\n");
				existingUser.setFirstName(actualUser.getFirstName());
			} else if (existingUser.getLastName() != null && !existingUser.getLastName().equals(actualUser.getLastName())) {
				updated = true;
				updatedUserMessage.append("Id: ").append(actualUser.getUserId()).append(". Last name updated from ").append(existingUser.getLastName()).append(" to ").append(actualUser.getLastName()).append("\n");
				existingUser.setLastName(actualUser.getLastName());
			} else {
				actualUser.setLogin(formatUserName(actualUser.getFirstName(), actualUser.getLastName(), actualUser.getLogin(), actualUser.getUserId().toString()));
				if (!actualUser.getLogin().equals(existingUser.getLogin())) {
					updated = true;
					updatedUserMessage.append("Id: ").append(actualUser.getUserId()).append(". Username updated from ").append(existingUser.getLogin()).append(" to ").append(actualUser.getLogin()).append("\n");
					existingUser.setLogin(actualUser.getLogin());
				}
			}
			if (updated) {
				userService.updateUser(existingUser);
				log.info(updatedUserMessage.toString());
			}

			reportMessage.append(updatedUserMessage);
		}));
	}

	private void handleNewUsers(List<TelegramUser> actualUsers, List<TelegramUser> existingUsers, StringBuilder reportMessage) {
		List<TelegramUser> newUsers = filterUsers(actualUsers, existingUsers);
		if (!newUsers.isEmpty()) {
			reportMessage.append("New users: ").append(newUsers.size()).append("\n");
			formatUsers(reportMessage, newUsers);
			newUsers.forEach(userService::saveUser);
		}
	}

	private void handleDeleted(List<TelegramUser> existingUsers, List<TelegramUser> actualUsers, StringBuilder reportMessage) {
		List<TelegramUser> deletedUsers = filterUsers(existingUsers, actualUsers);

		if (!deletedUsers.isEmpty()) {
			reportMessage.append("Deleted users: ").append(deletedUsers.size()).append("\n");
			formatUsers(reportMessage, deletedUsers);
		}
	}

	private void formatUsers(StringBuilder reportMessage, List<TelegramUser> telegramUsers) {
		for (int i = 0; i < telegramUsers.size(); i++) {
			TelegramUser telegramUser = telegramUsers.get(i);
			telegramUser.setLogin(formatUserName(telegramUser.getFirstName(), telegramUser.getLastName(), telegramUser.getLogin(), telegramUser.getUserId().toString()));
			reportMessage.append(i + 1)
					.append(". ")
					.append(telegramUser.getUserId())
					.append(" ")
					.append(telegramUser.getFirstName())
					.append(" ")
					.append(telegramUser.getLastName())
					.append(" ")
					.append(telegramUser.getLogin())
					.append("\n");
		}
	}
}

