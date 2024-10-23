package org.esadev.leetcodersbot.telegram.command;


import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public interface TelegramCommand {
	boolean supports(Update update);

	void execute(Update update, TelegramClient client);
}
