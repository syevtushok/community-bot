package org.esadev.leetcodersbot.props;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram.advanced")
public record TelegramAdvancedProps(String chatName, String allUsersInGroupUrl, Long adminId) {
}
