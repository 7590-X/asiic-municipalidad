import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ClarityModule } from '@clr/angular';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, ClarityModule],
  template: `
    <router-outlet />
  `,
})
export class AppComponent {}
