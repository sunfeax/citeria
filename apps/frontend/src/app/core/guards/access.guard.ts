import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionStore } from '../../features/auth/session.store';
import { routePaths } from '../../shared/util/route-paths';

export const accessGuard: CanActivateFn = (_route, state) => {
  const sessionStore = inject(SessionStore);
  const router = inject(Router);

  if (sessionStore.user()) {
    return true;
  }

  return router.createUrlTree([routePaths.login], {
    queryParams: { returnUrl: state.url },
  });
};
