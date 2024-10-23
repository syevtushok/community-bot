package org.esadev.leetcodersbot;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.esadev.leetcodersbot.props.BotProps;
import org.esadev.leetcodersbot.telegram.command.TelegramCommand;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Slf4j
@Component
public class LeetcodersFriendBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
	private final List<TelegramCommand> commands;
	@Getter
	private final TelegramClient telegramClient;
	private final BotProps botProps;

	public LeetcodersFriendBot(List<TelegramCommand> commands, BotProps botProps) {
		this.commands = commands;
		this.botProps = botProps;
		telegramClient = new OkHttpTelegramClient(getBotToken());
	}


	@Override
	public String getBotToken() {
		return botProps.token();
	}

	@Override
	public LongPollingUpdateConsumer getUpdatesConsumer() {
		return this;
	}

	@Override
	public void consume(Update update) {
		try {
			commands.stream()
					.filter(telegramCommand -> telegramCommand.supports(update))
					.forEach(telegramCommand -> telegramCommand.execute(update, telegramClient));
		} catch (Exception e) {
			log.error("Something went wrong. Update {}", update, e);
		}
	}


}