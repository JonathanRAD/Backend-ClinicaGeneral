import { Component, OnInit, signal, TemplateRef, ViewChild } from '@angular/core';
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
  conferencias = signal<Conferencia[]>([]);
  pacientes = signal<Patient[]>([]);
  medicos = signal<Medico[]>([]);
  
  columnasMostrar = ['fecha', 'paciente', 'medico', 'duracion', 'estado', 'acciones'];
  
  formProgramar: FormGroup;
  @ViewChild('modalProgramar') modalProgramar!: TemplateRef<any>;

  conferenciaActiva: Conferencia | null = null;
  nombreAdmin: string = 'Admin';

  constructor(
    private conferenciaService: ConferenciaService,
    private pacienteService: PacienteService,
    private medicoService: MedicoService,
    private authService: AutenticacionService,
    private fb: FormBuilder,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
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
    this.conferenciaService.conferencias().length === 0 && this.conferenciaService.cargarTodas();
    // Suscribirse a las conferencias
    // Para simplificar, obtenemos directo de la API
    this.conferenciaService.getPorMedico(0); // Dummy, mejor obtenerTodas
    this.conferenciaService.cargarTodas();
    
    // Subscribe to signals indirectly or fetch manually
    this.pacienteService.pacientes().length === 0 && this.pacienteService.cargarPacientes();
    this.medicoService.medicos().length === 0 && this.medicoService.cargarMedicos();

    setTimeout(() => {
      this.conferencias.set(this.conferenciaService.conferencias());
      this.pacientes.set(this.pacienteService.pacientes());
      this.medicos.set(this.medicoService.medicos());
    }, 1000);
  }

  abrirModalProgramar(): void {
    this.pacientes.set(this.pacienteService.pacientes());
    this.medicos.set(this.medicoService.medicos());
    this.formProgramar.reset({ duracionMinutos: 30 });
    this.dialog.open(this.modalProgramar, { width: '500px' });
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
      next: (res) => {
        this.snackBar.open('Conferencia programada con éxito', 'Cerrar', { duration: 3000 });
        this.dialog.closeAll();
        // Refresh
        this.conferenciaService.cargarTodas();
        setTimeout(() => this.conferencias.set(this.conferenciaService.conferencias()), 500);
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
        this.conferenciaService.cargarTodas();
        setTimeout(() => this.conferencias.set(this.conferenciaService.conferencias()), 500);
      }
    });
  }
}
