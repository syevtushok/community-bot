package org.esadev.leetcodersbot.controller;

import lombok.RequiredArgsConstructor;
import org.esadev.leetcodersbot.scheduler.UserImportScheduler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

	private final UserImportScheduler userImportScheduler;

	@GetMapping("/import")
	public String importUsers() throws TelegramApiException {
		userImportScheduler.importUsers();
		return "Users imported";
	}
}
