import { UserRole } from './user-role';
import { UserType } from './user-type';

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string | null;
  role: UserRole;
  type: UserType;
  isActive: boolean;
  avatarVersion: number | null;
  hasPassword: boolean;
  createdAt: string;
}
