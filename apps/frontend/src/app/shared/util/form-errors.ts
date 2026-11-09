import { HttpErrorResponse } from '@angular/common/http';
import {
  EmailValidationError,
  FieldTree,
  MaxLengthValidationError,
  MinLengthValidationError,
  PatternValidationError,
  RequiredValidationError,
  ValidationError,
} from '@angular/forms/signals';

import { ApiError } from '../models/api-error';

const SERVER_ERROR_KIND = 'server';

export function getFieldError(field: { errors(): readonly ValidationError[] }): string {
  const error = field.errors()[0];

  if (error instanceof RequiredValidationError) return 'Field is required';
  if (error instanceof MinLengthValidationError)
    return `Must be at least ${error.minLength} characters`;
  if (error instanceof MaxLengthValidationError)
    return `Must be at most ${error.maxLength} characters`;
  if (error instanceof EmailValidationError) return 'Invalid email address';
  if (error instanceof PatternValidationError) return 'Invalid format';
  return error?.message ?? '';
}

export function handleSubmitError<T>(
  form: FieldTree<T>,
  err: unknown,
  fallback: string,
  notify: (message: string) => void,
): ValidationError.WithOptionalFieldTree[] | undefined {
  const apiError = (err as HttpErrorResponse).error as ApiError | null;
  const errors = apiError?.errors ?? {};
  const fields = form as unknown as Record<string, FieldTree<unknown> | undefined>;

  const fieldErrors = Object.entries(errors).flatMap(([name, message]) => {
    const fieldTree = fields[name];
    return fieldTree ? [{ kind: SERVER_ERROR_KIND, message, fieldTree }] : [];
  });
  if (fieldErrors.length) {
    return fieldErrors;
  }

  notify(Object.values(errors)[0] ?? apiError?.detail ?? fallback);
  return undefined;
}
