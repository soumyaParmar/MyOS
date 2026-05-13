# Feature 016 — Encrypt Sensitive Personal Data at Rest (AES-256)

## Source Checkbox

```text
Phase 1 — Foundation & Auth
- [ ] Encrypt sensitive personal data at rest (AES-256)
```

## Goal and User Value

Protect sensitive personal data stored in PostgreSQL so that even if the database is compromised (backup theft, SQL injection, unauthorized access), the raw values of personally identifiable information (PII) remain unreadable. This is a core security hardening step required before the app stores financial data, health info, and AI conversation logs in later phases.

## Scope

### In Scope

- Build a reusable **AES-256-GCM** `AttributeConverter` for JPA that transparently encrypts/decrypts `String` columns
- Encrypt the following sensitive fields on the `User` entity:
  - `email` — PII, used as username (requires special handling for lookups — see design below)
  - `name` — PII
  - `preferences` — stores personal configuration as JSON
  - `providerId` — OAuth provider-specific user identifier
- Store the AES encryption key in `.env` (never in source code)
- Add `application.yml` property placeholder for the key
- Create a Flyway migration (`V6__encrypt_existing_user_data.sql`) as a **Java-based migration** to encrypt existing plaintext data in-place
- Add a **hashed email column** (`email_hash`) for indexed lookups (since encrypted email cannot be used in `WHERE` clauses directly)
- Update `UserRepository` to query by `email_hash` instead of raw `email`
- Update `UserDetailsServiceImpl`, `AuthService`, and `CustomOAuth2UserService` to use the hash-based lookup
- Unit tests for the converter and integration test for the encrypt/decrypt round-trip

### Out of Scope

- Encrypting the `Token.token` field (JWTs are already opaque signed strings; token security is handled by the rotation system)
- Encrypting `password` (already hashed with BCrypt — hashing ≠ encryption)
- Database-level Transparent Data Encryption (TDE) — this is application-level encryption
- Key rotation mechanism (can be added later as a separate feature)
- Encrypting data in MongoDB or other stores (no MongoDB data yet)

## Design

### Encryption Algorithm

**AES-256-GCM** (Galois/Counter Mode):
- Provides both confidentiality and integrity (authenticated encryption)
- Java's `javax.crypto` supports this natively — no external libraries needed
- Each encryption produces a unique random 12-byte IV (initialization vector)
- Output format stored in DB: `Base64(IV + ciphertext + GCM-tag)`

### Architecture

```text
┌──────────────┐       ┌───────────────────────┐       ┌────────────┐
│  User.java   │◄─────►│  EncryptedStringConverter  │◄─────►│ PostgreSQL │
│  (plaintext) │       │  (AES-256-GCM)        │       │ (ciphertext)│
└──────────────┘       └───────────────────────┘       └────────────┘
                                  │
                                  ▼
                       ┌──────────────────────┐
                       │  EncryptionUtil       │
                       │  (key from config)    │
                       └──────────────────────┘
```

### Component Breakdown

#### 1. `EncryptionUtil.java` — `com.myos.security`
Static utility class that provides:
- `encrypt(String plaintext, SecretKey key) → String` (Base64-encoded IV+ciphertext)
- `decrypt(String ciphertext, SecretKey key) → String`
- `deriveKey(String base64Key) → SecretKey`
- `hashForLookup(String value) → String` (SHA-256 hex digest for indexed email lookups)

#### 2. `EncryptionConfig.java` — `com.myos.config`
Spring `@Configuration` bean that:
- Reads `app.encryption.key` from `application.yml`
- Validates key length (must be 32 bytes / 256 bits, Base64-encoded)
- Exposes a `SecretKey` bean for injection

#### 3. `EncryptedStringConverter.java` — `com.myos.security`
JPA `AttributeConverter<String, String>`:
- `convertToDatabaseColumn(String)` → encrypts plaintext
- `convertToEntityAttribute(String)` → decrypts ciphertext
- Obtains `SecretKey` via Spring's `ApplicationContext` (converters can't use constructor injection directly; use a static holder pattern or `@Component` with `@Converter(autoApply = false)`)

#### 4. Updated `User.java` entity
- Annotate `name`, `email`, `preferences`, `providerId` with `@Convert(converter = EncryptedStringConverter.class)`
- Add new column `email_hash` (`VARCHAR(64)`, indexed, unique) for lookup
- Populate `email_hash` automatically in setter / `@PrePersist` / `@PreUpdate`

#### 5. Updated `UserRepository.java`
- Change `findByEmail(String email)` → `findByEmailHash(String emailHash)`
- Add helper to hash before querying

#### 6. Updated services
- `UserDetailsServiceImpl` — hash email before lookup
- `AuthService` — hash email before lookup during registration/login
- `CustomOAuth2UserService` — hash email before lookup

#### 7. Flyway Java migration `V6__encrypt_existing_user_data.java`
- Reads all users from `users` table
- Encrypts `name`, `email`, `preferences`, `provider_id` in-place
- Computes and stores `email_hash` for each row
- Uses raw JDBC (Flyway Java migrations have access to `Connection`)

## Backend Changes

### New Files

| File | Package | Purpose |
|------|---------|---------|
| `EncryptionUtil.java` | `com.myos.security` | AES-256-GCM encrypt/decrypt + SHA-256 hashing |
| `EncryptionConfig.java` | `com.myos.config` | Reads key from config, exposes `SecretKey` bean |
| `EncryptedStringConverter.java` | `com.myos.security` | JPA `AttributeConverter` for transparent encryption |
| `V6__encrypt_existing_user_data.java` | `db.migration` | Java-based Flyway migration to encrypt existing data |

