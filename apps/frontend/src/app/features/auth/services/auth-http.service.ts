import { HttpClient, HttpContext } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { IS_PUBLIC_ENDPOINT, SKIP_REFRESH } from '../../../core/config/http-context';
import { GoogleLoginRequest } from '../models/google-login';
import { LoginRequest, LoginResponse } from '../models/login';
import { RefreshResponse } from '../models/refresh';
import { RegisterRequest, RegisterResponse } from '../models/register';
import { User } from '../models/user';

@Service()
export class AuthHttpService {
  /** INJECTORS */
  private readonly http = inject(HttpClient);

  /** ACTIONS */
  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.baseUrl}/auth/login`, payload, {
      withCredentials: true,
      context: new HttpContext().set(IS_PUBLIC_ENDPOINT, true).set(SKIP_REFRESH, true),
    });
  }

  loginWithGoogle(payload: GoogleLoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.baseUrl}/auth/google`, payload, {
      withCredentials: true,
      context: new HttpContext().set(IS_PUBLIC_ENDPOINT, true).set(SKIP_REFRESH, true),
    });
  }

  register(payload: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(
      `${environment.baseUrl}/auth/register`,
      payload,
      {
        withCredentials: true,
        context: new HttpContext().set(IS_PUBLIC_ENDPOINT, true),
      },
    );
  }

  refresh(): Observable<RefreshResponse> {
    return this.http.post<RefreshResponse>(
      `${environment.baseUrl}/auth/refresh`,
      {},
      {
        withCredentials: true,
        context: new HttpContext().set(IS_PUBLIC_ENDPOINT, true).set(SKIP_REFRESH, true),
      },
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>(
      `${environment.baseUrl}/auth/logout`,
      {},
      { withCredentials: true, context: new HttpContext().set(SKIP_REFRESH, true) },
    );
  }

  getMe(): Observable<User> {
    return this.http.get<User>(`${environment.baseUrl}/users/me`, {
      withCredentials: true,
    });
  }

  softDeleteAccount(uuid: string): Observable<User> {
    return this.http.delete<User>(`${environment.baseUrl}/users/${uuid}`, {
      withCredentials: true,
    });
  }
}
