import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { PageableContent } from '../../../shared/models/pageable';
import { OfferingFilters, OfferingList } from '../models/offering-list';

@Service()
export class OfferingService {
  /** INJECTORS */
  private readonly http = inject(HttpClient);

  /** ACTIONS */
  getList(
    page: number,
    size: number,
    filters: OfferingFilters,
  ): Observable<PageableContent<OfferingList>> {
    let params = new HttpParams().set('page', page).set('size', size);

    for (const [key, value] of Object.entries(filters)) {
      if (value !== '' && value !== null && value !== undefined) {
        params = params.set(key, value);
      }
    }

    return this.http.get<PageableContent<OfferingList>>(
      `${environment.baseUrl}/offerings`,
      {
        params,
        withCredentials: true,
      },
    );
  }
}
