package org.esadev.leetcodersbot.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.esadev.leetcodersbot.entity.UserEntity;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {
	public static List<List<UserEntity>> splitList(List<UserEntity> input) {
		List<List<UserEntity>> result = new ArrayList<>();
		int i = 0;

		while (i < input.size() - 1) {
			result.add(new ArrayList<>(input.subList(i, i + 2)));
			i += 2;
		}

		if (i < input.size()) {
			if (input.size() == 1) {
				result.add(new ArrayList<>(input.subList(0, 1)));
			} else {
				result.get(result.size() - 1).add(input.get(i));
			}
		}

		return result;
	}
}