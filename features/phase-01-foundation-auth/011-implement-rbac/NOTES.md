# Implementation Notes - RBAC

## Method Security
Add to `SecurityConfig.java`:
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // This enables @PreAuthorize
public class SecurityConfig { ... }
```

## Admin Check Example
```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public String getStats() {
        return "System Stats: ALL GOOD";
    }
}
```

## Frontend Role Check
```typescript
const { user } = useAuth();
const isAdmin = user?.roles.includes('ROLE_ADMIN');
```
