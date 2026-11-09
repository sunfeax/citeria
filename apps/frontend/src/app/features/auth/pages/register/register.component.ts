import { authReturnUrl } from '../../../../shared/util/auth-return-url';
import { BrandComponent } from '../../../../shared/components/brand/brand.component';
import { Component, inject, signal } from '@angular/core';
import {
  email,
  form,
  FormField,
  FormRoot,
  required,
  validate,
} from '@angular/forms/signals';
import { MatButton, MatIconButton } from '@angular/material/button';
import { MatButtonToggle, MatButtonToggleGroup } from '@angular/material/button-toggle';
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
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { SnackbarService } from '../../../../shared/services/snackbar.service';
import { getFieldError, handleSubmitError } from '../../../../shared/util/form-errors';
import {
  newPasswordRules,
  personNameRules,
  phoneRules,
} from '../../../../shared/util/form-rules';
import { UserType } from '../../models/user-type';
import { GoogleSignInComponent } from '../../components/google-sign-in/google-sign-in.component';
import { AuthService } from '../../services/auth.service';
import { GoogleIdentityService } from '../../services/google-identity.service';
import { routePaths } from '../../../../shared/util/route-paths';
import { getRegisterPayload } from './../../../../shared/util/payload-handler';

@Component({
  selector: 'app-register',
  imports: [
    BrandComponent,
    FormField,
    FormRoot,
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
  readonly returnUrl = authReturnUrl(
    inject(ActivatedRoute).snapshot.queryParamMap.get('returnUrl'),
  );

  /** TEMPLATE HELPERS */
  readonly getFieldError = getFieldError;
  readonly passwordRequirements = [
    'Only Latin letters',
    'At least 8 characters',
    'At least one digit',
    'At least one uppercase Latin letter',
    'At least one special character: @#$%^&+=!',
    'No spaces',
  ].join('\n');

  /** STATE */
  isPasswordVisible = signal<boolean>(false);
  isConfirmPasswordVisible = signal<boolean>(false);

  /** FORM */
  registerModel = signal({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    type: UserType.CLIENT as UserType,
  });
  registerForm = form(
    this.registerModel,
    s => {
      personNameRules(s.firstName);
      personNameRules(s.lastName);
      required(s.email);
      email(s.email);
      phoneRules(s.phone);
      newPasswordRules(s.password);
      required(s.confirmPassword);
      validate(s.confirmPassword, ({ value, valueOf }) =>
        value() && valueOf(s.password) && value() !== valueOf(s.password)
          ? { kind: 'passwordMismatch', message: "Passwords don't match" }
          : undefined,
      );
      required(s.type);
    },
    {
      submission: {
        action: async field => {
          this.isPasswordVisible.set(false);
          this.isConfirmPasswordVisible.set(false);

          try {
            await firstValueFrom(
              this.authService.register(getRegisterPayload(this.registerModel())),
            );
            this.router.navigate([routePaths.login], {
              queryParams: { returnUrl: this.returnUrl },
            });
          } catch (err) {
            return handleSubmitError(field, err, 'Registration failed.', message =>
              this.snackbarService.error(message),
            );
          }
          return undefined;
        },
      },
    },
  );
}
