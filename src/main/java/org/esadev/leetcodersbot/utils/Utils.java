package org.esadev.leetcodersbot.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.IntStream;

import static org.esadev.leetcodersbot.utils.Const.AT_SIGN;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {

	public static List<String> splitText(String text, int maxLength) {
		int length = text.length();

		return IntStream
				.iterate(0, i -> i < length, i -> i + maxLength).mapToObj(i -> text.substring(i, Math.min(length, i + maxLength)))
				.toList();
	}

	public static String formatUserName(String firstName, String lastName, String userName, String id) {
		if (StringUtils.isEmpty(userName)) {
			String fullName = StringUtils.defaultString(firstName) + " " + StringUtils.defaultString(lastName);
			String customName = fullName.trim().isEmpty() ? id : fullName.trim();
			return ("[%s](tg://user?id=%s)").formatted(customName, id);
		}
		return AT_SIGN + userName;
	}
}
