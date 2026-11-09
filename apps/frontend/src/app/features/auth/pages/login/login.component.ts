import { BrandComponent } from '../../../../shared/components/brand/brand.component';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
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
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { SnackbarService } from '../../../../shared/services/snackbar.service';
import { getFieldError } from '../../../../shared/util/form-errors';
import { routePaths } from '../../../../shared/util/route-paths';
import { GoogleSignInComponent } from '../../components/google-sign-in/google-sign-in.component';
import { AuthService } from '../../services/auth.service';
import { GoogleIdentityService } from '../../services/google-identity.service';

@Component({
  selector: 'app-login',
  imports: [
    BrandComponent,
    FormField,
    FormRoot,
    GoogleSignInComponent,
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

  /** GOOGLE */
  readonly isGoogleEnabled = inject(GoogleIdentityService).isEnabled;

  /** ROUTES */
  readonly routePaths = routePaths;

  /** HELPERS */
  readonly getFieldError = getFieldError;

  /** STATE */
  isPasswordVisible = signal<boolean>(false);

  /** FORM */
  loginModel = signal({ email: '', password: '' });
  loginForm = form(
    this.loginModel,
    s => {
      required(s.email);
      email(s.email);
      required(s.password);
    },
    {
      submission: {
        action: async () => {
          this.isPasswordVisible.set(false);

          try {
            await firstValueFrom(this.authService.login(this.loginModel()));
            this.router.navigateByUrl(routePaths.offerings);
          } catch (err) {
            this.snackbarService.error(
              (err as HttpErrorResponse).status === 401
                ? 'Invalid email or password.'
                : 'Unable to sign in right now. Please try later.',
            );
          }
        },
      },
    },
  );
}
