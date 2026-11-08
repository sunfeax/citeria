import { Service, signal } from '@angular/core';
import { User } from './models/user';

@Service()
export class SessionStore {
  /** STATE */
  private readonly accessToken = signal<string | null>(null);
  private readonly currentUser = signal<User | null>(null);
  readonly user = this.currentUser.asReadonly();

  /** ACTIONS */
  requireUser(): User {
    const user = this.currentUser();
    if (!user) {
      throw new Error('Session service doesn´t have an authenticated session');
    }
    return user;
  }

  getAccessToken(): string | null {
    return this.accessToken();
  }

  setAccessToken(token: string): void {
    this.accessToken.set(token);
  }

  clearSession(): void {
    this.accessToken.set(null);
    this.currentUser.set(null);
  }

  setUser(user: User): void {
    this.currentUser.set(user);
  }
}
