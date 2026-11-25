import { Component, OnInit, inject } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../api.service';
import { Availability } from '../models';
import { addDays, errorMessage, todayIso } from '../utils';

@Component({
  selector: 'app-hotel-details',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, DatePipe, DecimalPipe],
  template: `
    <a class="back" routerLink="/">&larr; All hotels</a>

    <form class="panel date-form" [formGroup]="form" (ngSubmit)="load()">
      <label>
        Check-in
        <input type="date" formControlName="checkIn" [min]="today" />
      </label>
      <label>
        Check-out
        <input type="date" formControlName="checkOut" [min]="today" />
      </label>
      <button class="btn btn-quiet" type="submit" [disabled]="loading">Update dates</button>
    </form>

    @if (loading) {
      <p class="muted">Checking availability...</p>
    }
    @if (error) {
      <p class="alert alert-error">{{ error }}</p>
    }

    @if (data) {
      <section class="panel details">
        <h1>{{ data.hotel.name }}</h1>
        <p class="muted">{{ data.hotel.location }}</p>
        <p>{{ data.hotel.description }}</p>
        <ul class="chips">
          @for (a of data.hotel.amenities; track a) {
            <li>{{ a }}</li>
          }
        </ul>
      </section>

      <section class="panel stay">
        <h2>Your stay</h2>
        <dl class="summary">
          <dt>Check-in</dt><dd>{{ data.checkIn | date: 'EEE, d MMM y' }}</dd>
          <dt>Check-out</dt><dd>{{ data.checkOut | date: 'EEE, d MMM y' }}</dd>
          <dt>Nights</dt><dd>{{ data.nights }}</dd>
          <dt>Room price</dt><dd>₹{{ data.hotel.pricePerNight | number: '1.0-0' }} per night</dd>
          <dt class="total">Total</dt><dd class="total">₹{{ data.totalAmount | number: '1.2-2' }}</dd>
        </dl>

        @if (!data.available) {
          <p class="badge badge-out">Sold out for these dates</p>
          <p class="muted">Try moving your check-in or check-out date.</p>
        } @else {
          @if (data.availableRooms <= 2) {
            <p class="badge badge-low">Only {{ data.availableRooms }} {{ data.availableRooms === 1 ? 'room' : 'rooms' }} left</p>
          } @else {
            <p class="badge badge-ok">{{ data.availableRooms }} rooms available</p>
          }
          <a class="btn" [routerLink]="['/book', data.hotel.id]"
             [queryParams]="{ checkIn: data.checkIn, checkOut: data.checkOut }">Book this room</a>
        }
      </section>
    }
  `,
})
export class HotelDetailsComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private api = inject(ApiService);
  private fb = inject(FormBuilder);

  today = todayIso();
  hotelId = 0;
  form = this.fb.nonNullable.group({ checkIn: [''], checkOut: [''] });
  data: Availability | null = null;
  loading = false;
  error = '';

  ngOnInit(): void {
    const snap = this.route.snapshot;
    this.hotelId = Number(snap.paramMap.get('id'));
    this.form.setValue({
      checkIn: snap.queryParamMap.get('checkIn') ?? addDays(this.today, 1),
      checkOut: snap.queryParamMap.get('checkOut') ?? addDays(this.today, 2),
    });
    this.load();
  }

  load(): void {
    const { checkIn, checkOut } = this.form.getRawValue();
    this.loading = true;
    this.error = '';
    this.data = null;
    this.api.getAvailability(this.hotelId, checkIn, checkOut).subscribe({
      next: (data) => {
        this.data = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = errorMessage(err);
        this.loading = false;
      },
    });
  }
}
