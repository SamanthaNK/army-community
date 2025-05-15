package com.armycommunity.model.member;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
