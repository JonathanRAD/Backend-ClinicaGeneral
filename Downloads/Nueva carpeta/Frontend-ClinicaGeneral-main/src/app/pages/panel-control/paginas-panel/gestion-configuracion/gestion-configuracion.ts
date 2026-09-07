import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { FormsModule } from '@angular/forms';
import { ThemeService } from '../../../../services/theme.service';

@Component({
  selector: 'app-gestion-configuracion',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatSlideToggleModule
  ],
  templateUrl: './gestion-configuracion.html',
  styleUrls: ['./gestion-configuracion.css']
})
export class GestionConfiguracion {
  isDarkMode: boolean;

  constructor(public themeService: ThemeService) {
    this.isDarkMode = this.themeService.isDarkMode();
  }

  onThemeChange() {
    this.themeService.toggleTheme();
    this.isDarkMode = this.themeService.isDarkMode();
  }
}