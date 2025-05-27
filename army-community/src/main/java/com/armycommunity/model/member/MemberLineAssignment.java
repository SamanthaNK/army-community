package com.armycommunity.model.member;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing the assignment of members to different lines.
 * Manages the many-to-many relationship between members and their roles within the group.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "member_line_assignments")
public class MemberLineAssignment {

    @Id
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "line_type", nullable = false)
    private MemberLine lineType;
}
