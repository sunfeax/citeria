import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Slot } from '../models/slot';
import { SpecialistDetail } from '../models/specialist-detail';

@Service()
export class SpecialistService {
  /** INJECTORS */
  private readonly http = inject(HttpClient);

  /** ACTIONS */
  getDetail(id: string): Observable<SpecialistDetail> {
    return this.http.get<SpecialistDetail>(
      `${environment.baseUrl}/specialist-detail/${id}`,
    );
  }

  getSlots(offeringId: string, from: string, to: string): Observable<Slot[]> {
    const params = new HttpParams().set('from', from).set('to', to);
    return this.http.get<Slot[]>(`${environment.baseUrl}/offerings/${offeringId}/slots`, {
      params,
    });
  }
}
