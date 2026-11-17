import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { PageableContent } from '../../../shared/models/pageable';
import {
  Appointment,
  AppointmentCreateRequest,
  AppointmentFilters,
} from '../models/appointment';

@Service()
export class AppointmentService {
  /** INJECTORS */
  private readonly http = inject(HttpClient);

  /** ACTIONS */
  getList(
    page: number,
    size: number,
    filters: AppointmentFilters,
  ): Observable<PageableContent<Appointment>> {
    let params = new HttpParams().set('page', page).set('size', size);

    for (const [key, value] of Object.entries(filters)) {
      if (value !== '') {
        params = params.set(key, value);
      }
    }

    return this.http.get<PageableContent<Appointment>>(
      `${environment.baseUrl}/appointments`,
      { params },
    );
  }

  create(payload: AppointmentCreateRequest): Observable<Appointment> {
    return this.http.post<Appointment>(`${environment.baseUrl}/appointments`, payload);
  }

  accept(id: string): Observable<Appointment> {
    return this.transition(id, 'accept');
  }

  reject(id: string): Observable<Appointment> {
    return this.transition(id, 'reject');
  }

  pay(id: string): Observable<Appointment> {
    return this.transition(id, 'pay');
  }

  cancel(id: string): Observable<Appointment> {
    return this.transition(id, 'cancel');
  }

  complete(id: string): Observable<Appointment> {
    return this.transition(id, 'complete');
  }

  private transition(id: string, action: string): Observable<Appointment> {
    return this.http.post<Appointment>(
      `${environment.baseUrl}/appointments/${id}/${action}`,
      null,
    );
  }
}
