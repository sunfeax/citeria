import { OfferingList } from '../../offering/models/offering-list';

export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY';

export interface WorkingHours {
  id: string;
  dayOfWeek: DayOfWeek;
  startTime: string;
  endTime: string;
  isActive: boolean;
}

export interface SpecialistDetail {
  id: string;
  firstName: string;
  lastName: string;
  about: string | null;
  avatarVersion: number | null;
  isActive: boolean;
  offerings: OfferingList[];
  workingHours: WorkingHours[];
  timeZone: string;
}
