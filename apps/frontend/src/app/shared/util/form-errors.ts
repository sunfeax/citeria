import { AbstractControl, FormGroup } from '@angular/forms';

const SERVER_ERROR_KEY = 'server';

export function getFieldError(control: AbstractControl): string {
  if (control.hasError(SERVER_ERROR_KEY)) return control.getError(SERVER_ERROR_KEY);
  if (control.hasError('required')) return 'Field is required';
  if (control.hasError('minlength'))
    return `Must be at least ${control.getError('minlength').requiredLength} characters`;
  if (control.hasError('maxlength'))
    return `Must be at most ${control.getError('maxlength').requiredLength} characters`;
  if (control.hasError('email')) return 'Invalid email address';
  if (control.hasError('pattern')) return 'Invalid format';
  return '';
}

export function applyServerErrors(form: FormGroup, errors: Record<string, string>): void {
  Object.entries(errors).forEach(([field, message]) => {
    const control = form.get(field);
    control?.setErrors({ [SERVER_ERROR_KEY]: message });
    control?.markAsTouched();
  });
}

export function clearServerErrors(form: FormGroup): void {
  Object.values(form.controls).forEach(control => {
    if (control.hasError(SERVER_ERROR_KEY)) {
      control.updateValueAndValidity({ emitEvent: false });
    }
  });
}
