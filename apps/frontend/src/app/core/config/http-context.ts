import { HttpContextToken } from '@angular/common/http';

export const SKIP_REFRESH = new HttpContextToken<boolean>(() => false);
export const IS_PUBLIC_ENDPOINT = new HttpContextToken<boolean>(() => false);
