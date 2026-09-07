import { Component, OnInit, signal, computed, Signal, TemplateRef, ViewChild } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AutenticacionService } from '../../../../services/autenticacion';
import { CitaService } from '../../../../services/cita';
import { Cita } from '../../../../core/models/cita';
import { PerfilPacienteService } from '../../../../services/perfil-paciente.service';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
interface ActividadDiaria {
  id: number;
  descripcion: string;
  icono: string;
  completado: boolean;
}

interface Consejo {
  titulo: string;
  contenido: string;
  icono: string;
}

@Component({
  selector: 'app-inicio-portal',
  standalone: true,
  imports: [
    CommonModule, RouterModule, DatePipe, MatCardModule, MatButtonModule,
    MatIconModule, MatDividerModule, MatListModule, MatTooltipModule, MatDialogModule
  ],
  templateUrl: './inicio-portal.html',
  styleUrls: ['./inicio-portal.css']
})
export class InicioPortalComponent implements OnInit {
  nombreUsuario = signal('');
  proximaCita: Signal<Cita | undefined>;
  
  @ViewChild('missingDataDialog') missingDataDialog!: TemplateRef<any>;
  
  actividadesDiarias = signal<ActividadDiaria[]>([]);
  progresoActividades: Signal<number>; 
  consejoDelDia = signal<Consejo | null>(null);
  resultadosPendientes = signal(1);
  mensajesNoLeidos = signal(2);
  recetasActivas = signal(3);
  
  private citas = signal<Cita[]>([]);
  private todosLosConsejos: Consejo[] = [
    { titulo: 'El Poder de la Hidratación', contenido: 'Comienza tu día con un vaso de agua para activar tu metabolismo y mejorar tu concentración.', icono: 'water_drop' },
    { titulo: 'Mindfulness en 5 Minutos', contenido: 'Dedica 5 minutos a respirar profundamente. Reduce el estrés y mejora tu enfoque.', icono: 'self_improvement' },
    { titulo: 'Nutrición Inteligente', contenido: 'Incluye al menos un vegetal de hoja verde en tu almuerzo para un aporte extra de vitaminas.', icono: 'restaurant' },
    { titulo: 'Pausa Activa', contenido: 'Por cada hora de trabajo sentado, levántate y camina durante 5 minutos. Tu espalda te lo agradecerá.', icono: 'directions_walk' },
    { titulo: 'Calidad del Sueño', contenido: 'Evita pantallas (móvil, TV) al menos 30 minutos antes de dormir para un descanso más profundo.', icono: 'bedtime' },
    { titulo: 'La Regla de los Colores', contenido: 'Intenta que tu plato de comida tenga al menos tres colores diferentes de alimentos naturales.', icono: 'palette' },
    { titulo: 'Conexión Social', contenido: 'Llama o visita a un ser querido. Las conexiones sociales son vitales para la salud mental.', icono: 'people' },
    { titulo: 'Estiramiento Matutino', contenido: 'Antes de levantarte, estira suavemente tus brazos y piernas para despertar tus músculos.', icono: 'accessibility_new' },
    { titulo: 'Snack de Energía', contenido: 'Un puñado de almendras o nueces es un snack perfecto para combatir la fatiga de media tarde.', icono: 'bolt' },
    { titulo: 'Gratitud Diaria', contenido: 'Antes de dormir, piensa en tres cosas por las que te sientas agradecido hoy. Fomenta una mentalidad positiva.', icono: 'volunteer_activism' }
  ];

  constructor(
    private authService: AutenticacionService,
    private citaService: CitaService,
    private perfilService: PerfilPacienteService,
    public dialog: MatDialog
  ) {
    const nombre = this.authService.getNombreUsuario()?.split('@')[0] || 'Paciente';
    this.nombreUsuario.set(nombre.charAt(0).toUpperCase() + nombre.slice(1));

    this.proximaCita = computed(() => {
      const ahora = new Date();
      return this.citas()
        .filter(c => new Date(c.fechaHora) > ahora && c.estado.toLowerCase() === 'programada')
        .sort((a, b) => new Date(a.fechaHora).getTime() - new Date(b.fechaHora).getTime())[0];
    });

    this.progresoActividades = computed(() => {
      const actividades = this.actividadesDiarias();
      if (actividades.length === 0) return 100;
      const completadas = actividades.filter(a => a.completado).length;
      return Math.round((completadas / actividades.length) * 100);
    });
  }

  ngOnInit(): void {
    this.citaService.getMisCitas().subscribe(citas => this.citas.set(citas));
    this.cargarDatosDashboard();

    this.perfilService.getMiPerfil().subscribe({
      next: (perfil) => this.verificarDatosFaltantes(perfil),
      error: () => console.error('No se pudo obtener el perfil para validar datos faltantes')
    });
  }
  
  verificarDatosFaltantes(paciente: any): void {
    const faltaDni = !paciente.dni;
    const faltaTelefono = !paciente.telefono;
    const faltaPeso = paciente.peso == null || paciente.peso === 0;
    const faltaAltura = paciente.altura == null || paciente.altura === 0;

    if (faltaDni || faltaTelefono || faltaPeso || faltaAltura) {
      setTimeout(() => {
        this.dialog.open(this.missingDataDialog, {
          width: '400px',
          disableClose: true,
          data: { 
            faltantes: { faltaDni, faltaTelefono, faltaPeso, faltaAltura } 
          }
        });
      }, 500);
    }
  }
  
  private cargarDatosDashboard(): void {
    this.actividadesDiarias.set([
      { id: 1, descripcion: 'Tomar vitamina D', icono: 'wb_sunny', completado: true },
      { id: 2, descripcion: 'Caminata de 30 minutos', icono: 'directions_walk', completado: false },
      { id: 3, descripcion: 'Beber 2L de agua', icono: 'local_drink', completado: false },
      { id: 4, descripcion: 'Leer 15 minutos', icono: 'menu_book', completado: false },
    ]);
    this.consejoDelDia.set(this.todosLosConsejos[Math.floor(Math.random() * this.todosLosConsejos.length)]);
  }
  toggleActividad(id: number): void {
    this.actividadesDiarias.update(actividades => 
      actividades.map(act => 
        act.id === id ? { ...act, completado: !act.completado } : act
      )
    );
  }
}