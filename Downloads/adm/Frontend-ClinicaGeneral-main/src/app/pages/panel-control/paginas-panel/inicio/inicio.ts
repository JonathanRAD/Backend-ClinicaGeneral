
import { Component, computed, Signal, AfterViewInit, ViewChild, ElementRef, effect, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Chart } from 'chart.js/auto';

import { PacienteService } from '../../../../services/paciente';
import { CitaService } from '../../../../services/cita';
import { FacturacionService } from '../../../../services/facturacion';
import { MedicoService } from '../../../../services/medico';
import { MedicamentoService } from '../../../../services/medicamento.service';
import { Cita } from '../../../../core/models/cita';
import { Factura } from '../../../../core/models/factura';
import { Patient } from '../../../../core/models/patient';

@Component({
  selector: 'app-inicio',
  standalone: true,
  imports: [CommonModule, RouterModule, MatCardModule, MatIconModule, MatListModule, MatButtonModule, MatTooltipModule],
  templateUrl: './inicio.html',
  styleUrls: ['./inicio.css']
})
export class Inicio implements OnInit, AfterViewInit {
  @ViewChild('ingresosChart')  ingresosChartCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('citasEstadoChart') citasEstadoChartCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('citasDiaChart') citasDiaChartCanvas!: ElementRef<HTMLCanvasElement>;

  // ── KPI Signals ──────────────────────────────────────────────────────────
  totalPacientes: Signal<number>;
  citasHoy: Signal<number>;
  ingresosMesActual: Signal<number>;
  totalMedicos: Signal<number>;
  citasProgramadas: Signal<number>;

  // ── Nuevos KPIs clínicos ─────────────────────────────────────────────────
  triajesPendientesHoy: Signal<number>;
  medicamentosStockCritico: Signal<number>;
  recetasHoy: Signal<number>;

  // ── Actividad reciente ───────────────────────────────────────────────────
  ultimosPacientes: Signal<Patient[]>;
  proximasCitas: Signal<Cita[]>;

  // ── Datos para gráficos ──────────────────────────────────────────────────
  private ingresosPorMesData: Signal<{ labels: string[]; data: number[] }>;
  private citasEstadoData: Signal<{ labels: string[]; data: number[] }>;
  private citasPorDiaData: Signal<{ labels: string[]; data: number[] }>;

  private ingresosChartInstance?: Chart;
  private citasEstadoChartInstance?: Chart;
  private citasDiaChartInstance?: Chart;

  fechaActual = new Date();

  constructor(
    private pacienteService: PacienteService,
    private citaService: CitaService,
    private facturacionService: FacturacionService,
    private medicoService: MedicoService,
    private medicamentoService: MedicamentoService
  ) {
    // KPIs base
    this.totalPacientes = computed(() => this.pacienteService.pacientes().length);
    this.citasHoy       = computed(() => this.getCitasDeHoy());
    this.ingresosMesActual = computed(() => this.getIngresosMesActual());
    this.totalMedicos   = computed(() => this.medicoService.medicos().length);
    this.citasProgramadas = computed(() =>
      this.citaService.citas().filter(c => c.estado === 'programada').length
    );

    // Nuevos KPIs clínicos
    this.triajesPendientesHoy = computed(() => {
      const hoy = new Date();
      return this.citaService.citas().filter(c => {
        const d = new Date(c.fechaHora);
        return d.getDate() === hoy.getDate() &&
               d.getMonth() === hoy.getMonth() &&
               d.getFullYear() === hoy.getFullYear() &&
               !c.triaje &&
               (c.estado === 'programada' || c.estado === 'en_espera');
      }).length;
    });

    this.medicamentosStockCritico = computed(() =>
      this.medicamentoService.medicamentos().filter(m => (m.stockTotal || 0) < 10).length
    );

    this.recetasHoy = computed(() => {
      // Contar citas completadas hoy con recetas (proxy)
      const hoy = new Date();
      return this.citaService.citas().filter(c => {
        const d = new Date(c.fechaHora);
        return d.getDate() === hoy.getDate() &&
               d.getMonth() === hoy.getMonth() &&
               d.getFullYear() === hoy.getFullYear() &&
               (c.estado === 'lista_consulta' || c.estado === 'completada');
      }).length;
    });

    // Actividad reciente
    this.ultimosPacientes = computed(() =>
      this.pacienteService.pacientes().slice(-4).reverse()
    );
    this.proximasCitas = computed(() =>
      this.citaService.citas()
        .filter(c => c.estado === 'programada' && new Date(c.fechaHora) >= new Date())
        .sort((a, b) => new Date(a.fechaHora).getTime() - new Date(b.fechaHora).getTime())
        .slice(0, 4)
    );

    // Datos de gráficos (signals reactivos)
    this.ingresosPorMesData = computed(() => this.procesarIngresosPorMes());
    this.citasEstadoData    = computed(() => this.procesarCitasPorEstado());
    this.citasPorDiaData    = computed(() => this.procesarCitasPorDia());

    // Efectos reactivos para actualizar gráficos cuando cambian los datos
    effect(() => { this.actualizarGrafico(this.ingresosChartInstance, this.ingresosPorMesData()); });
    effect(() => { this.actualizarGrafico(this.citasEstadoChartInstance, this.citasEstadoData()); });
    effect(() => { this.actualizarGrafico(this.citasDiaChartInstance, this.citasPorDiaData()); });
  }

