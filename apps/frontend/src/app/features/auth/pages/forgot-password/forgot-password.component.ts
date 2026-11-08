import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { routePaths } from '../../../../shared/util/route-paths';

@Component({
  selector: 'app-forgot-password',
  imports: [RouterLink],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.scss',
})
export class ForgotPasswordComponent {
  /** ROUTES */
  readonly routePaths = routePaths;
}
