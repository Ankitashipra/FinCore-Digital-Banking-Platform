# FinCore Frontend

Angular standalone frontend for the FinCore API Gateway.

## Run (PowerShell)
```powershell
cd C:\dev\FinCore\frontend
npm install
npm start
```
Open http://localhost:4200.

API base URL: `src/environments/environment.ts` (defaults to `http://localhost:8086`).

Includes login, overview, accounts, transfers, transactions and notifications.
This is a development integration starter, not a production-certified banking app.
Review token storage, CSP, CORS, MFA, audit logging and backend authorization before public launch.
