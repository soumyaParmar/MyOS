/**
 * UserPreferencesResponseDTO
 * 
 * Represents the preferences settings fetched from the backend.
 * Every field matches the UserPreferencesResponse Java class.
 */
export interface UserPreferencesResponseDTO {
    jobTypes: string[];
    monthlyBudgetLimit: number;
    emailNotificationsEnabled: boolean;
    pushNotificationsEnabled: boolean;
}

/**
 * UserPreferencesRequestDTO
 * 
 * Represents the data sent to the backend to update preferences.
 * Every field matches the UserPreferencesRequest Java class.
 */
export interface UserPreferencesRequestDTO {
    jobTypes: string[];
    monthlyBudgetLimit: number;
    emailNotificationsEnabled: boolean;
    pushNotificationsEnabled: boolean;
}
