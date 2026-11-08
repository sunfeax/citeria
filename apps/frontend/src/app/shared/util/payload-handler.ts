import { RegisterRequest } from '../../features/auth/models/register';
import { ChangePasswordRequest } from '../../features/profile/models/user-change-password';
import { UserUpdateRequest } from '../../features/profile/models/user-update-request';

export function getRegisterPayload(raw: RegisterRequest): RegisterRequest {
  return {
    firstName: raw.firstName.trim(),
    lastName: raw.lastName.trim(),
    email: raw.email.trim(),
    phone: raw.phone.trim(),
    password: raw.password,
    type: raw.type,
  };
}
export function getUserUpdatePayload(raw: UserUpdateRequest): UserUpdateRequest {
  return {
    firstName: raw.firstName?.trim(),
    lastName: raw.lastName?.trim(),
    email: raw.email?.trim(),
    phone: raw.phone?.trim(),
  };
}
export function getChangePasswordPayload(raw: ChangePasswordRequest): ChangePasswordRequest {
  return {
    currentPassword: raw.currentPassword.trim(),
    newPassword: raw.newPassword.trim(),
  };
}
