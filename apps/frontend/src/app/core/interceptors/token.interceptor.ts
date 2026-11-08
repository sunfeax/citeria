import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { SessionStore } from '../../features/auth/session.store';
import { IS_PUBLIC_ENDPOINT } from '../config/http-context';

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
  const sessionStore = inject(SessionStore);
  const token = sessionStore.getAccessToken();
  const newReq =
    token && !req.context.get(IS_PUBLIC_ENDPOINT)
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

  return next(newReq);
};
