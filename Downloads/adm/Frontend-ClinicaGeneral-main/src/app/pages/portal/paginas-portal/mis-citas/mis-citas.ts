
import { Component, OnInit, signal, computed, Signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { CitaService } from '../../../../services/cita';
import { Cita } from '../../../../core/models/cita';
import { Spinner } from '../../../../shared/spinner/spinner';
import { RouterModule } from '@angular/router';
import { DialogoConfirmacion } from '../../../panel-control/componentes-panel/dialogo-confirmacion/dialogo-confirmacion';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Notificacion } from '../../../../services/notificacion';
import { HttpErrorResponse } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatRippleModule } from '@angular/material/core';
import { MatTabsModule } from '@angular/material/tabs';
import { saveAs } from 'file-saver';

@Component({
  selector: 'app-mis-citas',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    Spinner,
    RouterModule,
    MatDialogModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatDividerModule,
    MatRippleModule,
    MatTabsModule 
  ],
  templateUrl: './mis-citas.html',
  styleUrls: ['./mis-citas.css']
})
export class MisCitas implements OnInit {
  citas = signal<Cita[]>([]);
  cargando = signal<boolean>(true);
  error = signal<string | null>(null);
  citaExpandida = signal<string | null>(null);
  citasProximas: Signal<Cita[]>;
  citasPasadas: Signal<Cita[]>;

   constructor(
    private citaService: CitaService,
    private dialog: MatDialog,
    private notificacion: Notificacion
  ) {
    const ahora = new Date();

    this.citasProximas = computed(() => 
      this.citas()
        .filter(c => new Date(c.fechaHora) >= ahora && c.estado.toLowerCase() === 'programada')
        .sort((a, b) => new Date(a.fechaHora).getTime() - new Date(b.fechaHora).getTime())
    );
    this.citasPasadas = computed(() => 
      this.citas()
        .filter(c => new Date(c.fechaHora) < ahora || c.estado.toLowerCase() !== 'programada')
        .sort((a, b) => new Date(b.fechaHora).getTime() - new Date(a.fechaHora).getTime())
    );
  }


  ngOnInit(): void {
    this.cargarMisCitas();
  }

  toggleCita(id: string): void {
    this.citaExpandida.set(this.citaExpandida() === id ? null : id);
  }

  cargarMisCitas(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.citaService.getMisCitas().subscribe({
      next: (data: Cita[]) => {
        this.citas.set(data);
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set('No se pudieron cargar tus citas. Por favor, intenta más tarde.');
        this.cargando.set(false);
      }
    });
  }

  confirmarCancelacion(event: MouseEvent, cita: Cita): void {
    event.stopPropagation(); 
    const dialogRef = this.dialog.open(DialogoConfirmacion, {
      width: '450px',
      data: {
        titulo: 'Cancelar Cita',
        mensaje: `¿Estás seguro de cancelar tu cita con el Dr(a). ${cita.medico.apellidos} para el ${new Date(cita.fechaHora).toLocaleDateString('es-ES', { day: '2-digit', month: 'long', year: 'numeric' })}?`
      }
    });

    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        this.cancelarCita(cita.id);
      }
    });
  }

  cancelarCita(id: string): void {
    this.citaService.cancelarMiCita(id).subscribe({
      next: () => {
        this.citas.update(citasActuales => citasActuales.filter(c => c.id !== id));
        this.notificacion.mostrar('Cita cancelada con éxito');
      },
      error: () => {
        this.notificacion.mostrar('No se pudo cancelar la cita. Inténtalo de nuevo.', 'error');
      }
    });
  }
   agregarAlCalendario(event: MouseEvent, cita: Cita): void {
    event.stopPropagation();

    const fechaInicio = new Date(cita.fechaHora);
    const fechaFin = new Date(fechaInicio.getTime() + 60 * 60 * 1000);
    const formatICSDate = (date: Date) => date.toISOString().replace(/[-:]/g, '').split('.')[0] + 'Z';

    const contenidoICS = [
      'BEGIN:VCALENDAR',
      'VERSION:2.0',
      'BEGIN:VEVENT',
      `DTSTART:${formatICSDate(fechaInicio)}`,
      `DTEND:${formatICSDate(fechaFin)}`,
      `SUMMARY:Cita Médica en Clínica Bienestar`,
      `DESCRIPTION:Cita con Dr(a). ${cita.medico.nombres} ${cita.medico.apellidos} (${cita.medico.especialidad}). Motivo: ${cita.motivo}`,
      `LOCATION:Clínica Bienestar, Consultorio ${cita.consultorio}`,
      'END:VEVENT',
      'END:VCALENDAR'
    ].join('\n');

    const blob = new Blob([contenidoICS], { type: 'text/calendar;charset=utf-8' });
    saveAs(blob, 'cita-clinica-bienestar.ics');
  }

  getEstadoClass(estado: string): string {
    switch (estado?.toLowerCase()) {
      case 'programada': return 'badge-primary';
      case 'completada': return 'badge-secondary';
      case 'cancelada': return 'badge-danger';
      default: return 'badge-light';
    }
  }
}