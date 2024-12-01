package org.esadev.leetcodersbot.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
	private final String specialCharsRegex = "[\\\\_*\\[\\]()~`><&#+\\-=|{}.!]";

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

		sanitizeUser(actualUsers);

		List<TelegramUser> existingUsers = userService.getAllUsers();

		handleDeleted(existingUsers, actualUsers, reportMessage);
		handleNewUsers(actualUsers, existingUsers, reportMessage);
		handleUpdatedUsers(reportMessage, actualUsers, existingUsers);
		sendReport(reportMessage.toString());
	}


	private void sanitizeUser(List<TelegramUser> actualUsers) {
		actualUsers.forEach(actualUser -> {
			actualUser.setFirstName(StringUtils.defaultString(actualUser.getFirstName()).replaceAll(specialCharsRegex, "\\\\$0"));
			actualUser.setLastName(StringUtils.defaultString(actualUser.getLastName()).replaceAll(specialCharsRegex, "\\\\$0"));
			actualUser.setLogin(formatUserName(actualUser.getFirstName(), actualUser.getLastName(), StringUtils.defaultString(actualUser.getLogin()).replaceAll(specialCharsRegex, "\\\\$0"), actualUser.getUserId().toString()));
		});
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
		Map<Long, TelegramUser> existingUsersMap = existingUsers.stream().collect(Collectors.toMap(TelegramUser::getUserId, Function.identity()));

		List<String> updatedMessages = new ArrayList<>();
		for (TelegramUser actualUser : actualUsers) {
			TelegramUser existingUser = existingUsersMap.get(actualUser.getUserId());
			if (existingUser != null) {
				StringBuilder updatedUserMessage = new StringBuilder();
				boolean updated = false;

				updated |= updateField("First name", existingUser.getFirstName(), actualUser.getFirstName(), updatedUserMessage, actualUser);
				updated |= updateField("Last name", existingUser.getLastName(), actualUser.getLastName(), updatedUserMessage, actualUser);
				updated |= updateField("Username", existingUser.getLogin(), actualUser.getLogin(), updatedUserMessage, actualUser);

				if (updated) {
					try {
						userService.updateUser(actualUser);
						updatedMessages.add(updatedUserMessage.toString());
					} catch (Exception e) {
						log.error("Failed to update user {}: {}", actualUser.getUserId(), e.getMessage(), e);
					}
				}
			}
		}

		updatedMessages.forEach(reportMessage::append);
	}

	private boolean updateField(String fieldName, String oldValue, String newValue, StringBuilder message, TelegramUser user) {
		if (!StringUtils.equals(oldValue, newValue)) {
			message.append("Id: ").append(user.getUserId()).append(". ").append(fieldName)
					.append(" updated from ").append(oldValue).append(" to ").append(newValue).append("\n");
			return true;
		}
		return false;
	}

	private void handleNewUsers(List<TelegramUser> actualUsers, List<TelegramUser> existingUsers, StringBuilder reportMessage) {
		List<TelegramUser> newUsers = filterUsers(actualUsers, existingUsers);
		if (!newUsers.isEmpty()) {
			reportMessage.append("New users: ").append(newUsers.size()).append("\n");
			formatUsers(reportMessage, newUsers);
			newUsers.forEach(userService::saveTelegramUser);
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

