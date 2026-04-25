import { Component, OnInit, signal, ViewChild } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { HistoriaClinicaService } from '../../../../services/historia-clinica';
import { HistoriaClinica } from '../../../../core/models/historia-clinica';
import { Spinner } from '../../../../shared/spinner/spinner';
import { RouterModule } from '@angular/router';

import { MatCardModule } from '@angular/material/card';
import { MatAccordion, MatExpansionModule } from '@angular/material/expansion';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-mi-historial',
  standalone: true,
  imports: [
    CommonModule, Spinner, RouterModule, DatePipe, MatCardModule,
    MatExpansionModule, MatIconModule, MatDividerModule, MatButtonModule
  ],
  templateUrl: './mi-historial.html',
  styleUrls: ['./mi-historial.css', './mi-historial.print.css']
})
export class MiHistorial implements OnInit {
  historia = signal<HistoriaClinica | null>(null);
  cargando = signal(true);
  error = signal<string | null>(null);

  @ViewChild(MatAccordion) accordion!: MatAccordion;

  constructor(private historiaService: HistoriaClinicaService) {}

  ngOnInit(): void {
    this.cargarHistorial();
  }

  cargarHistorial(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.historiaService.getMiHistorial().subscribe({
      next: (data) => {
        this.historia.set(data);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar tu historial clínico. Es posible que aún no tengas uno generado.');
        this.cargando.set(false);
      }
    });
  }

  imprimirHistorial(): void {
    if (this.accordion) {
      this.accordion.openAll();

      setTimeout(() => {
        window.print();
        this.accordion.closeAll();
      }, 500);
    } else {
      window.print();
    }
  }
}