export enum AppointmentStatus {
  PENDING = 'PENDING',
  AWAITING_PAYMENT = 'AWAITING_PAYMENT',
  CONFIRMED = 'CONFIRMED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  REJECTED = 'REJECTED',
  EXPIRED = 'EXPIRED',
}

export interface Appointment {
  id: string;
  clientId: string;
  clientName: string;
  clientEmail: string;
  offeringId: string;
  offeringName: string;
  specialistId: string;
  specialistName: string;
  startTime: string;
  endTime: string;
  status: AppointmentStatus;
  priceAmount: number;
  paymentDeadline: string | null;
}

export interface AppointmentCreateRequest {
  offeringId: string;
  startTime: string;
}
