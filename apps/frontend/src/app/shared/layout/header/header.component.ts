import { BrandComponent } from '../../components/brand/brand.component';
import { Component, inject } from '@angular/core';
import { MatIcon } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { SessionStore } from '../../../features/auth/session.store';
import { ThemeService } from '../../services/theme.service';
import { routePaths } from '../../util/route-paths';

@Component({
  selector: 'app-header',
  imports: [BrandComponent, RouterLink, MatIcon],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss',
})
export class HeaderComponent {
  protected readonly sessionStore = inject(SessionStore);
  protected readonly themeService = inject(ThemeService);
  protected readonly routePaths = routePaths;
}
