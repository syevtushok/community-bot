package org.esadev.leetcodersbot.telegram.command;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.esadev.leetcodersbot.utils.Const.ONLINE_COFFEE_RULES_CALLBACK;
import static org.esadev.leetcodersbot.utils.Const.RULES_ARE_SIMPLE;

@Component
public class OnlineCoffeeRulesCallback implements TelegramCommand {

	@Override
	public boolean supports(Update update) {
		return update.hasCallbackQuery()
				&& update.getCallbackQuery().getData() != null
				&& update.getCallbackQuery().getData().equals(ONLINE_COFFEE_RULES_CALLBACK);
	}

	@SneakyThrows
	@Override
	public void execute(Update update, TelegramClient client) {
		AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(update.getCallbackQuery().getId());
		answerCallbackQuery.setShowAlert(true);
		answerCallbackQuery.setText(RULES_ARE_SIMPLE);
		client.execute(answerCallbackQuery);

	}
}
