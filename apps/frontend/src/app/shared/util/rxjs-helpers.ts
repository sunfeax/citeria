import { Signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { debounce, of, timer } from 'rxjs';

export function debouncedSignal<T>(source: Signal<T>, ms: number): Signal<T> {
  const initial = source();
  return toSignal(
    toObservable(source).pipe(debounce(value => (value === initial ? of(0) : timer(ms)))),
    { initialValue: initial },
  );
}
