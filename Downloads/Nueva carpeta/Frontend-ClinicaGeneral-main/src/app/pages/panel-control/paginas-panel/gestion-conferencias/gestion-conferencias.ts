import { Component, OnInit, signal, TemplateRef, ViewChild, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConferenciaService } from '../../../../services/conferencia.service';
import { PacienteService } from '../../../../services/paciente';
import { MedicoService } from '../../../../services/medico';
import { Conferencia } from '../../../../core/models/conferencia';
import { Patient } from '../../../../core/models/patient';
import { Medico } from '../../../../core/models/medico';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AutenticacionService } from '../../../../services/autenticacion';
import { SalaVideoconferencia } from '../../../../shared/sala-videoconferencia/sala-videoconferencia';

@Component({
  selector: 'app-gestion-conferencias',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatCardModule, MatTableModule,
    MatButtonModule, MatIconModule, MatDialogModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatTooltipModule, SalaVideoconferencia, DatePipe
  ],
  templateUrl: './gestion-conferencias.html',
  styleUrls: ['./gestion-conferencias.css']
})
export class GestionConferencias implements OnInit {
  // ── Inyecciones Modernas vía inject() ────────────────────────────────────
  private conferenciaService = inject(ConferenciaService);
  private pacienteService = inject(PacienteService);
  private medicoService = inject(MedicoService);
  private authService = inject(AutenticacionService);
  private fb = inject(FormBuilder);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  // ── Vinculación Directa a Señales del Servicio (100% Reactivo) ────────────
  conferencias = this.conferenciaService.conferencias;
  pacientes = this.pacienteService.pacientes;
  medicos = this.medicoService.medicos;
  
  columnasMostrar = ['fecha', 'paciente', 'medico', 'duracion', 'estado', 'acciones'];
  
  formProgramar: FormGroup;
  @ViewChild('modalProgramar') modalProgramar!: TemplateRef<any>;

  conferenciaActiva: Conferencia | null = null;
  nombreAdmin: string = 'Admin';

  constructor() {
    this.formProgramar = this.fb.group({
      pacienteId: ['', Validators.required],
      medicoId: ['', Validators.required],
      fechaProgramada: ['', Validators.required],
      duracionMinutos: [30, [Validators.required, Validators.min(15)]]
    });
    const authName = this.authService.getNombreUsuario()?.split('@')[0] || 'Admin';
    this.nombreAdmin = authName.charAt(0).toUpperCase() + authName.slice(1);
  }

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    // Carga atómica reactiva solo si no se han cargado previamente
    if (this.conferenciaService.conferencias().length === 0) {
      this.conferenciaService.cargarTodas();
    }
    if (this.pacienteService.pacientes().length === 0) {
      this.pacienteService.cargarPacientes();
    }
    if (this.medicoService.medicos().length === 0) {
      this.medicoService.cargarMedicos();
    }
  }

  abrirModalProgramar(): void {
    this.formProgramar.reset({ duracionMinutos: 30 });
    this.dialog.open(this.modalProgramar, { width: '500px', disableClose: true });
  }

  guardarConferencia(): void {
    if (this.formProgramar.invalid) return;
    
    const val = this.formProgramar.value;
    const paciente = this.pacientes().find(p => p.id === val.pacienteId);
    const medico = this.medicos().find(m => m.id === val.medicoId);

    if (!paciente || !medico) return;

    const nuevaConferencia: Conferencia = {
      paciente: paciente,
      medico: medico,
      fechaProgramada: val.fechaProgramada,
      duracionMinutos: val.duracionMinutos
    };

    this.conferenciaService.programar(nuevaConferencia).subscribe({
      next: () => {
        this.snackBar.open('Conferencia programada con éxito', 'Cerrar', { duration: 3000 });
        this.dialog.closeAll();
        // El servicio maneja de manera interna la inserción de la nueva conferencia
        // actualizando la señal reactiva y propagándola al instante a la UI.
      },
      error: () => this.snackBar.open('Error al programar', 'Cerrar', { duration: 3000 })
    });
  }

  unirseSala(conferencia: Conferencia): void {
    this.conferenciaActiva = conferencia;
  }

  cerrarSala(): void {
    this.conferenciaActiva = null;
  }

  cambiarEstado(conferencia: Conferencia, estado: string): void {
    if (!conferencia.id) return;
    this.conferenciaService.actualizarEstado(conferencia.id, estado).subscribe({
      next: () => {
        this.snackBar.open('Estado actualizado', 'Cerrar', { duration: 2000 });
        // El servicio maneja la actualización del estado de manera reactiva
        // en la colección de la señal, reflejando el cambio de inmediato.
      }
    });
  }
}
