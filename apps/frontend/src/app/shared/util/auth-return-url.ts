import { routePaths } from './route-paths';

const returnPaths: readonly string[] = [
  routePaths.offerings,
  routePaths.profile,
  routePaths.bookings,
];

export function authReturnUrl(value: string | null): string {
  if (
    !value ||
    value.includes('\\') ||
    [...value].some(char => char.charCodeAt(0) < 32)
  ) {
    return routePaths.offerings;
  }

  const path = value.split(/[?#]/, 1)[0];
  return returnPaths.includes(path) || path.startsWith(`${routePaths.specialists}/`)
    ? value
    : routePaths.offerings;
}
