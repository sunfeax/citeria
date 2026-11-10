import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HomeFaqComponent } from '../../components/home-faq/home-faq.component';
import { SessionStore } from '../../../auth/session.store';
import { routePaths } from '../../../../shared/util/route-paths';

const FIRST_HOUR = 9;
const LAST_HOUR = 18;

interface BoardEntry {
  day: number;
  start: number;
  span: number;
  label: string;
  isOwn?: boolean;
  isWideOnly?: boolean;
}

function formatSlot(halfHours: number): string {
  const minutes = FIRST_HOUR * 60 + halfHours * 30;
  const hours = String(Math.floor(minutes / 60)).padStart(2, '0');
  return `${hours}:${minutes % 60 === 0 ? '00' : '30'}`;
}

@Component({
  selector: 'app-home',
  imports: [RouterLink, HomeFaqComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent {
  /** INJECTORS */
  protected readonly sessionStore = inject(SessionStore);

  /** ROUTES */
  protected readonly routePaths = routePaths;

  /** DATA */
  protected readonly days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
  protected readonly hours = Array.from(
    { length: LAST_HOUR - FIRST_HOUR },
    (_, index) => `${String(FIRST_HOUR + index).padStart(2, '0')}:00`,
  );
  protected readonly entries = (
    [
      { day: 1, start: 1, span: 2, label: 'Haircut' },
      { day: 1, start: 6, span: 3, label: 'Yoga class' },
      { day: 1, start: 12, span: 3, label: 'Tax advice' },
      { day: 2, start: 2, span: 2, label: 'Piano lesson' },
      { day: 2, start: 8, span: 4, label: 'Massage' },
      { day: 2, start: 14, span: 2, label: 'Coaching' },
      { day: 3, start: 0, span: 2, label: 'Dental check-up' },
      { day: 3, start: 4, span: 3, label: 'English lesson' },
      { day: 3, start: 10, span: 3, label: 'Physio', isOwn: true },
      { day: 3, start: 15, span: 2, label: 'Coaching' },
      { day: 4, start: 3, span: 3, label: 'Legal advice', isWideOnly: true },
      { day: 4, start: 9, span: 2, label: 'Nutrition', isWideOnly: true },
      { day: 4, start: 13, span: 2, label: 'Haircut', isWideOnly: true },
      { day: 5, start: 1, span: 4, label: 'Massage', isWideOnly: true },
      { day: 5, start: 7, span: 2, label: 'English lesson', isWideOnly: true },
      { day: 5, start: 12, span: 3, label: 'Coaching', isWideOnly: true },
      { day: 6, start: 2, span: 3, label: 'Yoga class', isWideOnly: true },
      { day: 6, start: 7, span: 2, label: 'Haircut', isWideOnly: true },
      { day: 7, start: 5, span: 2, label: 'Piano lesson', isWideOnly: true },
    ] satisfies BoardEntry[]
  ).map(entry => ({
    ...entry,
    time: `${formatSlot(entry.start)}–${formatSlot(entry.start + entry.span)}`,
  }));
  protected readonly steps = [
    {
      title: 'Find a specialist',
      description: 'Search by service and compare prices, length and descriptions.',
    },
    {
      title: 'Pick a free time',
      description: 'You only see the times when the specialist is available.',
    },
    {
      title: 'Get confirmed',
      description: 'The specialist accepts your request, then you pay to keep the time.',
    },
  ];
}
