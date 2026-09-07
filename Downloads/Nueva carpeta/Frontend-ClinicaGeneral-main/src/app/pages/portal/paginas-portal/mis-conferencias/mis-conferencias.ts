import { Component, OnInit, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ConferenciaService } from '../../../../services/conferencia.service';
import { PerfilPacienteService } from '../../../../services/perfil-paciente.service';
import { Conferencia } from '../../../../core/models/conferencia';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AutenticacionService } from '../../../../services/autenticacion';
import { SalaVideoconferencia } from '../../../../shared/sala-videoconferencia/sala-videoconferencia';

@Component({
  selector: 'app-mis-conferencias',
  standalone: true,
  imports: [
    CommonModule, MatCardModule, MatTableModule,
    MatButtonModule, MatIconModule, MatTooltipModule, SalaVideoconferencia, DatePipe
  ],
  templateUrl: './mis-conferencias.html',
  styleUrls: ['./mis-conferencias.css']
})
export class MisConferencias implements OnInit {
  conferencias = signal<Conferencia[]>([]);
  columnasMostrar = ['fecha', 'medico', 'duracion', 'estado', 'acciones'];
  
  conferenciaActiva: Conferencia | null = null;
  nombrePaciente: string = 'Paciente';

  constructor(
    private conferenciaService: ConferenciaService,
    private perfilService: PerfilPacienteService,
    private authService: AutenticacionService
  ) {}

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.perfilService.getMiPerfil().subscribe({
      next: (perfil) => {
        if (perfil && perfil.id) {
          this.nombrePaciente = `${perfil.nombres} ${perfil.apellidos}`;
          this.conferenciaService.getPorPaciente(perfil.id).subscribe({
            next: (data) => this.conferencias.set(data),
            error: (err) => console.error('Error al cargar conferencias del paciente:', err)
          });
        }
      },
      error: (err) => console.error('Error al cargar perfil del paciente', err)
    });
  }

  unirseSala(conferencia: Conferencia): void {
    this.conferenciaActiva = conferencia;
  }

  cerrarSala(): void {
    this.conferenciaActiva = null;
  }
}
