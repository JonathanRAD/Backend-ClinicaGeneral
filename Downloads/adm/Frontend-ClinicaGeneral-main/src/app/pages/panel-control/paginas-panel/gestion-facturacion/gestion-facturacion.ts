
import { Component, Signal, signal, WritableSignal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FacturacionService, FacturaPayload } from '../../../../services/facturacion';
import { Factura } from '../../../../core/models/factura';
import { TablaGenerica, ColumnConfig } from '../../../../shared/tabla-generica/tabla-generica';
import { DialogoConfirmacion } from '../../componentes-panel/dialogo-confirmacion/dialogo-confirmacion';
import { FormularioFacturacion } from '../../componentes-panel/formulario-facturacion/formulario-facturacion';
import { DialogoDespachoComponent } from '../../componentes-panel/dialogo-despacho/dialogo-despacho';
import { HistoriaClinicaService } from '../../../../services/historia-clinica';
import { MedicamentoService } from '../../../../services/medicamento.service';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-gestion-facturacion',
  standalone: true,
  imports: [ 
    CommonModule,
    TablaGenerica,
    MatCardModule, 
    MatIconModule,   
    MatDividerModule,
    MatButtonModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './gestion-facturacion.html',
  styleUrls: ['./gestion-facturacion.css']
})
export class GestionFacturacion {
  facturas: Signal<Factura[]>;
  facturaSeleccionada: WritableSignal<Factura | null> = signal(null);
  isAnimatingOut: WritableSignal<boolean> = signal(false);
  enviandoCorreo: WritableSignal<boolean> = signal(false);

  columnasFacturacion: ColumnConfig[] = [
    { name: 'cita.paciente.nombres', header: 'Paciente' },
    { name: 'fechaEmision', header: 'Fecha de Emisión', isDate: true },
    { name: 'monto', header: 'Monto (S/)' },
    { name: 'estado', header: 'Estado' },
  ];

  constructor(
    private facturacionService: FacturacionService,
    private historiaClinicaService: HistoriaClinicaService,
    private medicamentoService: MedicamentoService,
    public dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.facturas = this.facturacionService.facturas;
    this.medicamentoService.cargarMedicamentos();
  }

   onSeleccionarFactura(factura: Factura) {
    if (this.facturaSeleccionada()?.id === factura.id) {
      return;
    }

    this.isAnimatingOut.set(true);

    setTimeout(() => {
      this.facturaSeleccionada.set(factura);
      this.isAnimatingOut.set(false);
    }, 150);
  }

  onAgregarFactura() {
    const dialogRef = this.dialog.open(FormularioFacturacion, {
      width: '500px',
      disableClose: true,
      panelClass: 'custom-dialog-container',
      data: { esModoEdicion: false }
    });

    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        const payload: FacturaPayload = {
          citaId: resultado.citaId,
          monto: resultado.monto,
          estado: resultado.estado,
          montoPagado: resultado.montoPagado
        };
        this.facturacionService.registrarFactura(payload);
        this.snackBar.open('Factura registrada con éxito', 'Cerrar', { duration: 3000 });
      }
    });
  }

  onEditarFactura(factura: Factura) {
    const dialogRef = this.dialog.open(FormularioFacturacion, {
      width: '500px',
      disableClose: true,
      panelClass: 'custom-dialog-container',
      data: { esModoEdicion: true, factura: factura }
    });

    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        const payload: FacturaPayload = {
            citaId: resultado.citaId,
            monto: resultado.monto,
            estado: resultado.estado,
            montoPagado: resultado.montoPagado 
        };

        this.facturacionService.actualizarFactura(factura.id, payload);
        this.snackBar.open('Factura actualizada correctamente', 'Cerrar', { duration: 3000 });
      }
    });
  }

  onEliminarFactura(factura: Factura) {
    const dialogRef = this.dialog.open(DialogoConfirmacion, {
      width: '450px',
      data: {
        titulo: 'Anular Factura',
        mensaje: `¿Estás seguro de anular la factura para ${factura.cita.paciente.nombres}?`
      }
    });

    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado) {
        this.facturacionService.eliminarFactura(factura.id);
        this.snackBar.open('Factura anulada correctamente', 'Cerrar', { duration: 3000 });
      }
    });
  }

  /**
   * Envía la factura seleccionada por correo electrónico al paciente.
   * Requiere que el backend implemente: POST /api/facturas/{id}/enviar-correo
   */
  enviarFacturaPorCorreo(factura: Factura): void {
    this.enviandoCorreo.set(true);
    this.facturacionService.enviarFacturaPorCorreo(factura.id).subscribe({
      next: (res) => {
        this.enviandoCorreo.set(false);
        this.snackBar.open(
          res?.message || `Factura enviada al correo de ${factura.cita.paciente.nombres}`,
          'Cerrar',
          { duration: 4000, panelClass: 'notificacion-exito' }
        );
      },
      error: (err) => {
        this.enviandoCorreo.set(false);
        // Mostrar mensaje de error claro si el endpoint aún no existe en el backend
        const msg = err.message?.includes('404')
          ? 'El endpoint de envío de correo aún no está implementado en el backend.'
          : (err.message || 'Error al enviar la factura por correo.');
        this.snackBar.open(msg, 'Cerrar', { duration: 5000, panelClass: 'notificacion-error' });
      }
    });
  }

  abrirDespachoFarmacia(factura: Factura): void {
    const pacienteId = (factura.cita.paciente as any).id;
    const pacienteNombre = `${factura.cita.paciente.nombres} ${factura.cita.paciente.apellidos}`;

    this.historiaClinicaService.getHistoriaPorPacienteId(pacienteId).subscribe({
      next: (historia) => {
        // Recopilar todas las recetas de todas las consultas
        const todasLasRecetas = historia.consultas?.flatMap((c: any) => c.recetas || []) || [];

        if (todasLasRecetas.length === 0) {
          this.snackBar.open('Este paciente no tiene recetas emitidas', 'Cerrar', { duration: 3000 });
          return;
        }

        this.dialog.open(DialogoDespachoComponent, {
          width: '600px',
          disableClose: true,
          panelClass: 'custom-dialog-container',
          data: {
            pacienteNombre,
            pacienteDni: factura.cita.paciente.dni,
            recetas: todasLasRecetas
          }
        });
      },
      error: () => this.snackBar.open('Error al cargar historia del paciente', 'Cerrar', { duration: 3000 })
    });
  }
}