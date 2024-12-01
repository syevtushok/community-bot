package org.esadev.leetcodersbot.telegram.command;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.esadev.leetcodersbot.creator.TelegramObjectCreator;
import org.esadev.leetcodersbot.entity.OnlineCoffeeEntity;
import org.esadev.leetcodersbot.entity.UserEntity;
import org.esadev.leetcodersbot.repository.OnlineCoffeeRepository;
import org.esadev.leetcodersbot.service.UserService;
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

import static org.esadev.leetcodersbot.creator.TelegramObjectCreator.createAnswerCallbackQuery;
import static org.esadev.leetcodersbot.creator.TelegramObjectCreator.createEditMessageText;
import static org.esadev.leetcodersbot.creator.TelegramObjectCreator.createInlineKeyboardButton;
import static org.esadev.leetcodersbot.utils.Const.APPROVE_RESPONSE_MESSAGE;
import static org.esadev.leetcodersbot.utils.Const.DECLINE_RESPONSE_MESSAGE;
import static org.esadev.leetcodersbot.utils.Const.LINE_BREAK;
import static org.esadev.leetcodersbot.utils.Const.OLD_COFFEE_POLL_RESPONSE;
import static org.esadev.leetcodersbot.utils.Const.ONLINE_COFFEE_PARTICIPATE_CALLBACK;
import static org.esadev.leetcodersbot.utils.Const.ONLINE_COFFEE_RULES_CALLBACK;
import static org.esadev.leetcodersbot.utils.Const.ONLINE_COFFEE_TEXT;
import static org.esadev.leetcodersbot.utils.Const.RULES_BUTTON_TEXT;
import static org.esadev.leetcodersbot.utils.Const.TO_PARTICIPATE_TEXT;

@Slf4j
@RequiredArgsConstructor
@Component
public class OnlineCoffeeParticipateCallback implements TelegramCommand {
	private final OnlineCoffeeRepository onlineCoffeeRepository;
	private final UserService userService;

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
					StringBuilder messageText = new StringBuilder(ONLINE_COFFEE_TEXT).append(LINE_BREAK);
					Optional<UserEntity> first = onlineCoffeeEntity.getUsers().stream().filter(user -> user.getId().equals(fromUser.getId())).findFirst();
					if (first.isPresent()) {
						log.info("User {} declined coffee", fromUser.getId());
						responseMessage = DECLINE_RESPONSE_MESSAGE;
						onlineCoffeeEntity.getUsers().remove(first.get());
						onlineCoffeeEntity.getUsers().forEach(user -> messageText.append(user.getUsername()).append(LINE_BREAK));
					} else {
						log.info("User {} agreed to coffee", fromUser.getId());
						responseMessage = APPROVE_RESPONSE_MESSAGE;
						userService.saveNewUser(fromUser, onlineCoffeeEntity);
						onlineCoffeeEntity.getUsers().forEach(user -> messageText.append(user.getUsername()).append(LINE_BREAK));
					}
					answerForPoll(update, client, message, messageText.toString(), responseMessage);
				} else {
					client.execute(TelegramObjectCreator.createAnswerCallbackQuery(update.getCallbackQuery().getId(), OLD_COFFEE_POLL_RESPONSE));
				}
			} else {
				client.execute(createAnswerCallbackQuery(update.getCallbackQuery().getId(), OLD_COFFEE_POLL_RESPONSE));
			}
		}
	}


	private void answerForPoll(Update update, TelegramClient client, Message message, String messageText, String responseMessage) throws TelegramApiException {
		InlineKeyboardButton participateButton = createInlineKeyboardButton(TO_PARTICIPATE_TEXT, ONLINE_COFFEE_PARTICIPATE_CALLBACK + LocalDate.now());
		InlineKeyboardButton rulesButton = createInlineKeyboardButton(RULES_BUTTON_TEXT, ONLINE_COFFEE_RULES_CALLBACK);
		EditMessageText editMessageText = createEditMessageText(message.getChatId(), messageText, message.getMessageId());
		editMessageText.setReplyMarkup(InlineKeyboardMarkup
				.builder()
				.keyboardRow(TelegramObjectCreator.createInlineKeyboardRow(List.of(participateButton, rulesButton)))
				.build());
		editMessageText.enableMarkdown(true);
		client.execute(editMessageText);
		client.execute(createAnswerCallbackQuery(update.getCallbackQuery().getId(), responseMessage));
	}
}
