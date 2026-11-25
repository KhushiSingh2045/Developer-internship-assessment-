import { HttpErrorResponse } from '@angular/common/http';

/** yyyy-MM-dd in the user's local time zone (what <input type="date"> uses). */
export function toIso(d: Date): string {
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${month}-${day}`;
}

export function todayIso(): string {
  return toIso(new Date());
}

export function addDays(iso: string, days: number): string {
  const [y, m, d] = iso.split('-').map(Number);
  return toIso(new Date(y, m - 1, d + days));
}

/** Turns any HTTP failure into a sentence a guest can act on. */
export function errorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return 'Cannot reach the server. Make sure the backend is running on port 8080.';
    }
    const body = err.error;
    if (body && typeof body === 'object' && typeof body.message === 'string' && body.message) {
      return body.message;
    }
  }
  return 'Something went wrong. Please try again.';
}

export function saveBlob(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
}
