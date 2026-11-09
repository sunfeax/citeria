import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { email, form, FormField, FormRoot, required } from '@angular/forms/signals';
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
import { MatTooltip } from '@angular/material/tooltip';
import { exhaustMap, finalize, firstValueFrom, switchMap } from 'rxjs';
import { ApiError } from '../../../../shared/models/api-error';
import { SnackbarService } from '../../../../shared/services/snackbar.service';
import { avatarUrl } from '../../../../shared/util/avatar-url';
import { getFieldError, handleSubmitError } from '../../../../shared/util/form-errors';
import {
  newPasswordRules,
  personNameRules,
  phoneRules,
} from '../../../../shared/util/form-rules';
import { SessionStore } from '../../../auth/session.store';
import { ProfileService } from '../../services/profile.service';
import { DialogService } from './../../../../shared/services/dialog.service';
import {
  getChangePasswordPayload,
  getUserUpdatePayload,
} from './../../../../shared/util/payload-handler';
import { AuthService } from './../../../auth/services/auth.service';

const AVATAR_MAX_BYTES = 5 * 1024 * 1024;
const AVATAR_TYPES = ['image/png', 'image/jpeg', 'image/jpg'];
type SettingsSection = 'profile' | 'security' | 'account';

@Component({
  selector: 'app-profile',
  imports: [
    FormField,
    FormRoot,
    MatButton,
    MatIconButton,
    MatFormField,
    MatLabel,
    MatError,
    MatSuffix,
    MatInput,
    MatIcon,
    MatProgressSpinner,
    MatTooltip,
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
  readonly avatarSrc = computed(() =>
    avatarUrl(this.user().id, this.user().avatarVersion),
  );
  readonly avatarAccept = AVATAR_TYPES.join(',');

  /** STATE */
  readonly activeSection = signal<SettingsSection>('profile');
  readonly sections: { id: SettingsSection; label: string; icon: string }[] = [
    { id: 'profile', label: 'Profile', icon: 'person_outline' },
    { id: 'security', label: 'Security', icon: 'shield' },
    { id: 'account', label: 'Account', icon: 'manage_accounts' },
  ];
  isAvatarLoading = signal<boolean>(false);
  isCurrentPasswordVisible = signal<boolean>(false);
  isNewPasswordVisible = signal<boolean>(false);

  /** FORM */
  profileModel = signal({
    firstName: this.user().firstName,
    lastName: this.user().lastName,
    email: this.user().email,
    phone: this.user().phone ?? '',
  });
  profileForm = form(
    this.profileModel,
    s => {
      personNameRules(s.firstName);
      personNameRules(s.lastName);
      required(s.email);
      email(s.email);
      phoneRules(s.phone);
    },
    {
      submission: {
        action: async field => {
          try {
            const user = await firstValueFrom(
              this.profileService.update(
                this.user().id,
                getUserUpdatePayload(this.profileModel()),
              ),
            );
            this.sessionStore.setUser(user);
            this.snackbarService.success('Your information has been updated.');
            this.profileForm().reset();
          } catch (err) {
            return handleSubmitError(field, err, 'Update failed.', message =>
              this.snackbarService.error(message),
            );
          }
          return undefined;
        },
      },
    },
  );
  passwordModel = signal({ currentPassword: '', newPassword: '' });
  passwordForm = form(
    this.passwordModel,
    s => {
      required(s.currentPassword, { when: () => this.hasPassword() });
      newPasswordRules(s.newPassword);
    },
    {
      submission: {
        action: async field => {
          try {
            await firstValueFrom(
              this.profileService.changePassword(
                this.user().id,
                getChangePasswordPayload(this.passwordModel()),
              ),
            );
            const wasSet = !this.hasPassword();
            this.snackbarService.success(
              wasSet ? 'Your password has been set.' : 'Your password has been updated.',
            );
            if (wasSet) {
              this.sessionStore.setUser({ ...this.user(), hasPassword: true });
            }
            this.passwordForm().reset({ currentPassword: '', newPassword: '' });
          } catch (err) {
            return handleSubmitError(field, err, 'Password update failed.', message =>
              this.snackbarService.error(message),
            );
          }
          return undefined;
        },
      },
    },
  );

  /** ACTIONS */
  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';

    if (!file) {
      return;
    }
    if (!AVATAR_TYPES.includes(file.type)) {
      this.snackbarService.error('Please choose a PNG or JPEG image.');
      return;
    }
    if (file.size > AVATAR_MAX_BYTES) {
      this.snackbarService.error('The photo must not exceed 5 MB.');
      return;
    }

    this.isAvatarLoading.set(true);
    this.profileService
      .uploadAvatar(this.user().id, file)
      .pipe(finalize(() => this.isAvatarLoading.set(false)))
      .subscribe({
        next: user => {
          this.sessionStore.setUser(user);
          this.snackbarService.success('Your photo has been updated.');
        },
        error: (err: HttpErrorResponse) => {
          const apiError = err.error as ApiError | null;
          this.snackbarService.error(
            apiError?.errors?.['file'] ??
              apiError?.detail ??
              'Unable to upload the photo.',
          );
        },
      });
  }

  removeAvatar(): void {
    this.dialogService
      .confirm({
        title: 'Remove photo',
        message: 'Do you want to remove your profile photo?',
        confirmText: 'Remove',
        cancelText: 'Cancel',
        variant: 'danger',
      })
      .pipe(
        exhaustMap(() => {
          this.isAvatarLoading.set(true);
          return this.profileService
            .deleteAvatar(this.user().id)
            .pipe(finalize(() => this.isAvatarLoading.set(false)));
        }),
      )
      .subscribe({
        next: () => {
          this.sessionStore.setUser({ ...this.user(), avatarVersion: null });
          this.snackbarService.success('Your photo has been removed.');
        },
        error: () => this.snackbarService.error('Unable to remove the photo.'),
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
