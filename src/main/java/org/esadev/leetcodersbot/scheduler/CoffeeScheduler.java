package org.esadev.leetcodersbot.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.esadev.leetcodersbot.bot.LeetcodersFriendBot;
import org.esadev.leetcodersbot.entity.OnlineCoffeeEntity;
import org.esadev.leetcodersbot.entity.UserEntity;
import org.esadev.leetcodersbot.props.BotProps;
import org.esadev.leetcodersbot.repository.OnlineCoffeeRepository;
import org.esadev.leetcodersbot.service.PairingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.esadev.leetcodersbot.creator.TelegramObjectCreator.createInlineKeyboardButton;
import static org.esadev.leetcodersbot.creator.TelegramObjectCreator.createInlineKeyboardRow;
import static org.esadev.leetcodersbot.utils.Const.HERE_ARE_OUR_PEOPLE;
import static org.esadev.leetcodersbot.utils.Const.LINE_BREAK;
import static org.esadev.leetcodersbot.utils.Const.ONLINE_COFFEE_PARTICIPATE_CALLBACK;
import static org.esadev.leetcodersbot.utils.Const.ONLINE_COFFEE_RULES_CALLBACK;
import static org.esadev.leetcodersbot.utils.Const.ONLINE_COFFEE_TEXT;
import static org.esadev.leetcodersbot.utils.Const.RULES_BUTTON_TEXT;
import static org.esadev.leetcodersbot.utils.Const.TO_PARTICIPATE_TEXT;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoffeeScheduler {
	private final OnlineCoffeeRepository onlineCoffeeRepository;
	private final LeetcodersFriendBot leetcodersFriendBot;
	private final BotProps botProps;
	private final PairingService pairingService;

	@Scheduled(cron = "0 0 5 * * TUE")
	public void startCoffeePoll() throws TelegramApiException {
		String participateCallback = ONLINE_COFFEE_PARTICIPATE_CALLBACK + LocalDate.now();
		InlineKeyboardButton participateButton = createInlineKeyboardButton(TO_PARTICIPATE_TEXT, participateCallback);
		InlineKeyboardButton rulesButton = createInlineKeyboardButton(RULES_BUTTON_TEXT, ONLINE_COFFEE_RULES_CALLBACK);

		SendMessage sendMessage = new SendMessage(botProps.chatId(), ONLINE_COFFEE_TEXT);
		sendMessage.setReplyMarkup(InlineKeyboardMarkup.builder().keyboardRow(createInlineKeyboardRow(List.of(participateButton, rulesButton))).build());

		OnlineCoffeeEntity entity = new OnlineCoffeeEntity();
		entity.setDate(LocalDate.now());
		entity.setIsActive(true);
		entity.setCoffeeName(participateCallback);

		Message sentMessage = leetcodersFriendBot.getTelegramClient().execute(sendMessage);
		entity.setMessageId(sentMessage.getMessageId());
		onlineCoffeeRepository.save(entity);

		leetcodersFriendBot.getTelegramClient().execute(PinChatMessage.builder()
				.messageId(sentMessage.getMessageId())
				.chatId(sentMessage.getChatId())
				.build());
	}

	@Scheduled(cron = "0 0 20 * * TUE")
	@Transactional
	public void finishCoffeePoll() throws TelegramApiException {
		Optional<OnlineCoffeeEntity> lastActiveOnlineCoffee = onlineCoffeeRepository.getFirstOnlineCoffeeEntityByIsActiveTrueOrderByDateDesc();

		if (lastActiveOnlineCoffee.isPresent() && !lastActiveOnlineCoffee.get().getUsers().isEmpty()) {
			OnlineCoffeeEntity onlineCoffeeEntity = lastActiveOnlineCoffee.get();
			try {
				leetcodersFriendBot.getTelegramClient().execute(UnpinChatMessage.builder()
						.chatId(botProps.chatId())
						.messageId(onlineCoffeeEntity.getMessageId())
						.build());
			} catch (Exception e) {
				log.error("Error while unpinning message {}", onlineCoffeeEntity.getMessageId(), e);
			}


			List<UserEntity> users = onlineCoffeeEntity.getUsers();
			Collections.shuffle(users);
			List<List<UserEntity>> pairs = pairingService.generateUniquePairs(users);
			StringBuilder builder = new StringBuilder();
			builder.append(HERE_ARE_OUR_PEOPLE);
			pairs.forEach(pair -> {
				pair.forEach(user -> builder
						.append(StringUtils.trim(StringUtils.defaultString(user.getFirstName()) + " " + StringUtils.defaultString(user.getLastName())))
						.append(" ")
						.append(user.getUsername())
						.append(LINE_BREAK)
				);
				builder.append(LINE_BREAK);
			});
			leetcodersFriendBot.getTelegramClient().execute(new SendMessage(botProps.chatId(), builder.toString()));
			pairingService.savePairingHistory(pairs, onlineCoffeeEntity.getDate().atStartOfDay());
		}
		lastActiveOnlineCoffee.ifPresent(onlineCoffeeEntity -> onlineCoffeeEntity.setIsActive(Boolean.FALSE));

	}


}
