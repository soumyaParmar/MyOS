package com.myos.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.myos.dto.UserPreferencesRequest;
import com.myos.dto.UserPreferencesResponse;
import com.myos.entity.User;
import com.myos.entity.UserPreferences;
import com.myos.repository.UserPreferencesRepository;
import com.myos.repository.UserRepository;


@Service
public class UserPreferencesService {
    private final UserPreferencesRepository userPreferencesRepository;
    private final UserRepository userRepository;

    public UserPreferencesService(UserPreferencesRepository userPreferencesRepository, UserRepository userRepository){
        this.userPreferencesRepository = userPreferencesRepository;
        this.userRepository = userRepository;
    }

    public UserPreferencesResponse getUserPreferences(UUID id){
        UserPreferences prefs = userPreferencesRepository.findByUserId(id)
                .orElseGet(() -> {
                    User user = getUserById(id);
                    return new UserPreferences(user);
                });
        return mapToResponseDTO(prefs);
    }

    @Transactional
    public UserPreferencesResponse update(UUID id , UserPreferencesRequest userPreferenceRequest){
        UserPreferences prefernce = userPreferencesRepository.findByUserId(id).orElseGet(()-> new UserPreferences(getUserById(id)));

        prefernce.setJobTypes(userPreferenceRequest.getJobTypes() != null ? String.join(",",userPreferenceRequest.getJobTypes()) : prefernce.getJobTypes());
        prefernce.setMonthlyBudgetLimit(userPreferenceRequest.getMonthlyBudgetLimit());
        prefernce.setEmailNotificationsEnabled(userPreferenceRequest.isEmailNotificationsEnabled());
        prefernce.setPushNotificationsEnabled(userPreferenceRequest.isPushNotificationsEnabled());

        return mapToResponseDTO(userPreferencesRepository.save(prefernce));
    }

    private User getUserById(UUID id){
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    private UserPreferencesResponse mapToResponseDTO(UserPreferences userPreferences){
        return UserPreferencesResponse.builder()
                .jobTypes(userPreferences.getJobTypes() != null ? userPreferences.getJobTypes().split(",") : new String[0])
                .monthlyBudgetLimit(userPreferences.getMonthlyBudgetLimit())
                .emailNotificationsEnabled(userPreferences.getEmailNotificationsEnabled())
                .pushNotificationsEnabled(userPreferences.getPushNotificationsEnabled())
                .build();
    }

}
