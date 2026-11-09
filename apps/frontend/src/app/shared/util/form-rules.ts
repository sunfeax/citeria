import {
  maxLength,
  minLength,
  pattern,
  required,
  SchemaPath,
} from '@angular/forms/signals';

export function personNameRules(path: SchemaPath<string>): void {
  required(path);
  minLength(path, 2);
  maxLength(path, 50);
  pattern(path, /^[\p{L}]+(?:[\s'-][\p{L}]+)*$/u);
}

export function phoneRules(path: SchemaPath<string>): void {
  minLength(path, 7);
  maxLength(path, 20);
  pattern(path, /^\+?\d+$/);
}

export function newPasswordRules(path: SchemaPath<string>): void {
  required(path);
  minLength(path, 8);
  pattern(path, /^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])[A-Za-z0-9@#$%^&+=!]+$/);
}
