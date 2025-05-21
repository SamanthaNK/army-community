package com.armycommunity.dto.request.member;

import com.armycommunity.model.member.MemberLine;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;


@Getter
@Setter
public class MemberRequest {
    @NotBlank(message = "Stage name is required")
    @Size(max = 50, message = "Stage name cannot exceed 50 characters")
    private String stageName;

    @NotBlank(message = "Real name is required")
    @Size(max = 100, message = "Real name cannot exceed 100 characters")
    private String realName;

    @NotNull(message = "Birthday is required")
    private LocalDate birthday;

    @Size(max = 100, message = "Position cannot exceed 100 characters")
    private String position;

    private Set<MemberLine> lineTypes;
}
