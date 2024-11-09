package org.esadev.leetcodersbot.service;

import lombok.RequiredArgsConstructor;
import org.esadev.leetcodersbot.entity.PairingHistory;
import org.esadev.leetcodersbot.entity.UserEntity;
import org.esadev.leetcodersbot.repository.PairingHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PairingService {

	private final PairingHistoryRepository pairingHistoryRepository;

	public List<List<UserEntity>> generateUniquePairs(List<UserEntity> users) {
		List<List<UserEntity>> pairs = new ArrayList<>();
		Set<UserEntity> processedUsers = new HashSet<>();
		Map<UserEntity, Set<UserEntity>> pairingHistory = loadPairingHistoryMap(users);

		for (UserEntity user : users) {
			if (processedUsers.contains(user)) continue;

			Set<UserEntity> previousPartners = pairingHistory.getOrDefault(user, new HashSet<>());
			UserEntity bestPartner = users.stream()
					.filter(potentialPartner -> !processedUsers.contains(potentialPartner) && !previousPartners.contains(potentialPartner) && !potentialPartner.equals(user))
					.findFirst()
					.orElseGet(() -> users.stream()
							.filter(p -> !processedUsers.contains(p) && !p.equals(user))
							.findFirst()
							.orElse(null));

			if (bestPartner != null) {
				pairs.add(Arrays.asList(user, bestPartner));
				processedUsers.add(user);
				processedUsers.add(bestPartner);
				pairingHistory.computeIfAbsent(user, k -> new HashSet<>()).add(bestPartner);
				pairingHistory.computeIfAbsent(bestPartner, k -> new HashSet<>()).add(user);
			}
		}

		if (users.size() > processedUsers.size()) {
			users.stream().filter(u -> !processedUsers.contains(u)).findFirst()
					.ifPresent(u -> pairs.set(pairs.size() - 1, List.of(pairs.getLast().getFirst(), pairs.getLast().get(1), u)));

		}

		return pairs;
	}

	public void savePairingHistory(List<List<UserEntity>> pairs, LocalDateTime meetingDate) {
		for (List<UserEntity> pair : pairs) {
			UserEntity user1 = pair.get(0);
			UserEntity user2 = pair.get(1);
			if (user1.getId() > user2.getId()) {
				UserEntity temp = user1;
				user1 = user2;
				user2 = temp;
			}

			PairingHistory history = new PairingHistory(user1, user2, meetingDate);
			pairingHistoryRepository.save(history);
		}
	}


	private Map<UserEntity, Set<UserEntity>> loadPairingHistoryMap(List<UserEntity> users) {
		Map<UserEntity, Set<UserEntity>> pairingHistory = new HashMap<>();
		for (UserEntity user : users) {
			List<PairingHistory> history = pairingHistoryRepository.findByUser1OrUser2(user, user);
			Set<UserEntity> partners = new HashSet<>();
			for (PairingHistory record : history) {
				if (record.getUser1().equals(user)) {
					partners.add(record.getUser2());
				} else {
					partners.add(record.getUser1());
				}
			}
			pairingHistory.put(user, partners);
		}
		return pairingHistory;
	}
}
