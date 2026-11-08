import { UserRole } from './user-role';
import { UserType } from './user-type';

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  role: UserRole;
  type: UserType;
  isActive: boolean;
  hasAvatar: boolean;
  hasPassword: boolean;
  createdAt: string;
}
