
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../environments/environment';
import {
  Account,
  Payment,
  Transaction,
  NotificationItem
} from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private base = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  register(body: { fullName: string; email: string; password: string }) {
    return this.http.post(`${this.base}/api/v1/auth/register`, body);
  }

  verifyEmail(email: string, otp: string) {
    return this.http.post(`${this.base}/api/v1/auth/verify-email`, { email, otp });
  }

  resendVerification(email: string) {
    return this.http.post(`${this.base}/api/v1/auth/resend-verification?email=${encodeURIComponent(email)}`, {});
  }

  createAccount(accountType: string) {
    return this.http.post<Account>(`${this.base}/api/v1/accounts`, { accountType });
  }

  lookupAccount(accountNumber: string) {
    return this.http.get<{accountId: string; accountNumber: string; accountType: string; currency: string; status: string}>(
      `${this.base}/api/v1/accounts/lookup/${encodeURIComponent(accountNumber)}`
    );
  }


  accounts() {
    return this.http.get<Account[] | { content?: Account[] }>(
      `${this.base}/api/v1/accounts`
    );
  }

  payments() {
    return this.http.get<Payment[] | { content?: Payment[] }>(
      `${this.base}/api/v1/payments`
    );
  }

  transactions() {
    return this.http.get<Transaction[] | { content?: Transaction[] }>(
      `${this.base}/api/v1/transactions`
    );
  }

  notifications() {
    return this.http.get<NotificationItem[] | { content?: NotificationItem[] }>(
      `${this.base}/api/v1/notifications`
    );
  }

  createPayment(
    body: {
      sourceAccountId: string;
      destinationAccountId: string;
      amount: number;
      currency: string;
      description: string;
    },
    idempotencyKey: string
  ) {
    const headers = new HttpHeaders({
      'Idempotency-Key': idempotencyKey
    });

    return this.http.post<Payment>(
      `${this.base}/api/v1/payments`,
      body,
      { headers }
    );
  }
}

export function asArray<T>(
  v: T[] | { content?: T[] } | null | undefined
): T[] {
  return Array.isArray(v) ? v : (v?.content ?? []);
}
