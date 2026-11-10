import { Routes } from '@angular/router';
import { accessGuard } from './core/guards/access.guard';
import { MainLayoutComponent } from './shared/layout/main-layout/main-layout.component';
import { SidebarLayoutComponent } from './shared/layout/sidebar-layout/sidebar-layout.component';

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        title: 'Citeria — Find your next specialist',
        loadComponent: () =>
          import('./features/home/pages/home/home.component').then(m => m.HomeComponent),
      },
    ],
  },
  {
    path: '',
    component: SidebarLayoutComponent,
    children: [
      {
        path: 'profile',
        canActivate: [accessGuard],
        loadComponent: () =>
          import('./features/profile/pages/profile/profile.component').then(
            m => m.ProfileComponent,
          ),
      },
      {
        path: 'offerings',
        loadComponent: () =>
          import('./features/offering/pages/offering/offering.component').then(
            m => m.OfferingComponent,
          ),
      },
      {
        path: 'specialists/:id',
        loadComponent: () =>
          import('./features/specialist/pages/specialist/specialist.component').then(
            m => m.SpecialistComponent,
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
