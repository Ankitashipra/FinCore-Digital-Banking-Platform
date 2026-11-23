import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginResponse } from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private tokenState = signal<string | null>(sessionStorage.getItem('fincore_access_token'));
  readonly isAuthenticated = () => !!this.tokenState();

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiBaseUrl}/api/v1/auth/login`, { email, password }).pipe(
      tap(response => {
        if (!response?.accessToken) throw new Error('Login response has no accessToken');
        sessionStorage.setItem('fincore_access_token', response.accessToken);
        this.tokenState.set(response.accessToken);
      })
    );
  }

  token(): string | null { return this.tokenState(); }

  role(): string | null {
    const token = this.tokenState();
    if (!token) return null;
    try {
      const payload = token.split('.')[1];
      if (!payload) return null;
      const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
      const decoded = atob(normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '='));
      const claims = JSON.parse(decoded) as { role?: string };
      return claims.role?.toUpperCase() ?? null;
    } catch {
      return null;
    }
  }

  logout(): void {
    sessionStorage.removeItem('fincore_access_token');
    this.tokenState.set(null);
  }
}
