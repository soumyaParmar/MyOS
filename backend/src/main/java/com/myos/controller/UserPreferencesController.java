package com.myos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myos.dto.UserPreferencesRequest;
import com.myos.dto.UserPreferencesResponse;
import com.myos.entity.User;
import com.myos.service.UserPreferencesService;

@RestController
@RequestMapping("/api/v1/preferences")
public class UserPreferencesController {

    @Autowired
    private UserPreferencesService userPreferencesService;

    @GetMapping
    public ResponseEntity<UserPreferencesResponse> getUserPreferences(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userPreferencesService.getUserPreferences(user.getId()));
    }

    @PutMapping
    public ResponseEntity<UserPreferencesResponse> updatePreferences(@AuthenticationPrincipal User user , @RequestBody UserPreferencesRequest userPreferencesRequest){
        return ResponseEntity.ok(userPreferencesService.update(user.getId(), userPreferencesRequest));
    }

}
