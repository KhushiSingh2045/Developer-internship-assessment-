import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="topbar">
      <a class="brand" routerLink="/">Harbour Stays</a>
      <nav>
        <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Find a hotel</a>
        <a routerLink="/manage" routerLinkActive="active">Manage a booking</a>
      </nav>
    </header>
    <main class="page">
      <router-outlet />
    </main>
    <footer class="footer">Demo project. No real payments or bookings are made.</footer>
  `,
})
export class AppComponent {}
