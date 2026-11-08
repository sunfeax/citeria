import { environment } from '../../../environments/environment';

export function avatarUrl(userId: string, version: number | null): string | null {
  return version ? `${environment.baseUrl}/users/${userId}/avatar?v=${version}` : null;
}
