import { Routes } from '@angular/router';
import { SearchComponent } from './pages/search.component';
import { HotelDetailsComponent } from './pages/hotel-details.component';
import { BookingFormComponent } from './pages/booking-form.component';
import { ConfirmationComponent } from './pages/confirmation.component';
import { ManageBookingComponent } from './pages/manage-booking.component';

export const routes: Routes = [
  { path: '', component: SearchComponent },
  { path: 'hotels/:id', component: HotelDetailsComponent },
  { path: 'book/:id', component: BookingFormComponent },
  { path: 'confirmation/:reference', component: ConfirmationComponent },
  { path: 'manage', component: ManageBookingComponent },
  { path: '**', redirectTo: '' },
];
