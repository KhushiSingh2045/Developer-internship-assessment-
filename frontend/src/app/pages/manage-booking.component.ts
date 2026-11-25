import { Component, OnInit, inject } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../api.service';
import { Booking } from '../models';
import { errorMessage, saveBlob } from '../utils';

@Component({
  selector: 'app-manage-booking',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, DecimalPipe],
  template: `
    <h1>Manage a booking</h1>
    <p class="lead">Enter the reference from your confirmation and the email you booked with.</p>

    <form class="panel search-form" [formGroup]="form" (ngSubmit)="lookup()" novalidate>
      <label>
        Booking reference
        <input type="text" formControlName="reference" placeholder="BK-1A2B3C4D" />
        @if (c.reference.touched && c.reference.errors) {
          <span class="field-error">Enter your booking reference.</span>
        }
      </label>
      <label>
        Email address
        <input type="email" formControlName="email" />
        @if (c.email.touched && c.email.errors) {
          <span class="field-error">Enter the email you booked with.</span>
        }
      </label>
      <button class="btn" type="submit" [disabled]="loading">Find booking</button>
    </form>

    @if (error) {
      <p class="alert alert-error">{{ error }}</p>
    }
    @if (message) {
      <p class="alert alert-ok">{{ message }}</p>
    }

    @if (booking) {
      <section class="panel">
        <div class="row-between">
          <h2>{{ booking.hotelName }}</h2>
          <span class="badge" [class.badge-ok]="booking.status === 'CONFIRMED'" [class.badge-out]="booking.status === 'CANCELLED'">
            {{ booking.status === 'CONFIRMED' ? 'Confirmed' : 'Cancelled' }}
          </span>
        </div>
        <p class="muted">{{ booking.hotelLocation }}</p>
        <dl class="summary">
          <dt>Reference</dt><dd>{{ booking.reference }}</dd>
          <dt>Guest</dt><dd>{{ booking.guestName }} ({{ booking.guestEmail }})</dd>
          <dt>Check-in</dt><dd>{{ booking.checkIn | date: 'EEE, d MMM y' }}</dd>
          <dt>Check-out</dt><dd>{{ booking.checkOut | date: 'EEE, d MMM y' }}</dd>
          <dt>Nights</dt><dd>{{ booking.nights }}</dd>
          <dt class="total">Total</dt><dd class="total">₹{{ booking.totalAmount | number: '1.2-2' }}</dd>
        </dl>
        @if (booking.status === 'CONFIRMED') {
          <div class="actions">
            <button class="btn btn-quiet" type="button" (click)="addToCalendar()">Add to calendar</button>
            <button class="btn btn-danger" type="button" [disabled]="loading" (click)="cancel()">Cancel booking</button>
          </div>
        }
      </section>
    }
  `,
})
export class ManageBookingComponent implements OnInit {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private route = inject(ActivatedRoute);

  form = this.fb.nonNullable.group({
    reference: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
  });

  booking: Booking | null = null;
  loading = false;
  error = '';
  message = '';

  get c() {
    return this.form.controls;
  }

  ngOnInit(): void {
    const q = this.route.snapshot.queryParamMap;
    const reference = q.get('reference');
    const email = q.get('email');
    if (reference && email) {
      this.form.setValue({ reference, email });
      this.lookup();
    }
  }

  lookup(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { reference, email } = this.form.getRawValue();
    this.loading = true;
    this.error = '';
    this.message = '';
    this.booking = null;
    this.api.getBooking(reference, email).subscribe({
      next: (booking) => {
        this.booking = booking;
        this.loading = false;
      },
      error: (err) => {
        this.error = errorMessage(err);
        this.loading = false;
      },
    });
  }

  cancel(): void {
    if (!this.booking || !window.confirm('Cancel this booking? This cannot be undone.')) return;
    this.loading = true;
    this.error = '';
    this.api.cancelBooking(this.booking.reference, this.booking.guestEmail).subscribe({
      next: (booking) => {
        this.booking = booking;
        this.message = 'Your booking has been cancelled and the room is available to other guests again.';
        this.loading = false;
      },
      error: (err) => {
        this.error = errorMessage(err);
        this.loading = false;
      },
    });
  }

  addToCalendar(): void {
    if (!this.booking) return;
    const ref = this.booking.reference;
    this.api.downloadCalendar(ref, this.booking.guestEmail).subscribe({
      next: (blob) => saveBlob(blob, `stay-${ref}.ics`),
      error: (err) => (this.error = errorMessage(err)),
    });
  }
}
