package org.esadev.leetcodersbot.props;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bot")
public record BotProps(String token, String chatId) {
}
