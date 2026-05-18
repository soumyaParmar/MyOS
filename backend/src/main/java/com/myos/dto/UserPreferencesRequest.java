package com.myos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPreferencesRequest {
    private String[] jobTypes;
    private Double monthlyBudgetLimit;
    private boolean emailNotificationsEnabled;
    private boolean pushNotificationsEnabled;
}
