import { Component, signal, WritableSignal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { ThemeService } from './services/theme.service';
import { OverlayContainer } from '@angular/cdk/overlay';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App {
  mostrarNavbar: WritableSignal<boolean> = signal(true);
  mostrarFooter: WritableSignal<boolean> = signal(true);

  constructor(
    private router: Router,
    private themeService: ThemeService,
    private overlayContainer: OverlayContainer
  ) {
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      const url = event.urlAfterRedirects;
      const esPaginaAutenticacion = ['/login', '/recuperar-contrasena'].includes(url);
      const esPanelDeControl = url.startsWith('/panel');

      this.mostrarNavbar.set(!esPaginaAutenticacion && !esPanelDeControl);
      this.mostrarFooter.set(!esPaginaAutenticacion && !esPanelDeControl);
    });

    // Aplicar dark-theme al body Y al cdk-overlay-container (para mat-select, etc.)
    effect(() => {
      const overlayEl = this.overlayContainer.getContainerElement();
      if (this.themeService.isDarkMode()) {
        document.body.classList.add('dark-theme');
        overlayEl.classList.add('dark-theme');
      } else {
        document.body.classList.remove('dark-theme');
        overlayEl.classList.remove('dark-theme');
      }
    });
  }
}