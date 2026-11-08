import { Routes } from '@angular/router';
import { accessGuard } from './core/guards/access.guard';
import { MainLayoutComponent } from './shared/layout/main-layout/main-layout.component';
import { SidebarLayoutComponent } from './shared/layout/sidebar-layout/sidebar-layout.component';

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [accessGuard],
    children: [],
  },
  {
    path: '',
    component: SidebarLayoutComponent,
    canActivate: [accessGuard],
    children: [
      {
        path: 'profile',
        loadComponent: () =>
          import('./features/profile/pages/profile/profile.component').then(
            m => m.ProfileComponent,
          ),
      },
      {
        path: 'services',
        loadComponent: () =>
          import('./features/service/pages/service/service.component').then(
            m => m.ServiceComponent,
          ),
      },
    ],
  },
  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./features/auth/pages/forgot-password/forgot-password.component').then(
        m => m.ForgotPasswordComponent,
      ),
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/pages/login/login.component').then(m => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/pages/register/register.component').then(
        m => m.RegisterComponent,
      ),
  },
];
