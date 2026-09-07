import { Component, Signal, computed, OnInit  } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { CitaService } from '../../../../services/cita';
import { Cita } from '../../../../core/models/cita';
import { TablaGenerica, ColumnConfig } from '../../../../shared/tabla-generica/tabla-generica';
import { DialogoConfirmacion } from '../../componentes-panel/dialogo-confirmacion/dialogo-confirmacion';
import { FormularioCita } from '../../componentes-panel/formulario-cita/formulario-cita';
import { Notificacion } from '../../../../services/notificacion'; 

@Component({
  selector: 'app-calendario-citas',
  standalone: true,
  imports: [CommonModule, RouterModule, TablaGenerica],
  templateUrl: './calendario-citas.html',
  styleUrls: ['./calendario-citas.css']
})
export class CalendarioCitas implements OnInit {
  citasParaVista: Signal<Cita[]>;

  columnasCitas: ColumnConfig[] = [
    { name: 'fechaHora', header: 'Fecha y Hora', isDate: true },
    { name: 'tiempoRestante', header: 'Tiempo Restante' },
    { name: 'numeroTurno', header: 'Turno' },
    { name: 'paciente.nombres', header: 'Paciente' },
    { name: 'medico.nombres', header: 'Médico' },
    { name: 'consultorio', header: 'Consultorio' },
    { name: 'motivo', header: 'Motivo' },
    { name: 'estado', header: 'Estado' }
  ];

  constructor(
    private citaService: CitaService,
    public dialog: MatDialog,
    private notificacionService: Notificacion 
  ) {
    this.citasParaVista = computed(() => {
      const ahora = new Date();
      return this.citaService.citas()
        .map(cita => {
          const { tiempoRestante, alertaClase } = this.calcularTiempoRestante(new Date(cita.fechaHora), ahora);
          return { ...cita, tiempoRestante, alertaClase };
        })
        .sort((a, b) => new Date(a.fechaHora).getTime() - new Date(b.fechaHora).getTime());
    });
  }
  
  ngOnInit(): void {
  this.citaService.cargarCitasPanel();
  }

  private calcularTiempoRestante(fechaCita: Date, ahora: Date): { tiempoRestante: string, alertaClase: 'rojo' | 'ambar' | 'verde' } {
    const diffMs = fechaCita.getTime() - ahora.getTime();
    if (diffMs <= 0) {
      return { tiempoRestante: 'Pasada', alertaClase: 'rojo' };
    }
    const diffHoras = diffMs / (1000 * 60 * 60);
    const diffDias = diffMs / (1000 * 60 * 60 * 24);
    let tiempoRestante = '';
    let alertaClase: 'rojo' | 'ambar' | 'verde' = 'verde';

    if (diffDias < 1) {
      tiempoRestante = `En ${Math.floor(diffHoras)} h`;
      alertaClase = 'rojo';
    } else if (diffDias < 7) {
      tiempoRestante = `En ${Math.floor(diffDias)} d`;
      alertaClase = 'ambar';
    } else {
      tiempoRestante = `En ${Math.floor(diffDias / 7)} sem`;
      alertaClase = 'verde';
    }
    return { tiempoRestante, alertaClase };
  }

  onAgregarCita() {
    const dialogRef = this.dialog.open(FormularioCita, {
      width: '600px',
      disableClose: true,
      panelClass: 'custom-dialog-container',
      data: { esModoEdicion: false }
    });
    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        this.citaService.crearCitaPanel(resultado).subscribe({
          next: () => this.notificacionService.mostrar('Cita agendada con éxito'),
          error: () => this.notificacionService.mostrar('Error al agendar la cita', 'error')
        });
      }
    });
  }

  onEditarCita(cita: Cita) {
    const dialogRef = this.dialog.open(FormularioCita, {
      width: '600px',
      disableClose: true,
      panelClass: 'custom-dialog-container',
      data: { esModoEdicion: true, cita: cita }
    });
    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        this.citaService.actualizarCitaPanel(cita.id, resultado).subscribe({
          next: () => this.notificacionService.mostrar('Cita actualizada correctamente'),
          error: () => this.notificacionService.mostrar('Error al actualizar la cita', 'error')
        });
      }
    });
  }

  onEliminarCita(cita: Cita) {
    const dialogRef = this.dialog.open(DialogoConfirmacion, {
      width: '450px',
      data: {
        titulo: 'Cancelar Cita',
        mensaje: `¿Estás seguro de que deseas cancelar la cita de ${cita.paciente.nombres} ${cita.paciente.apellidos}?`
      }
    });
    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        this.citaService.eliminarCitaPanel(cita.id).subscribe({
          next: () => {
            this.notificacionService.mostrar('Cita cancelada');
          },
          error: () => this.notificacionService.mostrar('Error al cancelar la cita', 'error')
        });
      }
    });
  }
}