  ngOnInit(): void {
    this.citaService.cargarCitasPanel();
    this.pacienteService.cargarPacientes();
    this.medicoService.cargarMedicos();
    this.medicamentoService.cargarMedicamentos();
  }

  ngAfterViewInit(): void {
    this.crearGraficoIngresos();
    this.crearGraficoCitasEstado();
    this.crearGraficoCitasDia();
  }

  // ── Helpers KPI ──────────────────────────────────────────────────────────
  private getCitasDeHoy(): number {
    const hoy = new Date().setHours(0, 0, 0, 0);
    return this.citaService.citas()
      .filter(c => new Date(c.fechaHora).setHours(0, 0, 0, 0) === hoy).length;
  }

  private getIngresosMesActual(): number {
    const hoy = new Date();
    const primerDiaMes = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
    return this.facturacionService.facturas()
      .filter(f => new Date(f.fechaEmision) >= primerDiaMes && f.estado === 'pagada')
      .reduce((sum: number, f: Factura) => sum + f.monto, 0);
  }

  // ── Procesadores de datos para gráficos ─────────────────────────────────
  private procesarIngresosPorMes(): { labels: string[]; data: number[] } {
    const conteo = Array(12).fill(0);
    this.facturacionService.facturas()
      .filter(f => f.estado === 'pagada')
      .forEach(f => { conteo[new Date(f.fechaEmision).getMonth()] += f.monto; });
    const labels = ['Ene','Feb','Mar','Abr','May','Jun','Jul','Ago','Sep','Oct','Nov','Dic'];
    return { labels, data: conteo };
  }

  private procesarCitasPorEstado(): { labels: string[]; data: number[] } {
    const citas = this.citaService.citas();
    return {
      labels: ['Programadas', 'Completadas', 'Canceladas'],
      data: [
        citas.filter(c => c.estado === 'programada').length,
        citas.filter(c => c.estado === 'completada').length,
        citas.filter(c => c.estado === 'cancelada').length
      ]
    };
  }

  private procesarCitasPorDia(): { labels: string[]; data: number[] } {
    const labels = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];
    const conteo = Array(7).fill(0);
    this.citaService.citas().forEach(c => {
      conteo[new Date(c.fechaHora).getDay()]++;
    });
    return { labels, data: conteo };
  }

  // ── Creación de gráficos ─────────────────────────────────────────────────
  private crearGraficoIngresos(): void {
    if (!this.ingresosChartCanvas) return;
    const datos = this.ingresosPorMesData();
    this.ingresosChartInstance = new Chart(this.ingresosChartCanvas.nativeElement, {
      type: 'line',
      data: {
        labels: datos.labels,
        datasets: [{
          label: 'Ingresos (S/)',
          data: datos.data,
          borderColor: '#198754',
          backgroundColor: 'rgba(25,135,84,0.12)',
          fill: true,
          tension: 0.4,
          pointRadius: 4,
          pointBackgroundColor: '#198754'
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } } }
      }
    });
  }

  private crearGraficoCitasEstado(): void {
    if (!this.citasEstadoChartCanvas) return;
    const datos = this.citasEstadoData();
    this.citasEstadoChartInstance = new Chart(this.citasEstadoChartCanvas.nativeElement, {
      type: 'doughnut',
      data: {
        labels: datos.labels,
        datasets: [{
          data: datos.data,
          backgroundColor: ['#0d6efd', '#198754', '#dc3545'],
          borderWidth: 0
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        cutout: '70%',
        plugins: { legend: { position: 'bottom', labels: { padding: 16, font: { size: 13 } } } }
      }
    });
  }

  private crearGraficoCitasDia(): void {
    if (!this.citasDiaChartCanvas) return;
    const datos = this.citasPorDiaData();
    this.citasDiaChartInstance = new Chart(this.citasDiaChartCanvas.nativeElement, {
      type: 'bar',
      data: {
        labels: datos.labels,
        datasets: [{
          label: 'Citas',
          data: datos.data,
          backgroundColor: [
            'rgba(13,110,253,0.7)', 'rgba(13,110,253,0.85)', 'rgba(13,110,253,0.85)',
            'rgba(13,110,253,0.85)', 'rgba(13,110,253,0.85)', 'rgba(13,110,253,0.85)',
            'rgba(13,110,253,0.7)'
          ],
          borderRadius: 8,
          borderSkipped: false
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { y: { beginAtZero: true, ticks: { stepSize: 1 }, grid: { color: 'rgba(0,0,0,0.05)' } } }
      }
    });
  }

  // ── Actualización reactiva de gráficos ───────────────────────────────────
  private actualizarGrafico(chart: Chart | undefined, datos: { labels: string[]; data: number[] }): void {
    if (!chart) return;
    chart.data.datasets[0].data = datos.data;
    chart.update('none');
  }
}