import { Injectable, signal, WritableSignal, effect } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  isDarkMode: WritableSignal<boolean> = signal(false);

  constructor() {
    // Leer preferencia guardada o detectar preferencia del SO
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme) {
      this.isDarkMode.set(savedTheme === 'dark');
    } else {
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      this.isDarkMode.set(prefersDark);
    }

    // Aplicar/quitar clase al body de forma reactiva
    effect(() => {
      if (this.isDarkMode()) {
        document.body.classList.add('dark-theme');
      } else {
        document.body.classList.remove('dark-theme');
      }
    });
  }

  toggleTheme(): void {
    this.isDarkMode.update(v => !v);
    localStorage.setItem('theme', this.isDarkMode() ? 'dark' : 'light');
  }
}