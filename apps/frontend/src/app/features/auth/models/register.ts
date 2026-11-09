import { UserType } from './user-type';
import { User } from './user';

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone: string | null;
  password: string;
  type: UserType;
}

export type RegisterResponse = User;
