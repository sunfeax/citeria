import { DestroyRef, inject, Service } from '@angular/core';
import { Observable } from 'rxjs';

export type EventType = 'login' | 'logout';

@Service()
export class TabSyncService {
  private readonly channel = new BroadcastChannel('app');

  incoming = new Observable<EventType>(subscriber => {
    const handler = (event: MessageEvent<EventType>) => subscriber.next(event.data);

    this.channel.addEventListener('message', handler);

    return () => this.channel.removeEventListener('message', handler);
  });

  constructor() {
    inject(DestroyRef).onDestroy(() => this.channel.close());
  }

  send(e: EventType): void {
    this.channel.postMessage(e);
  }
}
