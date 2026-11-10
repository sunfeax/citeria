import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Appointment, AppointmentCreateRequest } from '../models/appointment';

@Service()
export class AppointmentService {
  /** INJECTORS */
  private readonly http = inject(HttpClient);

  /** ACTIONS */
  create(payload: AppointmentCreateRequest): Observable<Appointment> {
    return this.http.post<Appointment>(`${environment.baseUrl}/appointments`, payload);
  }
}
