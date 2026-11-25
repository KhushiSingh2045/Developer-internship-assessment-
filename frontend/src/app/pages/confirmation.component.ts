import { Component, OnInit, inject } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../api.service';
import { Booking } from '../models';
import { errorMessage, saveBlob } from '../utils';

@Component({
  selector: 'app-confirmation',
  standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe],
  template: `
    @if (error) {
      <p class="alert alert-error">{{ error }}</p>
    }
    @if (booking) {
      <h1>You are booked in</h1>
      <p class="lead">A confirmation is shown below. Keep your reference safe.</p>

      <div class="stub">
        <p class="stub-label">Booking reference</p>
        <p class="stub-code">{{ booking.reference }}</p>
      </div>

      <section class="panel">
        <h2>{{ booking.hotelName }}</h2>
        <p class="muted">{{ booking.hotelLocation }}</p>
        <dl class="summary">
          <dt>Guest</dt><dd>{{ booking.guestName }} ({{ booking.guestEmail }})</dd>
          <dt>Check-in</dt><dd>{{ booking.checkIn | date: 'EEE, d MMM y' }}</dd>
          <dt>Check-out</dt><dd>{{ booking.checkOut | date: 'EEE, d MMM y' }}</dd>
          <dt>Nights</dt><dd>{{ booking.nights }}</dd>
          <dt class="total">Total</dt><dd class="total">₹{{ booking.totalAmount | number: '1.2-2' }}</dd>
        </dl>
        @if (calendarError) {
          <p class="alert alert-error">{{ calendarError }}</p>
        }
        <div class="actions">
          <button class="btn" type="button" (click)="addToCalendar()">Add to calendar</button>
          <a class="btn btn-quiet" routerLink="/manage" [queryParams]="{ reference: booking.reference, email: booking.guestEmail }">Manage this booking</a>
        </div>
      </section>
    }
  `,
})
export class ConfirmationComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private api = inject(ApiService);

  booking: Booking | null = null;
  error = '';
  calendarError = '';

  ngOnInit(): void {
    const reference = this.route.snapshot.paramMap.get('reference') ?? '';
    const email = this.route.snapshot.queryParamMap.get('email') ?? '';
    this.api.getBooking(reference, email).subscribe({
      next: (booking) => (this.booking = booking),
      error: (err) => (this.error = errorMessage(err)),
    });
  }

  addToCalendar(): void {
    if (!this.booking) return;
    this.calendarError = '';
    this.api.downloadCalendar(this.booking.reference, this.booking.guestEmail).subscribe({
      next: (blob) => saveBlob(blob, `stay-${this.booking?.reference}.ics`),
      error: () => (this.calendarError = 'Could not create the calendar file. Please try again.'),
    });
  }
}
