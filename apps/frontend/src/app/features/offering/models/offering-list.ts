export interface OfferingList {
  id: string;
  specialistId: string;
  specialistName: string;
  specialistAvatarVersion: number | null;
  name: string;
  description: string;
  priceAmount: number;
  durationMinutes: number;
  currency: string;
  isActive: boolean;
  createdAt: string;
}

export interface OfferingFilters {
  search: string;
  minPrice: number | null;
  maxPrice: number | null;
  active: boolean | null;
}
