export interface ApiError {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string | null;
  code: ApiErrorCode;
  timestamp: string;
  errors: Record<string, string>;
}

export type ApiErrorCode =
  | 'RESOURCE_NOT_FOUND'
  | 'USER_NOT_FOUND'
  | 'SLOT_ALREADY_BOOKED'
  | 'CONFLICT'
  | 'VALIDATION_ERROR'
  | 'CONTENT_TOO_LARGE'
  | 'INVALID_REQUEST_BODY'
  | 'UNAUTHORIZED'
  | 'FORBIDDEN'
  | 'AUTHENTICATION_FAILED'
  | 'FEATURE_UNAVAILABLE'
  | 'INTERNAL_ERROR';
