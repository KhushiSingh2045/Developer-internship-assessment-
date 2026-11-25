import { Component, OnInit, inject } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../api.service';
import { Availability } from '../models';
import { errorMessage } from '../utils';

@Component({
  selector: 'app-booking-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, DatePipe, DecimalPipe],
  template: `
    <a class="back" [routerLink]="['/hotels', hotelId]" [queryParams]="{ checkIn: checkIn, checkOut: checkOut }">&larr; Back to hotel</a>
    <h1>Confirm your booking</h1>

    @if (loading) {
      <p class="muted">Loading your stay...</p>
    }
    @if (loadError) {
      <p class="alert alert-error">{{ loadError }}</p>
    }

    @if (data) {
      <div class="two-col">
        <section class="panel">
          <h2>{{ data.hotel.name }}</h2>
          <p class="muted">{{ data.hotel.location }}</p>
          <dl class="summary">
            <dt>Check-in</dt><dd>{{ data.checkIn | date: 'EEE, d MMM y' }}</dd>
            <dt>Check-out</dt><dd>{{ data.checkOut | date: 'EEE, d MMM y' }}</dd>
            <dt>Nights</dt><dd>{{ data.nights }}</dd>
            <dt>Room price</dt><dd>₹{{ data.hotel.pricePerNight | number: '1.0-0' }} per night</dd>
            <dt class="total">Total to pay</dt><dd class="total">₹{{ data.totalAmount | number: '1.2-2' }}</dd>
          </dl>
          @if (!data.available) {
            <p class="badge badge-out">Sold out for these dates</p>
          }
        </section>

        <form class="panel" [formGroup]="form" (ngSubmit)="submit()" novalidate>
          <h2>Guest details</h2>
          <label>
            Full name
            <input type="text" formControlName="guestName" autocomplete="name" />
            @if (c.guestName.touched && c.guestName.errors) {
              <span class="field-error">Enter your name (up to 100 characters).</span>
            }
          </label>
          <label>
            Email address
            <input type="email" formControlName="guestEmail" autocomplete="email" />
            @if (c.guestEmail.touched && c.guestEmail.errors) {
              <span class="field-error">Enter a valid email address.</span>
            }
          </label>
          <p class="muted small">You will need this email and your booking reference to view or cancel the booking.</p>

          @if (submitError) {
            <p class="alert alert-error">{{ submitError }}</p>
          }
          <button class="btn" type="submit" [disabled]="submitting || !data.available">
            {{ submitting ? 'Booking...' : 'Confirm booking' }}
          </button>
        </form>
      </div>
    }
  `,
})
export class BookingFormComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private api = inject(ApiService);
  private fb = inject(FormBuilder);

  hotelId = 0;
  checkIn = '';
  checkOut = '';
  data: Availability | null = null;
  loading = false;
  loadError = '';
  submitting = false;
  submitError = '';

  form = this.fb.nonNullable.group({
    guestName: ['', [Validators.required, Validators.maxLength(100)]],
    guestEmail: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
  });

  get c() {
    return this.form.controls;
  }

  ngOnInit(): void {
    const snap = this.route.snapshot;
    this.hotelId = Number(snap.paramMap.get('id'));
    this.checkIn = snap.queryParamMap.get('checkIn') ?? '';
    this.checkOut = snap.queryParamMap.get('checkOut') ?? '';
    this.loading = true;
    this.api.getAvailability(this.hotelId, this.checkIn, this.checkOut).subscribe({
      next: (data) => {
        this.data = data;
        this.loading = false;
      },
      error: (err) => {
        this.loadError = errorMessage(err);
        this.loading = false;
      },
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (!this.data) return;
    const { guestName, guestEmail } = this.form.getRawValue();
    this.submitting = true;
    this.submitError = '';
    this.api
      .createBooking({
        hotelId: this.hotelId,
        guestName,
        guestEmail,
        checkIn: this.data.checkIn,
        checkOut: this.data.checkOut,
      })
      .subscribe({
        next: (booking) => {
          this.router.navigate(['/confirmation', booking.reference], {
            queryParams: { email: booking.guestEmail },
          });
        },
        error: (err) => {
          this.submitError = errorMessage(err);
          this.submitting = false;
        },
      });
  }
}
