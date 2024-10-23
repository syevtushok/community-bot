package org.esadev.leetcodersbot.telegram.command;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.esadev.leetcodersbot.creator.TelegramObjectCreator.createSendMessage;

@Component
public class ChatMemberUpdatedCommand implements TelegramCommand {
	@Override
	public boolean supports(Update update) {
		return update.getMyChatMember() != null;
	}

	@SneakyThrows
	@Override
	public void execute(Update update, TelegramClient client) {
		SendMessage sendMessage = createSendMessage(update.getMyChatMember().getChat().getId(), update.getMyChatMember().getChat().getId().toString());
		client.execute(sendMessage);
	}
}
