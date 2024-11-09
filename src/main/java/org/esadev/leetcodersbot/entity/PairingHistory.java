package org.esadev.leetcodersbot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "PairingHistory", uniqueConstraints = @UniqueConstraint(columnNames = {"user1_id", "user2_id", "meeting_date"}))
public class PairingHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user1_id", nullable = false)
	private UserEntity user1;

	@ManyToOne
	@JoinColumn(name = "user2_id", nullable = false)
	private UserEntity user2;

	@Column(name = "meeting_date", nullable = false)
	private LocalDateTime meetingDate;


	public PairingHistory(UserEntity user1, UserEntity user2, LocalDateTime meetingDate) {
		this.user1 = user1;
		this.user2 = user2;
		this.meetingDate = meetingDate;
	}
}
