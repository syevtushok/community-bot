package org.esadev.leetcodersbot.telegram.command;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.esadev.leetcodersbot.creator.TelegramObjectCreator;
import org.esadev.leetcodersbot.entity.OnlineCoffeeEntity;
import org.esadev.leetcodersbot.entity.UserEntity;
import org.esadev.leetcodersbot.repository.OnlineCoffeeRepository;
import org.esadev.leetcodersbot.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.esadev.leetcodersbot.creator.TelegramObjectCreator.createAnswerCallbackQuery;
import static org.esadev.leetcodersbot.creator.TelegramObjectCreator.createEditMessageText;
import static org.esadev.leetcodersbot.creator.TelegramObjectCreator.createInlineKeyboardButton;
import static org.esadev.leetcodersbot.utils.Const.APPROVE_RESPONSE_MESSAGE;
import static org.esadev.leetcodersbot.utils.Const.AT_SIGN;
import static org.esadev.leetcodersbot.utils.Const.DECLINE_RESPONSE_MESSAGE;
import static org.esadev.leetcodersbot.utils.Const.OLD_COFFEE_POLL_RESPONSE;
import static org.esadev.leetcodersbot.utils.Const.ONLINE_COFFEE_PARTICIPATE_CALLBACK;
import static org.esadev.leetcodersbot.utils.Const.ONLINE_COFFEE_RULES_CALLBACK;
import static org.esadev.leetcodersbot.utils.Const.RULES_BUTTON_TEXT;
import static org.esadev.leetcodersbot.utils.Const.TO_PARTICIPATE_TEXT;

@RequiredArgsConstructor
@Component
public class OnlineCoffeeParticipateCallback implements TelegramCommand {
	private final OnlineCoffeeRepository onlineCoffeeRepository;
	private final UserRepository userRepository;

	@Override
	public boolean supports(Update update) {
		return update.hasCallbackQuery()
				&& update.getCallbackQuery().getData() != null
				&& update.getCallbackQuery().getData().startsWith(ONLINE_COFFEE_PARTICIPATE_CALLBACK);
	}

	@SneakyThrows
	@Override
	@Transactional
	public void execute(Update update, TelegramClient client) {
		Optional<OnlineCoffeeEntity> onlineCoffee = onlineCoffeeRepository.getFirstOnlineCoffeeEntityByIsActiveTrueOrderByDateDesc();

		if (onlineCoffee.isPresent()) {
			OnlineCoffeeEntity onlineCoffeeEntity = onlineCoffee.get();
			if (onlineCoffeeEntity.getCoffeeName().equals(update.getCallbackQuery().getData())) {
				if (update.getCallbackQuery().getMessage() instanceof Message message) {
					User fromUser = update.getCallbackQuery().getFrom();
					String responseMessage;
					String messageText = message.getText();
					Optional<UserEntity> first = onlineCoffeeEntity.getUsers().stream().filter(user -> user.getId().equals(fromUser.getId())).findFirst();
					if (first.isPresent()) {
						responseMessage = DECLINE_RESPONSE_MESSAGE;
						messageText = messageText.replace(formatUserName(fromUser), EMPTY);
						onlineCoffeeEntity.getUsers().remove(first.get());
					} else {
						messageText = "%s\n%s".formatted(messageText, formatUserName(fromUser));
						responseMessage = APPROVE_RESPONSE_MESSAGE;
						userRepository.findById(fromUser.getId()).ifPresentOrElse(user -> onlineCoffeeEntity.getUsers().add(user),
								() -> {
									UserEntity user = new UserEntity();
									user.setId(fromUser.getId());
									user.setFirstName(fromUser.getFirstName());
									user.setLastName(fromUser.getLastName());
									user.setUsername(formatUserName(fromUser));
									user.setOnlineCoffees(List.of(onlineCoffeeEntity));
									UserEntity save = userRepository.save(user);
									onlineCoffeeEntity.getUsers().add(save);
								});
					}
					answerForPoll(update, client, message, messageText, responseMessage);
				} else {
					client.execute(TelegramObjectCreator.createAnswerCallbackQuery(update.getCallbackQuery().getId(), OLD_COFFEE_POLL_RESPONSE));
				}
			} else {
				client.execute(createAnswerCallbackQuery(update.getCallbackQuery().getId(), OLD_COFFEE_POLL_RESPONSE));
			}
		}
	}

	private String formatUserName(User fromUser) {
		if (fromUser.getUserName() == null) {
			return "[%s](tg://user?id=%s)".formatted(fromUser.getFirstName() + StringUtils.defaultString(fromUser.getLastName()), fromUser.getId());
		}
		return AT_SIGN + fromUser.getUserName();
	}

	private void answerForPoll(Update update, TelegramClient client, Message message, String messageText, String responseMessage) throws TelegramApiException {
		InlineKeyboardButton participateButton = createInlineKeyboardButton(TO_PARTICIPATE_TEXT, ONLINE_COFFEE_PARTICIPATE_CALLBACK + LocalDate.now());
		InlineKeyboardButton rulesButton = createInlineKeyboardButton(RULES_BUTTON_TEXT, ONLINE_COFFEE_RULES_CALLBACK);
		EditMessageText editMessageText = createEditMessageText(message.getChatId(), messageText, message.getMessageId());
		editMessageText.setReplyMarkup(InlineKeyboardMarkup
				.builder()
				.keyboardRow(TelegramObjectCreator.createInlineKeyboardRow(List.of(participateButton, rulesButton)))
				.build());
		client.execute(editMessageText);
		client.execute(createAnswerCallbackQuery(update.getCallbackQuery().getId(), responseMessage));
	}
}
