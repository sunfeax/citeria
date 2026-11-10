import { routePaths } from './route-paths';

export function authReturnUrl(value: string | null): string {
  if (
    !value ||
    value.includes('\\') ||
    [...value].some(char => char.charCodeAt(0) < 32)
  ) {
    return routePaths.offerings;
  }

  const path = value.split(/[?#]/, 1)[0];
  return path === routePaths.offerings ||
    path === routePaths.profile ||
    path.startsWith(`${routePaths.specialists}/`)
    ? value
    : routePaths.offerings;
}
