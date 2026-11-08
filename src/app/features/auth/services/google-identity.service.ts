import { DOCUMENT } from '@angular/common';
import { inject, Service } from '@angular/core';
import { Observable, shareReplay, tap } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { GoogleAccountsId } from '../models/google-identity';

const SCRIPT_URL = 'https://accounts.google.com/gsi/client';

@Service()
export class GoogleIdentityService {
  /** INJECTORS */
  private readonly document = inject(DOCUMENT);

  /** STATE */
  readonly clientId = environment.googleClientId;
  readonly isEnabled = this.clientId !== '';
  private accountsId$: Observable<GoogleAccountsId> | null = null;

  /** ACTIONS */
  load(): Observable<GoogleAccountsId> {
    this.accountsId$ ??= new Observable<GoogleAccountsId>(subscriber => {
      const loaded = this.document.defaultView?.google?.accounts.id;
      if (loaded) {
        subscriber.next(loaded);
        subscriber.complete();
        return;
      }

      const script = this.document.createElement('script');
      script.src = SCRIPT_URL;
      script.async = true;
      script.onload = () => {
        const accountsId = this.document.defaultView?.google?.accounts.id;
        if (accountsId) {
          subscriber.next(accountsId);
          subscriber.complete();
        } else {
          subscriber.error(new Error('Google Identity Services did not initialize.'));
        }
      };
      script.onerror = () => {
        script.remove();
        subscriber.error(new Error('Unable to load Google Identity Services.'));
      };
      this.document.head.appendChild(script);
    }).pipe(tap({ error: () => (this.accountsId$ = null) }), shareReplay(1));

    return this.accountsId$;
  }
}
