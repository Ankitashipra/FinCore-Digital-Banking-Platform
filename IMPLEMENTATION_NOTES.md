# FinCore update — implementation notes

## Included in this source update
- Email OTP verification now confirms email ownership only; it no longer calls Account Service or silently creates an account.
- Verified customers enter `PENDING_REVIEW` and remain inactive, so the existing login and JWT filter reject them until an administrator approves the application.
- Identity Service adds administrator-only customer application listing and approve/reject endpoints. Decisions record reviewer email, timestamp, and optional reason. An administrator cannot approve their own customer record.
- Registration and verification screens explain the review stage and clarify that email OTP is not Aadhaar/KYC verification.
- Existing verified database rows receive the `APPROVED` default when Hibernate adds the new application status column. New registrations explicitly set `PENDING_EMAIL`.

## Administrator provisioning
There is intentionally no public admin registration endpoint. Provision an administrator through a controlled database operation. Generate a BCrypt password hash using the Identity Service `PasswordEncoder` (for example, a temporary internal runner), then insert/update a trusted operator row with `role='ADMIN'`, `active=true`, `email_verified=true`, and `application_status='APPROVED'`. Never commit the password or hash to source control. Restrict database access and rotate the operator password after initial provisioning.

## New admin API
- `GET /api/v1/admin/customers` — ADMIN role only; returns customer application metadata.
- `PUT /api/v1/admin/customers/{id}/decision` — ADMIN role only; JSON body: `{"decision":"APPROVE","reason":"..."}` or `{"decision":"REJECT","reason":"..."}`.

These endpoints are in Identity Service. They do not yet implement the complete admin console, account-opening approval workflow, account/transaction read-only investigation screens, or cross-customer transfer recipient discovery. Those require coordinated changes to Account Service and the Angular UI and must not be represented as completed by this package.

## Important operational notes
- Configure SMTP for real email delivery. `APP_MAIL_DEV_LOG_OTP=true` is development-only and must be false in deployed environments.
- Email OTP proves access to an email inbox only; it is not legal identity verification or Aadhaar/KYC.
- Existing accounts and database volumes are preserved. Do not run `docker compose down -v`.
- Build/test the Identity Service before deploying: `mvn clean test` from `backend/identity-service`.
- Review existing generated-password / in-memory security logs in other services before any public deployment.
