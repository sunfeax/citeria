import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { User } from '../../auth/models/user';
import { ChangePasswordRequest } from '../models/user-change-password';
import { UserUpdateRequest } from '../models/user-update-request';

@Service()
export class ProfileService {
  /** INJECTORS */
  private readonly http = inject(HttpClient);

  /** ACTIONS */
  update(id: string, payload: UserUpdateRequest): Observable<User> {
    return this.http.patch<User>(`${environment.baseUrl}/users/${id}`, payload, {
      withCredentials: true,
    });
  }
  changePassword(id: string, payload: ChangePasswordRequest): Observable<void> {
    return this.http.patch<void>(`${environment.baseUrl}/users/${id}/password`, payload, {
      withCredentials: true,
    });
  }
}
