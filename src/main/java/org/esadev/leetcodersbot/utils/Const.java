package org.esadev.leetcodersbot.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Const {
	public static final String DECLINE_RESPONSE_MESSAGE = "Ти відмовився від кави 😢";
	public static final String APPROVE_RESPONSE_MESSAGE = "Тебе чекає кавуся ☕️";
	public static final String OLD_COFFEE_POLL_RESPONSE = "Це опитування вже неактуальне ((";
	public static final String ONLINE_COFFEE_TEXT = "Запрошую вас всіх приєднатись до онлайн кави!🧑‍💻☕️👩‍💻\nДавайте знайомитись!";
	public static final String TO_PARTICIPATE_TEXT = "П'ю каву!";
	public static final String ONLINE_COFFEE_RULES_CALLBACK = "online_coffee_rules";
	public static final String ONLINE_COFFEE_PARTICIPATE_CALLBACK = "online_coffee_participate_";
	public static final String RULES_BUTTON_TEXT = "Правила";
	public static final String HERE_ARE_OUR_PEOPLE = "Смачної кави та цікавої розмови:\n\n";
	public static final String LINE_BREAK = "\n";
	public static final String AT_SIGN = "@";
	public static final String RULES_ARE_SIMPLE = """
			Візьми участь в онлайн каві.\s
			В кінці дня бот автоматично обере пари людей, які підуть на каву.
			Ваша задача - це домовитись за формат зустрічі (Наприклад zoom, 20 хвилин зустріч)""";

}
