import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators as v,
} from '@angular/forms';
import { MatButton, MatIconButton } from '@angular/material/button';
import {
  MatError,
  MatFormField,
  MatLabel,
  MatSuffix,
} from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
import { MatInput } from '@angular/material/input';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { finalize, switchMap } from 'rxjs';
import { ApiError } from '../../../../shared/models/api-error';
import { SnackbarService } from '../../../../shared/services/snackbar.service';
import {
  applyServerErrors,
  clearServerErrors,
  getFieldError,
} from '../../../../shared/util/form-errors';
import { SessionStore } from '../../../auth/session.store';
import { ProfileService } from '../../services/profile.service';
import { DialogService } from './../../../../shared/services/dialog.service';
import {
  getChangePasswordPayload,
  getUserUpdatePayload,
} from './../../../../shared/util/payload-handler';
import { AuthService } from './../../../auth/services/auth.service';

@Component({
  selector: 'app-profile',
  imports: [
    ReactiveFormsModule,
    MatButton,
    MatIconButton,
    MatFormField,
    MatLabel,
    MatError,
    MatSuffix,
    MatInput,
    MatIcon,
    MatProgressSpinner,
  ],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss',
})
export class ProfileComponent {
  /** INJECTORS */
  private readonly sessionStore = inject(SessionStore);
  private readonly profileService = inject(ProfileService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly dialogService = inject(DialogService);
  private readonly authService = inject(AuthService);

  /** TEMPLATE HELPERS */
  readonly getFieldError = getFieldError;

  /** DATA */
  readonly user = computed(() => this.sessionStore.requireUser());
  readonly hasPassword = computed(() => this.user().hasPassword);

  /** STATE */
  isProfileLoading = signal<boolean>(false);
  isPasswordLoading = signal<boolean>(false);
  isCurrentPasswordVisible = signal<boolean>(false);
  isNewPasswordVisible = signal<boolean>(false);

  /** FORM */
  profileForm = new FormGroup({
    firstName: new FormControl(this.user().firstName, {
      nonNullable: true,
      validators: [
        v.required,
        v.minLength(2),
        v.maxLength(50),
        v.pattern(/^[\p{L}]+(?:[\s'-][\p{L}]+)*$/u),
      ],
    }),
    lastName: new FormControl(this.user().lastName, {
      nonNullable: true,
      validators: [
        v.required,
        v.minLength(2),
        v.maxLength(50),
        v.pattern(/^[\p{L}]+(?:[\s'-][\p{L}]+)*$/u),
      ],
    }),
    email: new FormControl(this.user().email, {
      nonNullable: true,
      validators: [v.required, v.email],
    }),
    phone: new FormControl(this.user().phone, {
      nonNullable: true,
      validators: [v.required, v.minLength(7), v.maxLength(20), v.pattern(/^\+?\d+$/)],
    }),
  });
  passwordForm = new FormGroup({
    currentPassword: new FormControl('', {
      nonNullable: true,
      validators: [v.required],
    }),
    newPassword: new FormControl('', {
      nonNullable: true,
      validators: [
        v.required,
        v.minLength(8),
        v.pattern(/^(?=.*[A-Z])(?=.*[@#$%^&+=!])[A-Za-z0-9@#$%^&+=!]+$/),
      ],
    }),
  });

  constructor() {
    if (!this.hasPassword()) {
      this.passwordForm.controls.currentPassword.disable();
    }
  }

  /** ACTIONS */
  submitProfileForm(): void {
    clearServerErrors(this.profileForm);

    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.isProfileLoading.set(true);
    this.profileService
      .update(this.user().id, getUserUpdatePayload(this.profileForm.getRawValue()))
      .pipe(
        finalize(() => {
          this.isProfileLoading.set(false);
        }),
      )
      .subscribe({
        next: user => {
          this.sessionStore.setUser(user);
          this.snackbarService.success('Your information has been updated.');
          this.profileForm.markAsPristine();
        },
        error: (err: HttpErrorResponse) => {
          const apiError = err.error as ApiError;
          if (apiError.errors) {
            applyServerErrors(this.profileForm, apiError.errors);
          } else {
            this.snackbarService.error(apiError.detail ?? 'Update failed.');
          }
        },
      });
  }

  submitPasswordForm(): void {
    clearServerErrors(this.passwordForm);

    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.isPasswordLoading.set(true);
    this.profileService
      .changePassword(
        this.user().id,
        getChangePasswordPayload(this.passwordForm.getRawValue()),
      )
      .pipe(
        finalize(() => {
          this.isPasswordLoading.set(false);
        }),
      )
      .subscribe({
        next: () => {
          const wasSet = !this.hasPassword();
          this.snackbarService.success(
            wasSet ? 'Your password has been set.' : 'Your password has been updated.',
          );
          if (wasSet) {
            this.sessionStore.setUser({ ...this.user(), hasPassword: true });
            this.passwordForm.controls.currentPassword.enable();
          }
          this.passwordForm.reset();
        },
        error: (err: HttpErrorResponse) => {
          const apiError = err.error as ApiError;
          if (apiError.errors) {
            applyServerErrors(this.passwordForm, apiError.errors);
          } else {
            this.snackbarService.error(apiError.detail ?? 'Password update failed.');
          }
        },
      });
  }

  deleteAccount(): void {
    this.dialogService
      .confirm({
        title: 'Delete account',
        message:
          'Are you sure you want to delete your account? You will be signed out on all devices.',
        confirmText: 'Delete',
        cancelText: 'Cancel',
        variant: 'danger',
      })
      .pipe(
        switchMap(() => this.authService.softDeleteAccount()),
        switchMap(() => this.authService.logout()),
      )
      .subscribe({
        error: () =>
          this.snackbarService.error('Unable to delete your account. Please try again.'),
      });
  }
}
