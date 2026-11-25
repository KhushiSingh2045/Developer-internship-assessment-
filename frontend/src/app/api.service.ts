import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from './config';
import { Availability, Booking, BookingRequest, Hotel } from './models';

/** Thin wrapper around the REST API so components never build URLs themselves. */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);

  searchHotels(location: string): Observable<Hotel[]> {
    let params = new HttpParams();
    if (location.trim()) {
      params = params.set('location', location.trim());
    }
    return this.http.get<Hotel[]>(`${API_URL}/hotels`, { params });
  }

  getAvailability(hotelId: number, checkIn: string, checkOut: string): Observable<Availability> {
    const params = new HttpParams().set('checkIn', checkIn).set('checkOut', checkOut);
    return this.http.get<Availability>(`${API_URL}/hotels/${hotelId}/availability`, { params });
  }

  createBooking(request: BookingRequest): Observable<Booking> {
    return this.http.post<Booking>(`${API_URL}/bookings`, request);
  }

  getBooking(reference: string, email: string): Observable<Booking> {
    return this.http.get<Booking>(`${this.bookingUrl(reference)}${this.emailQuery(email)}`);
  }

  cancelBooking(reference: string, email: string): Observable<Booking> {
    return this.http.post<Booking>(`${this.bookingUrl(reference)}/cancel${this.emailQuery(email)}`, null);
  }

  downloadCalendar(reference: string, email: string): Observable<Blob> {
    return this.http.get(`${this.bookingUrl(reference)}/calendar${this.emailQuery(email)}`, {
      responseType: 'blob',
    });
  }

  private bookingUrl(reference: string): string {
    return `${API_URL}/bookings/${encodeURIComponent(reference.trim())}`;
  }

  // encodeURIComponent keeps a "+" in an email address from being read as a space.
  private emailQuery(email: string): string {
    return `?email=${encodeURIComponent(email.trim())}`;
  }
}
