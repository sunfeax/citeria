import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
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
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { SnackbarService } from '../../../../shared/services/snackbar.service';
import { getFieldError } from '../../../../shared/util/form-errors';
import { routePaths } from '../../../../shared/util/route-paths';
import { LoginRequest } from '../../models/login';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    RouterLink,
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
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  /** INJECTORS */
  private authService = inject(AuthService);
  private router = inject(Router);
  private snackbarService = inject(SnackbarService);

  /** ROUTES */
  readonly routePaths = routePaths;

  /** HELPERS */
  readonly getFieldError = getFieldError;

  /** STATE */
  isSubmitted = signal<boolean>(false);
  isLoading = signal<boolean>(false);
  isPasswordVisible = signal<boolean>(false);
  serverError = signal<string | null>(null);

  /** FORM */
  loginForm = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [v.required, v.email],
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [v.required],
    }),
  });

  /** ACTIONS */
  onSubmit(): void {
    this.isPasswordVisible.set(false);
    this.isSubmitted.set(true);
    this.serverError.set(null);

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);

    const payload: LoginRequest = this.loginForm.getRawValue();

    this.authService
      .login(payload)
      .pipe(
        finalize(() => {
          this.isLoading.set(false);
        }),
      )
      .subscribe({
        next: () => {
          this.serverError.set(null);
          this.router.navigateByUrl(routePaths.profile);
        },
        error: (err: HttpErrorResponse) => {
          if (err.status === 401) {
            this.snackbarService.error('Invalid email or password.');
            return;
          } else {
            this.snackbarService.error('Unable to sign in right now. Please try later.');
          }
        },
      });
  }
}
