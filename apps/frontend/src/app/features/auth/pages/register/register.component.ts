import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  FormGroupDirective,
  NgForm,
  ReactiveFormsModule,
  Validators as v,
  ValidationErrors,
  ValidatorFn,
} from '@angular/forms';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatButtonToggle, MatButtonToggleGroup } from '@angular/material/button-toggle';
import { ErrorStateMatcher } from '@angular/material/core';
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
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ApiError } from '../../../../shared/models/api-error';
import { SnackbarService } from '../../../../shared/services/snackbar.service';
import {
  applyServerErrors,
  clearServerErrors,
  getFieldError,
} from '../../../../shared/util/form-errors';
import { UserType } from '../../models/user-type';
import { GoogleSignInComponent } from '../../components/google-sign-in/google-sign-in.component';
import { AuthService } from '../../services/auth.service';
import { GoogleIdentityService } from '../../services/google-identity.service';
import { routePaths } from '../../../../shared/util/route-paths';
import { getRegisterPayload } from './../../../../shared/util/payload-handler';

@Component({
  selector: 'app-register',
  imports: [
    ReactiveFormsModule,
    GoogleSignInComponent,
    RouterLink,
    MatButton,
    MatIconButton,
    MatButtonToggleGroup,
    MatButtonToggle,
    MatFormField,
    MatLabel,
    MatError,
    MatSuffix,
    MatInput,
    MatIcon,
    MatProgressSpinner,
    MatTooltip,
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent {
  /** INJECTORS */
  private authService = inject(AuthService);
  private snackbarService = inject(SnackbarService);
  private router = inject(Router);

  /** GOOGLE */
  readonly isGoogleEnabled = inject(GoogleIdentityService).isEnabled;

  readonly UserType = UserType;
  readonly routePaths = routePaths;

  /** TEMPLATE HELPERS */
  readonly getFieldError = getFieldError;
  readonly confirmPasswordMatcher = new ConfirmPasswordErrorMatcher();
  readonly passwordRequirements = [
    'Only Latin letters',
    'At least 8 characters',
    'At least one digit',
    'At least one uppercase Latin letter',
    'At least one special character: @#$%^&+=!',
    'No spaces',
  ].join('\n');

  /** STATE */
  isLoading = signal<boolean>(false);
  isPasswordVisible = signal<boolean>(false);
  isConfirmPasswordVisible = signal<boolean>(false);

  /** FORM */
  registerForm = new FormGroup(
    {
      firstName: new FormControl('', {
        nonNullable: true,
        validators: [
          v.required,
          v.minLength(2),
          v.maxLength(50),
          v.pattern(/^[\p{L}]+(?:[\s'-][\p{L}]+)*$/u),
        ],
      }),
      lastName: new FormControl('', {
        nonNullable: true,
        validators: [
          v.required,
          v.minLength(2),
          v.maxLength(50),
          v.pattern(/^[\p{L}]+(?:[\s'-][\p{L}]+)*$/u),
        ],
      }),
      email: new FormControl('', {
        nonNullable: true,
        validators: [v.required, v.email],
      }),
      phone: new FormControl('', {
        nonNullable: true,
        validators: [v.required, v.minLength(7), v.maxLength(20), v.pattern(/^\+?\d+$/)],
      }),
      password: new FormControl('', {
        nonNullable: true,
        validators: [
          v.required,
          v.minLength(8),
          v.pattern(/^(?=.*[A-Z])(?=.*[@#$%^&+=!])[A-Za-z0-9@#$%^&+=!]+$/),
        ],
      }),
      confirmPassword: new FormControl('', {
        nonNullable: true,
        validators: [v.required],
      }),
      type: new FormControl(UserType.CLIENT, {
        nonNullable: true,
        validators: [v.required],
      }),
    },
    {
      validators: [this.passwordComparator()],
    },
  );

  /** ACTIONS */
  onSubmit(): void {
    this.isPasswordVisible.set(false);
    this.isConfirmPasswordVisible.set(false);
    clearServerErrors(this.registerForm);

    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.authService
      .register(getRegisterPayload(this.registerForm.getRawValue()))
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
        }),
      )
      .subscribe({
        next: () => {
          this.router.navigateByUrl(routePaths.login);
        },
        error: (err: HttpErrorResponse) => {
          const apiError = err.error as ApiError;
          if (apiError.errors) {
            applyServerErrors(this.registerForm, apiError.errors);
          } else {
            this.snackbarService.error(apiError.detail ?? 'Registration failed.');
          }
        },
      });
  }

  /** HELPERS */
  passwordComparator(): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      const { password, confirmPassword } = group.value;
      if (!password || !confirmPassword) return null;
      return password === confirmPassword ? null : { passwordMismatch: true };
    };
  }
}

class ConfirmPasswordErrorMatcher implements ErrorStateMatcher {
  isErrorState(
    control: AbstractControl | null,
    form: FormGroupDirective | NgForm | null,
  ): boolean {
    if (!control) return false;
    const isInteracted = control.touched || !!form?.submitted;
    return (
      isInteracted && (control.invalid || !!control.parent?.hasError('passwordMismatch'))
    );
  }
}
