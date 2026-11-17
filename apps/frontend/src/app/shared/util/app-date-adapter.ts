import { formatDate } from '@angular/common';
import { Injectable, Provider } from '@angular/core';
import {
  DateAdapter,
  MAT_DATE_FORMATS,
  MAT_NATIVE_DATE_FORMATS,
  MatDateFormats,
  NativeDateAdapter,
} from '@angular/material/core';

const DATE_LOCALE = 'en-US';

export const APP_DATE_FORMATS: MatDateFormats = {
  parse: MAT_NATIVE_DATE_FORMATS.parse,
  display: { ...MAT_NATIVE_DATE_FORMATS.display, dateInput: 'd MMM y' },
};

@Injectable()
class AppDateAdapter extends NativeDateAdapter {
  override format(date: Date, displayFormat: object): string {
    if (typeof displayFormat === 'string') {
      return formatDate(date, displayFormat, DATE_LOCALE);
    }
    return super.format(date, displayFormat);
  }
}

export function provideAppDateAdapter(): Provider[] {
  return [
    { provide: DateAdapter, useClass: AppDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: APP_DATE_FORMATS },
  ];
}
