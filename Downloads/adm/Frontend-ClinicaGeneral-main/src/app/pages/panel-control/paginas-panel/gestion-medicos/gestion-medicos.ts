
import { Component, Signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Medico } from '../../../../core/models/medico';
import { MedicoService } from '../../../../services/medico';
import { TablaGenerica, ColumnConfig } from '../../../../shared/tabla-generica/tabla-generica';
import { FormularioMedico } from '../../componentes-panel/formulario-medico/formulario-medico';
import { DialogoConfirmacion } from '../../componentes-panel/dialogo-confirmacion/dialogo-confirmacion';

interface MedicoConEdad extends Medico {
  edad?: number;
}

@Component({
  selector: 'app-gestion-medicos',
  standalone: true,
  imports: [CommonModule, TablaGenerica],
  templateUrl: './gestion-medicos.html',
  styleUrls: ['./gestion-medicos.css']
  
})
export class GestionMedicos {
  medicosParaVista: Signal<MedicoConEdad[]>;

  columnasMedicos: ColumnConfig[] = [
    { name: 'nombres', header: 'Nombres' },
    { name: 'apellidos', header: 'Apellidos' },
    { name: 'especialidad', header: 'Especialidad' },
    { name: 'edad', header: 'Edad' } 
  ];

  constructor(
    private medicoService: MedicoService,
    public dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.medicosParaVista = computed(() => {
      return this.medicoService.medicos().map(medico => ({
        ...medico,
        edad: this.calcularEdad(medico.fechaNacimiento)
      }));
    });
  }
  private calcularEdad(fechaNacimiento?: Date): number | undefined {
    if (!fechaNacimiento) return undefined;
    const hoy = new Date();
    const nacimiento = new Date(fechaNacimiento);
    let edad = hoy.getFullYear() - nacimiento.getFullYear();
    const m = hoy.getMonth() - nacimiento.getMonth();
    if (m < 0 || (m === 0 && hoy.getDate() < nacimiento.getDate())) {
        edad--;
    }
    return edad;
  }

  onAgregarMedico() {
    const dialogRef = this.dialog.open(FormularioMedico, {
      width: '500px',
      disableClose: true,
      panelClass: 'custom-dialog-container',
      data: { esModoEdicion: false }
    });
    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        this.medicoService.crearMedico(resultado);
        this.snackBar.open('Médico agregado con éxito', 'Cerrar', { duration: 3000 });
      }
    });
  }

  onEditarMedico(medico: Medico) {
    const dialogRef = this.dialog.open(FormularioMedico, {
      width: '500px',
      disableClose: true,
      panelClass: 'custom-dialog-container',
      data: { esModoEdicion: true, medico: medico }
    });
    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        this.medicoService.actualizarMedico({ id: medico.id, ...resultado });
        this.snackBar.open('Médico actualizado correctamente', 'Cerrar', { duration: 3000 });
      }
    });
  }

  onEliminarMedico(medico: Medico) {
    const dialogRef = this.dialog.open(DialogoConfirmacion, {
      width: '450px',
      data: {
        titulo: 'Confirmar Eliminación',
        mensaje: `¿Estás seguro de eliminar a Dr(a). ${medico.nombres} ${medico.apellidos}?`
      }
    });
    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        this.medicoService.eliminarMedico(medico.id);
        this.snackBar.open('Médico eliminado', 'Cerrar', { duration: 3000 });
      }
    });
  }
}