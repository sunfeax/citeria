export interface FaqItem {
  question: string;
  answer: string;
  isPopular?: boolean;
}

export interface FaqSection {
  id: string;
  title: string;
  items: FaqItem[];
}

export const SUPPORT_EMAIL = 'support@sunfeax.com';

export const FAQ_SECTIONS: FaqSection[] = [
  {
    id: 'booking',
    title: 'Booking',
    items: [
      {
        question: 'How do I book a visit?',
        isPopular: true,
        answer:
          'Open a specialist from Find specialists, choose a service, a day and a free time, then select Book. Booking requires a client account.',
      },
      {
        question: 'How far ahead can I book?',
        answer:
          'From tomorrow up to four weeks ahead. Times are shown in the booking time zone noted under the list of times.',
      },
      {
        question: 'What happens after I send a request?',
        answer:
          'The specialist reviews it and accepts or rejects it. Until then the time is not reserved, so other clients can request it too. You can have up to 10 requests waiting at once.',
      },
      {
        question: 'Why are some days greyed out?',
        answer:
          'The specialist does not work on those days, or every time on them is already taken.',
      },
    ],
  },
  {
    id: 'payment',
    title: 'Payment and cancellation',
    items: [
      {
        question: 'When do I pay?',
        isPopular: true,
        answer:
          'Only after the specialist accepts your request. You then have 24 hours to pay, and no later than 6 hours before the visit, whichever comes first. A request that is not paid in time expires.',
      },
      {
        question: 'Is the payment real?',
        isPopular: true,
        answer:
          'No. Citeria is a demo project: payment is simulated and no money is charged.',
      },
      {
        question: 'Can I cancel a booking?',
        isPopular: true,
        answer:
          'Yes, at any time before the visit: a request waiting for the specialist, an accepted request waiting for payment, or a confirmed visit. Open it in My bookings and select Cancel.',
      },
    ],
  },
  {
    id: 'specialists',
    title: 'For specialists',
    items: [
      {
        question: 'How do clients find me?',
        isPopular: true,
        answer:
          'Add at least one service and your working hours. Your active services appear in the catalog, and clients can book free times within your hours.',
      },
      {
        question: 'How do I handle requests?',
        answer:
          'New requests appear in Bookings. Accept or reject each one. Once the client pays, the visit is confirmed. After the visit, mark it as completed.',
      },
      {
        question: 'Can I book another specialist?',
        answer:
          'No. Each account has one role. To book a visit, use a separate client account.',
      },
    ],
  },
  {
    id: 'account',
    title: 'Account',
    items: [
      {
        question: 'Can I sign in with Google?',
        answer:
          'Yes. If your Google email matches an existing account, you are signed in to that account.',
      },
      {
        question: 'How do I change my details or photo?',
        answer:
          'Open Settings from your name at the bottom of the sidebar. A specialist can also add an About text there.',
      },
      {
        question: 'I forgot my password. What now?',
        answer:
          'Password reset by email is not available yet. Write to support from the email of your account and we will help you get back in.',
      },
      {
        question: 'How do I delete my account?',
        answer:
          'Go to Settings, open Account and select Delete account. You are signed out on all devices.',
      },
    ],
  },
];

export const POPULAR_FAQ: FaqItem[] = FAQ_SECTIONS.flatMap(section =>
  section.items.filter(item => item.isPopular),
);
