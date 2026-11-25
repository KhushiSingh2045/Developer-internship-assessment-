import { Component, OnInit, inject } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../api.service';
import { Hotel } from '../models';
import { addDays, errorMessage, todayIso } from '../utils';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, DecimalPipe],
  template: `
    <h1>Where are you staying?</h1>
    <p class="lead">Pick your dates, then choose a hotel. Prices are per night.</p>

    <form class="panel search-form" [formGroup]="form" (ngSubmit)="search()">
      <label>
        Location
        <input type="text" formControlName="location" placeholder="e.g. Goa, Jaipur" />
      </label>
      <label>
        Check-in
        <input type="date" formControlName="checkIn" [min]="today" />
      </label>
      <label>
        Check-out
        <input type="date" formControlName="checkOut" [min]="today" />
      </label>
      <button class="btn" type="submit" [disabled]="loading">Search</button>
    </form>

    @if (dateError) {
      <p class="alert alert-warn">{{ dateError }}</p>
    }
    @if (error) {
      <p class="alert alert-error">{{ error }}</p>
    }
    @if (loading) {
      <p class="muted">Loading hotels...</p>
    }
    @if (!loading && !error && hotels.length === 0) {
      <p class="empty">No hotels match that location. Clear the location box to see every hotel.</p>
    }

    <div class="hotel-list">
      @for (hotel of hotels; track hotel.id) {
        <article class="hotel panel">
          <div class="hotel-main">
            <h2>{{ hotel.name }}</h2>
            <p class="muted">{{ hotel.location }}</p>
            <p>{{ hotel.description }}</p>
            <ul class="chips">
              @for (a of hotel.amenities; track a) {
                <li>{{ a }}</li>
              }
            </ul>
          </div>
          <div class="hotel-side">
            <p class="price">₹{{ hotel.pricePerNight | number: '1.0-0' }}<span> / night</span></p>
            @if (dateError) {
              <button class="btn" type="button" disabled>View rooms</button>
            } @else {
              <a class="btn" [routerLink]="['/hotels', hotel.id]" [queryParams]="dates">View rooms</a>
            }
          </div>
        </article>
      }
    </div>
  `,
})
export class SearchComponent implements OnInit {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);

  today = todayIso();
  form = this.fb.nonNullable.group({
    location: [''],
    checkIn: [addDays(todayIso(), 1)],
    checkOut: [addDays(todayIso(), 2)],
  });

  hotels: Hotel[] = [];
  loading = false;
  error = '';

  get dates(): { checkIn: string; checkOut: string } {
    const v = this.form.getRawValue();
    return { checkIn: v.checkIn, checkOut: v.checkOut };
  }

  get dateError(): string {
    const { checkIn, checkOut } = this.dates;
    if (!checkIn || !checkOut) return 'Choose both a check-in and a check-out date.';
    if (checkIn < this.today) return 'Check-in cannot be in the past.';
    if (checkOut <= checkIn) return 'Check-out must be after check-in.';
    return '';
  }

  ngOnInit(): void {
    this.search();
  }

  search(): void {
    this.loading = true;
    this.error = '';
    this.api.searchHotels(this.form.getRawValue().location).subscribe({
      next: (hotels) => {
        this.hotels = hotels;
        this.loading = false;
      },
      error: (err) => {
        this.hotels = [];
        this.error = errorMessage(err);
        this.loading = false;
      },
    });
  }
}
