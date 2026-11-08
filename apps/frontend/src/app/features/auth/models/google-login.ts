import { UserType } from './user-type';

export interface GoogleLoginRequest {
  credential: string;
  type?: UserType;
}
