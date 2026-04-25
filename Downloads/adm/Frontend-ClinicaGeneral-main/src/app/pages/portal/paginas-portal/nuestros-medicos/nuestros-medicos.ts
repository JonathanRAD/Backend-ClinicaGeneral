import { Component, OnInit, signal, computed, Signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MedicoService } from '../../../../services/medico';
import { Medico } from '../../../../core/models/medico';
import { Spinner } from '../../../../shared/spinner/spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { PerfilMedicoComponent } from '../../componentes-portal/perfil-medico/perfil-medico';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { Router } from '@angular/router';

@Component({
  selector: 'app-nuestros-medicos',
  standalone: true,
  imports: [
    CommonModule, MatCardModule, MatIconModule, Spinner,
    MatDialogModule, MatFormFieldModule, MatSelectModule, FormsModule,
    MatInputModule, MatButtonModule
  ],
  templateUrl: './nuestros-medicos.html',
  styleUrls: ['./nuestros-medicos.css']
})
export class NuestrosMedicosComponent implements OnInit {
  cargando = signal(true);
  
  private medicos = signal<Medico[]>([]);
  
  especialidades: Signal<string[]>;
  especialidadSeleccionada = signal<string>('todos');
  nombreBusqueda = signal<string>('');
  medicosFiltrados: Signal<Medico[]>;

  constructor(
    private medicoService: MedicoService,
    public dialog: MatDialog,
    private router: Router 
  ) {
    this.especialidades = computed(() => 
      [...new Set(this.medicos().map(m => m.especialidad))].sort()
    );

    this.medicosFiltrados = computed(() => {
      const especialidad = this.especialidadSeleccionada();
      const busqueda = this.nombreBusqueda().toLowerCase();

      return this.medicos().filter(medico => {
        const porEspecialidad = especialidad === 'todos' || medico.especialidad === especialidad;
        const porNombre = `${medico.nombres} ${medico.apellidos}`.toLowerCase().includes(busqueda);
        return porEspecialidad && porNombre;
      });
    });
  }

  ngOnInit(): void {
    this.medicoService.getMedicos().subscribe(data => {
      this.medicos.set(data);
      this.cargando.set(false);
    });
  }

  verPerfil(medico: Medico): void {
    this.dialog.open(PerfilMedicoComponent, {
      width: '500px',
      data: medico,
      panelClass: 'custom-dialog-container'
    });
  }
  
  agendarCitaConMedico(medico: Medico): void {
    this.router.navigate(['/portal/agendar-cita'], { queryParams: { medicoId: medico.id } });
  }

  // Genera color de avatar único basado en el apellido
  getAvatarColor(apellido: string): string {
    const colors = [
      'linear-gradient(135deg, #1565c0, #00acc1)',
      'linear-gradient(135deg, #6f42c1, #9b6edc)',
      'linear-gradient(135deg, #198754, #2abb5b)',
      'linear-gradient(135deg, #fd7e14, #ffa94d)',
      'linear-gradient(135deg, #dc3545, #ff6b7a)',
      'linear-gradient(135deg, #0d6efd, #4d9fff)',
      'linear-gradient(135deg, #20c997, #0dcaf0)',
    ];
    let hash = 0;
    for (let i = 0; i < apellido.length; i++) hash += apellido.charCodeAt(i);
    return colors[hash % colors.length];
  }

  getSpecialtyBg(esp: string): string {
    const map: Record<string, string> = {
      'Cardiología': 'rgba(220,53,69,0.1)',
      'Neurología': 'rgba(111,66,193,0.1)',
      'Pediatría': 'rgba(253,126,20,0.1)',
      'Ginecología': 'rgba(255,105,180,0.1)',
      'Traumatología': 'rgba(25,135,84,0.1)',
      'Medicina General': 'rgba(13,110,253,0.1)',
    };
    return map[esp] ?? 'rgba(108,117,125,0.1)';
  }

  getSpecialtyColor(esp: string): string {
    const map: Record<string, string> = {
      'Cardiología': '#dc3545',
      'Neurología': '#6f42c1',
      'Pediatría': '#fd7e14',
      'Ginecología': '#e83e8c',
      'Traumatología': '#198754',
      'Medicina General': '#0d6efd',
    };
    return map[esp] ?? '#6c757d';
  }
}