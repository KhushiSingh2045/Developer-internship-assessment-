export interface Hotel {
  id: number;
  name: string;
  location: string;
  description: string;
  amenities: string[];
  pricePerNight: number;
  totalRooms: number;
}

export interface Availability {
  hotel: Hotel;
  checkIn: string;
  checkOut: string;
  nights: number;
  availableRooms: number;
  available: boolean;
  totalAmount: number;
}

export interface BookingRequest {
  hotelId: number;
  guestName: string;
  guestEmail: string;
  checkIn: string;
  checkOut: string;
}

export interface Booking {
  reference: string;
  hotelId: number;
  hotelName: string;
  hotelLocation: string;
  guestName: string;
  guestEmail: string;
  checkIn: string;
  checkOut: string;
  nights: number;
  totalAmount: number;
  status: 'CONFIRMED' | 'CANCELLED';
  createdAt: string;
}