### Modified Files

| File | Change |
|------|--------|
| `User.java` | Add `@Convert` annotations, add `email_hash` field, add `@PrePersist`/`@PreUpdate` lifecycle hooks |
| `UserRepository.java` | Change lookup method to use `emailHash` |
| `UserDetailsServiceImpl.java` | Hash email before repository call |
| `AuthService.java` | Hash email before repository calls |
| `CustomOAuth2UserService.java` | Hash email before repository calls |
| `application.yml` | Add `app.encryption.key` placeholder |
| `.env` | Add `ENCRYPTION_KEY` with a generated 256-bit Base64 key |
| `pom.xml` | No changes needed — `javax.crypto` is part of the JDK |

## Data Model Changes

### `users` table — Flyway V6

```sql
-- Add email_hash column (before encrypting email)
ALTER TABLE users ADD COLUMN email_hash VARCHAR(64);

-- This will be populated by the Java migration
-- After migration, add constraints:
CREATE UNIQUE INDEX idx_users_email_hash ON users(email_hash);
```

> **Note:** The actual encryption of existing data happens in the **Java migration** class `V6__encrypt_existing_user_data.java`, not in SQL. Flyway supports both SQL and Java migrations. The Java migration runs after the SQL migration adds the column.

We will split this into two Flyway steps:
- `V6__add_email_hash_column.sql` — adds the column
- `V7__encrypt_existing_user_data.java` — Java migration that encrypts data and populates hashes

## API Contracts

No API contract changes. Encryption/decryption is transparent at the JPA layer. All REST responses continue to return plaintext values (the decrypted data). The encryption only protects data **at rest** in the database.

## Security and Privacy Considerations

1. **Key Management**: The AES key is stored in `.env` and loaded via environment variable. It must **never** be committed to source control.
2. **Key Strength**: 256-bit key (32 bytes, Base64-encoded = 44 characters). Generated with `SecureRandom`.
3. **IV Uniqueness**: A fresh 12-byte IV is generated for every encryption operation using `SecureRandom`. This ensures identical plaintexts produce different ciphertexts.
4. **Authenticated Encryption**: GCM mode provides integrity verification — tampered ciphertext will fail decryption.
5. **Email Lookups**: Since encrypted email can't be used in SQL `WHERE` clauses (different IV each time → different ciphertext), a SHA-256 hash of the email is stored for indexed lookups. SHA-256 is deterministic and collision-resistant.
6. **Logging Safety**: The `EncryptionUtil` must never log plaintext or keys. The `User.toString()` should not include sensitive fields.
7. **Migration Safety**: The V7 Java migration should be idempotent — if a value is already encrypted (starts with Base64 pattern), skip it.

## Testing and Verification Plan

### Unit Tests
- `EncryptionUtilTest.java`:
  - Encrypt → decrypt round-trip produces original plaintext
  - Different encryptions of same plaintext produce different ciphertexts (IV uniqueness)
  - Tampered ciphertext throws exception
  - Null/empty input handling
- `EncryptedStringConverterTest.java`:
  - `convertToDatabaseColumn` returns non-null encrypted string
  - `convertToEntityAttribute` returns original plaintext
  - Null input returns null (nullable columns)

### Integration Tests
- Verify `User` entity can be saved and loaded with encrypted fields
- Verify `findByEmailHash` returns the correct user
- Verify the Flyway migration runs successfully against a test DB

### Manual Verification
- Start the app, register a user, then query PostgreSQL directly — confirm `name`, `email`, `preferences`, and `provider_id` columns contain Base64 ciphertext, not plaintext
- Confirm the API still returns plaintext values in responses
- Confirm login (both local and OAuth2) still works with the hash-based email lookup

## Acceptance Criteria

- [ ] `EncryptionUtil` correctly encrypts/decrypts with AES-256-GCM
- [ ] `EncryptedStringConverter` transparently encrypts on write and decrypts on read
- [ ] `User.email`, `User.name`, `User.preferences`, and `User.providerId` are stored encrypted in PostgreSQL
- [ ] `email_hash` column exists, is unique-indexed, and populated for all users
- [ ] All services use `emailHash` for user lookups (no plaintext email queries)
- [ ] Existing data is migrated via Flyway Java migration
- [ ] AES key is configured via `.env` / environment variable only
- [ ] Unit tests pass for encryption utility and converter
- [ ] Login and OAuth2 flows work correctly after the change
- [ ] Direct database inspection shows only ciphertext for sensitive columns

## Open Questions

1. **Should we encrypt `roles`?** Currently it's a comma-separated string like `"ROLE_USER,ROLE_ADMIN"`. It's not PII, but it could be considered security-sensitive. Recommendation: skip for now since it's used in authorization checks and isn't personal data.

2. **Key rotation strategy?** This plan does not include key rotation. A future feature could add a `key_version` column and support decrypting with old keys while encrypting with the new key. Should we file a backlog item for this?

3. **Performance impact?** AES-256-GCM is hardware-accelerated on modern CPUs (AES-NI). The overhead per field is negligible (~microseconds). However, since email lookups now use `email_hash` instead of the encrypted value, query performance should be identical to the current setup.
