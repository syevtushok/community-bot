package org.esadev.leetcodersbot.service;

import lombok.RequiredArgsConstructor;
import org.esadev.leetcodersbot.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.esadev.leetcodersbot.entity.UserEntity;

import java.util.*;

import static org.esadev.leetcodersbot.utils.Utils.splitList;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<List<UserEntity>> createPairs(List<UserEntity> users) {
        List<List<UserEntity>> groups = createGroupsWithoutDuplicate(users);
        saveGroups(groups);
        return groups;
    }

    private List<List<UserEntity>> createGroupsWithoutDuplicate(List<UserEntity> users) {
        int maxAttempts = 5;
        int attempt = 0;
        while (attempt < maxAttempts) {
            Collections.shuffle(users);
            List<List<UserEntity>> interlocutorsGroups = splitList(users);
            if (areGroupsValid(interlocutorsGroups)) {
                return interlocutorsGroups;
            }
            attempt++;
        }
        return createGroupsWithMinimalDuplicate(users);
    }

    private boolean areGroupsValid(List<List<UserEntity>> users) {
        for (List<UserEntity> group : users) {
            if (!isGroupValid(group)) {
                return false;
            }
        }
        return true;
    }

    private boolean isGroupValid(List<UserEntity> users) {
        if (users.size() == 2) {
            UserEntity user1 = users.get(0);
            UserEntity user2 = users.get(1);
            return !werePreviouslyPaired(user1, user2);
        } else if (users.size() == 3) {
            UserEntity user1 = users.get(0);
            UserEntity user2 = users.get(1);
            UserEntity user3 = users.get(2);
            return !werePreviouslyPaired(user1, user2) &&
                    !werePreviouslyPaired(user2, user3) &&
                    !werePreviouslyPaired(user3, user1);
        }
        return true;
    }

    private boolean werePreviouslyPaired(UserEntity user1, UserEntity user2) {
        return (user1.getInterlocutor() != null &&
                user1.getInterlocutor().equals(user2.getUsername())) ||
                (user2.getInterlocutor() != null &&
                        user2.getInterlocutor().equals(user1.getUsername()));
    }

    private List<List<UserEntity>> createGroupsWithMinimalDuplicate(List<UserEntity> users) {
        List<List<UserEntity>> bestGroups = null;
        int minDuplicate = Integer.MAX_VALUE;
        int maxAttempts = 10;
        for (int i = 0; i < maxAttempts; i++) {
            Collections.shuffle(users);
            List<List<UserEntity>> candidateGroups = splitList(users);
            int conflicts = countDuplicate(candidateGroups);
            if (conflicts < minDuplicate) {
                minDuplicate = conflicts;
                bestGroups = candidateGroups;

                if (conflicts == 0) {
                    break;
                }
            }
        }
        return bestGroups;
    }

    private int countDuplicate(List<List<UserEntity>> users) {
        int duplicates = 0;
        for (List<UserEntity> group : users) {
            if (group.size() == 2) {
                if (werePreviouslyPaired(group.get(0), group.get(1))) {
                    duplicates++;
                }
            } else if (group.size() == 3) {
                if (werePreviouslyPaired(group.get(0), group.get(1))) duplicates++;
                if (werePreviouslyPaired(group.get(1), group.get(2))) duplicates++;
                if (werePreviouslyPaired(group.get(2), group.get(0))) duplicates++;
            }
        }
        return duplicates;
    }

    private void saveGroups(List<List<UserEntity>> users) {
        for (List<UserEntity> group : users) {
            if (group.size() == 2) {
                UserEntity user1 = group.get(0);
                UserEntity user2 = group.get(1);

                user1.setInterlocutor(user2.getUsername());
                user2.setInterlocutor(user1.getUsername());

                userRepository.save(user1);
                userRepository.save(user2);
            } else if (group.size() == 3) {
                UserEntity user1 = group.get(0);
                UserEntity user2 = group.get(1);
                UserEntity user3 = group.get(2);

                user1.setInterlocutor(user2.getUsername());
                user2.setInterlocutor(user3.getUsername());
                user3.setInterlocutor(user1.getUsername());

                userRepository.save(user1);
                userRepository.save(user2);
                userRepository.save(user3);
            }
        }
    }
}