package org.esadev.leetcodersbot.creator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TelegramObjectCreator {

	public static InlineKeyboardRow createInlineKeyboardRow(List<InlineKeyboardButton> buttons) {

		return new InlineKeyboardRow(buttons);
	}

	public static InlineKeyboardButton createInlineKeyboardButton(String text, String callbackData) {
		return InlineKeyboardButton
				.builder()
				.text(text)
				.callbackData(callbackData)
				.build();
	}

	public static AnswerCallbackQuery createAnswerCallbackQuery(String callbackQueryId, String text) {
		return AnswerCallbackQuery.builder()
				.callbackQueryId(callbackQueryId)
				.text(text)
				.build();

	}

	public static EditMessageText createEditMessageText(Long chatId, String text, Integer messageId) {
		return EditMessageText.builder()
				.messageId(messageId)
				.text(text)
				.chatId(chatId)
				.parseMode(ParseMode.MARKDOWN)
				.build();
	}

	public static SendMessage createSendMessage(Long chatId, String text) {
		return SendMessage.builder()
				.chatId(chatId)
				.parseMode(ParseMode.MARKDOWN)
				.text(text)
				.build();
	}
}
