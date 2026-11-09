import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable, switchMap } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { User } from '../../auth/models/user';
import { AuthHttpService } from '../../auth/services/auth-http.service';
import { ChangePasswordRequest } from '../models/user-change-password';
import { UserUpdateRequest } from '../models/user-update-request';

@Service()
export class ProfileService {
  /** INJECTORS */
  private readonly http = inject(HttpClient);
  private readonly authHttpService = inject(AuthHttpService);

  /** ACTIONS */
  update(id: string, payload: UserUpdateRequest): Observable<User> {
    return this.http.patch<User>(`${environment.baseUrl}/users/${id}`, payload);
  }
  uploadAvatar(id: string, file: File): Observable<User> {
    const body = new FormData();
    body.append('file', file);

    return this.http
      .post<void>(`${environment.baseUrl}/users/${id}/avatar`, body)
      .pipe(switchMap(() => this.authHttpService.getMe()));
  }
  deleteAvatar(id: string): Observable<void> {
    return this.http.delete<void>(`${environment.baseUrl}/users/${id}/avatar`);
  }
  changePassword(id: string, payload: ChangePasswordRequest): Observable<void> {
    return this.http.patch<void>(`${environment.baseUrl}/users/${id}/password`, payload);
  }
}
