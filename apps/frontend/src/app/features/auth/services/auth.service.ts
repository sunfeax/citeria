import { HttpErrorResponse } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import {
  catchError,
  finalize,
  map,
  Observable,
  shareReplay,
  switchMap,
  tap,
  throwError,
} from 'rxjs';
import { TabSyncService } from '../../../core/services/tab-sync.service';
import { publicRoutePaths, routePaths } from '../../../shared/util/route-paths';
import { GoogleLoginRequest } from '../models/google-login';
import { LoginRequest, LoginResponse } from '../models/login';
import { RefreshResponse } from '../models/refresh';
import { RegisterRequest, RegisterResponse } from '../models/register';
import { User } from '../models/user';
import { SessionStore } from '../session.store';
import { AuthHttpService } from './auth-http.service';

@Service()
export class AuthService {
  /** INJECTORS */
  private readonly authHttpService = inject(AuthHttpService);
  private readonly sessionStore = inject(SessionStore);
  private readonly tabSyncService = inject(TabSyncService);
  private readonly router = inject(Router);

  /** STATE */
  private refresh$: Observable<RefreshResponse> | null = null;

  constructor() {
    this.tabSyncService.incoming
      .pipe(
        switchMap(event => {
          return this.restoreSession().pipe(map(user => ({ user, event })));
        }),
        takeUntilDestroyed(),
      )
      .subscribe(data => {
        if (data.event === 'login') {
          this.router.navigateByUrl(routePaths.offerings);
        } else {
          this.router.navigateByUrl(routePaths.login);
        }
      });
  }

  /** ACTIONS */
  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.authHttpService
      .login(payload)
      .pipe(tap(response => this.startSession(response)));
  }

  loginWithGoogle(payload: GoogleLoginRequest): Observable<LoginResponse> {
    return this.authHttpService
      .loginWithGoogle(payload)
      .pipe(tap(response => this.startSession(response)));
  }

  register(payload: RegisterRequest): Observable<RegisterResponse> {
    return this.authHttpService.register(payload);
  }

  refresh(): Observable<RefreshResponse> {
    if (!this.refresh$) {
      this.refresh$ = this.authHttpService.refresh().pipe(
        tap(response => this.sessionStore.setAccessToken(response.accessToken)),
        catchError((err: HttpErrorResponse) => {
          if (err.status === 401) {
            this.endSession(!this.isOnPublicPage());
          }
          return throwError(() => err);
        }),
        finalize(() => (this.refresh$ = null)),
        shareReplay(1),
      );
    }
    return this.refresh$;
  }

  logout(): Observable<void> {
    return this.authHttpService.logout().pipe(
      finalize(() => {
        this.endSession();
        this.tabSyncService.send('logout');
      }),
    );
  }

  getMe(): Observable<User> {
    return this.authHttpService.getMe().pipe(
      tap(response => {
        this.sessionStore.setUser(response);
      }),
    );
  }

  restoreSession(): Observable<User> {
    return this.refresh().pipe(switchMap(() => this.getMe()));
  }

  softDeleteAccount(): Observable<User> {
    return this.authHttpService.softDeleteAccount(this.sessionStore.requireUser().id);
  }

  private startSession(response: LoginResponse): void {
    this.sessionStore.setAccessToken(response.accessToken);
    this.sessionStore.setUser(response.user);
    this.tabSyncService.send('login');
  }

  private endSession(redirectToLogin = true): void {
    const hadUser = this.sessionStore.user() !== null;
    this.sessionStore.clearSession();
    if (hadUser && redirectToLogin) {
      void this.router.navigateByUrl(routePaths.login);
    }
  }

  private isOnPublicPage(): boolean {
    const path = this.router.url.split(/[?#]/, 1)[0];
    return (
      publicRoutePaths.includes(path) || path.startsWith(`${routePaths.specialists}/`)
    );
  }
}
