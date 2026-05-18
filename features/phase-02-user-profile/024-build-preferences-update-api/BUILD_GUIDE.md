# Build Guide: Preferences Update API

In this task, you will build the "brain" behind user settings. This API allows the MyOS system to know what kind of jobs the user likes, what their budget is, and how they want to be notified.

## 🎯 Significance
Think of `UserPreferences` as the **Configuration File** for your AI agents. 
- The **Job Agent** will look at `job_types` to filter listings.
- The **Finance Agent** will look at `monthly_budget_limit` to send alerts.
Without this API, the agents wouldn't know how to serve the user personally.

---

## 🏗️ Step 1: Create the DTOs (Data Transfer Objects)
**Location:** `com.myos.dto`

We never send our JPA Entities directly to the frontend. Why? Because entities often contain sensitive data (like `user_id`) or internal database fields (like `id` or `version`) that the frontend doesn't need.

### Task:
1. Create `UserPreferencesResponse.java`:
    - Include fields: `jobTypes`, `monthlyBudgetLimit`, `emailNotificationsEnabled`, `pushNotificationsEnabled`.
    - Use `@Data` or `@Getter/@Setter` and `@Builder` from Lombok.
2. Create `UserPreferencesRequest.java`:
    - Include the same fields. This will be used to capture what the user wants to change.

---

## 🧠 Step 2: The Service Layer (Business Logic)
**Location:** `com.myos.service`
**File:** `UserPreferencesService.java`

The service layer is where the "thinking" happens.

### Logic to implement:
1. **Fetch Preferences**: Use the `UserPreferencesRepository` to find preferences by `userId`.
2. **Handle "First Time" Users**: If a user just signed up, they might not have a preferences record in the DB yet. Your service should check for this and return a **default** set of preferences if none are found.
3. **Update Logic**: When updating, you shouldn't just replace the object. You should find the existing one, update its fields, and then save it.

### Spring Concepts to use:
- `@Service`: Tells Spring this class is a bean that holds business logic.
- `@Transactional`: Ensures that if something goes wrong during the DB save, the changes are rolled back.
- **Dependency Injection**: Inject the `UserPreferencesRepository` and `UserRepository` via the constructor.

---

## 🚪 Step 3: The Controller (The API Gateway)
**Location:** `com.myos.controller`
**File:** `UserPreferencesController.java`

### Task:
Create two endpoints:
1. `GET /api/preferences`: Returns the current user's preferences.
2. `PUT /api/preferences`: Accepts the DTO and updates the settings.

### 🛡️ Security Pro-Tip (IMPORTANT):
**Do NOT** pass the `userId` in the URL (e.g., `/api/preferences/123`). 
A malicious user could change the ID to `456` and edit someone else's settings!
Instead, get the `userId` from the `SecurityContextHolder` or use `@AuthenticationPrincipal`. This ensures users can only ever see/edit **their own** data.

---

## 🧪 Step 4: Verification
Once you've written the code:
1. **Compile**: Run `./mvnw clean compile` to check for syntax errors.
2. **Test**: Try calling the API using a tool like Postman or Curl.
   - You'll need to include your JWT in the `Authorization` header.

---

## 💡 Learning Notes for the implementation:
- **Optional<T>**: When calling `repository.findByUserId()`, use `Optional`. It prevents `NullPointerException` and makes your code cleaner.
- **Mapping**: You'll need to convert your Entity to a DTO before returning it. You can do this manually in the service layer for now.
