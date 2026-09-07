import { Component, Signal, computed, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Cita } from '../../../../core/models/cita';
import { CitaService } from '../../../../services/cita';
import { FormularioTriaje } from '../../componentes-panel/formulario-triaje/formulario-triaje';
import { Triaje } from '../../../../core/models/triaje';

@Component({
  selector: 'app-gestion-triaje',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule, DatePipe],
  templateUrl: './gestion-triaje.html',
  styleUrls: ['./gestion-triaje.css']
})
export class GestionTriaje implements OnInit {

  fechaHoy = new Date();

  citasParaTriaje: Signal<Cita[]>;
  citasAtendidas: Signal<number>;

  constructor(
    private citaService: CitaService,
    public dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.citasParaTriaje = computed(() => {
      const hoy = new Date();
      return this.citaService.citas().filter(c => {
        const d = new Date(c.fechaHora);
        const esHoy =
          d.getDate() === hoy.getDate() &&
          d.getMonth() === hoy.getMonth() &&
          d.getFullYear() === hoy.getFullYear();
        // Mostrar las que están en espera/programadas O ya en triaje (lista_consulta) para ver estado completo
        return esHoy && (c.estado === 'programada' || c.estado === 'en_espera' || c.estado === 'lista_consulta');
      }).sort((a, b) => (a.numeroTurno || 999) - (b.numeroTurno || 999));
    });

    this.citasAtendidas = computed(() => {
      const hoy = new Date();
      return this.citaService.citas().filter(c => {
        const d = new Date(c.fechaHora);
        return d.getDate() === hoy.getDate() &&
               d.getMonth() === hoy.getMonth() &&
               d.getFullYear() === hoy.getFullYear() &&
               (c.estado === 'lista_consulta' || c.estado === 'completada');
      }).length;
    });
  }

  ngOnInit(): void {
    this.citaService.cargarCitasPanel();
  }

  onRegistrarTriaje(cita: Cita) {
    const dialogRef = this.dialog.open(FormularioTriaje, {
      width: '700px',
      maxHeight: '90vh',
      disableClose: true,
      panelClass: 'custom-dialog-container',
      data: { cita }
    });

    dialogRef.afterClosed().subscribe((resultado: Triaje) => {
      if (resultado) {
        this.citaService.registrarTriaje(cita.id, resultado).subscribe({
          next: () => {
            this.snackBar.open(
              `✅ Triaje registrado · ${cita.paciente.nombres} listo para consulta`,
              'Cerrar',
              { duration: 4000, panelClass: ['notificacion-exito'] }
            );
            this.citaService.cargarCitasPanel();
          },
          error: (err: any) => {
            const msg = err?.error?.detail || err?.error?.message || 'Error al registrar el triaje';
            this.snackBar.open(`❌ ${msg}`, 'Cerrar', { duration: 4000, panelClass: ['notificacion-error'] });
            console.error(err);
          }
        });
      }
    });
  }
}
