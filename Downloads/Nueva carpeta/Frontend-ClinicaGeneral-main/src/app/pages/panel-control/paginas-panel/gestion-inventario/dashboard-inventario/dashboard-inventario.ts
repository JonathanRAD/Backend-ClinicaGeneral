import {
  Component, OnInit, AfterViewInit,
  ViewChild, ElementRef, Signal, computed, effect
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { Chart } from 'chart.js/auto';
import { MedicamentoService } from '../../../../../services/medicamento.service';
import { Medicamento, LoteMedicamento } from '../../../../../core/models/medicamento';

interface LoteRiesgo {
  medicamentoId: any;
  producto: string;
  stock: number;
  laboratorio: string;
  presentacion: string;
  proveedor: string;
  unidades: number;
  diasRestantes: number;
  numeroLote: string;
}

@Component({
  selector: 'app-dashboard-inventario',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule],
  templateUrl: './dashboard-inventario.html',
  styleUrls: ['./dashboard-inventario.css']
})
export class DashboardInventario implements OnInit, AfterViewInit {

  @ViewChild('stockBarChart')   stockBarChartCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('estadoDonutChart') estadoDonutChartCanvas!: ElementRef<HTMLCanvasElement>;

  private barChart?: Chart;
  private donutChart?: Chart;

  // ── Signals computados ─────────────────────────────────────────────────
  totalProductos: Signal<number>;
  productosOk: Signal<number>;
  productosAdvertencia: Signal<number>;
  productosCriticos: Signal<number>;
  lotesProxVencer: Signal<number>;
  lotesRiesgo: Signal<LoteRiesgo[]>;
  stockBajo: Signal<Medicamento[]>;

  private readonly STOCK_MINIMO = 10;
  private readonly DIAS_WARN    = 60;
  private readonly DIAS_DANGER  = 15;

  constructor(private medService: MedicamentoService) {
    const meds = this.medService.medicamentos;

    this.totalProductos       = computed(() => meds().length);
    this.productosOk          = computed(() => meds().filter(m => (m.stockTotal ?? 0) > this.STOCK_MINIMO).length);
    this.productosAdvertencia = computed(() => meds().filter(m => { const s = m.stockTotal ?? 0; return s > 0 && s <= this.STOCK_MINIMO; }).length);
    this.productosCriticos    = computed(() => meds().filter(m => (m.stockTotal ?? 0) === 0).length);

    this.lotesRiesgo = computed(() => {
      const hoy = new Date();
      const resultado: LoteRiesgo[] = [];
      for (const med of meds()) {
        for (const lote of (med.lotes ?? [])) {
          const fv = new Date(lote.fechaVencimiento);
          const dias = Math.floor((fv.getTime() - hoy.getTime()) / 86_400_000);
          if (dias <= this.DIAS_WARN) {
            resultado.push({
              medicamentoId: med.id,
              producto:      med.nombre,
              stock:         lote.stock,
              laboratorio:   lote.numeroLote,
              presentacion:  med.formaFarmaceutica ?? '—',
              proveedor:     (lote as any).proveedor ?? '—',
              unidades:      lote.stock,
              diasRestantes: dias,
              numeroLote:    lote.numeroLote
            });
          }
        }
      }
      return resultado.sort((a, b) => a.diasRestantes - b.diasRestantes);
    });

    this.lotesProxVencer = computed(() => this.lotesRiesgo().length);

    this.stockBajo = computed(() =>
      meds()
        .filter(m => (m.stockTotal ?? 0) <= this.STOCK_MINIMO)
        .sort((a, b) => (a.stockTotal ?? 0) - (b.stockTotal ?? 0))
    );

    // Re-render charts when data changes
    effect(() => {
      const _ = meds();
      this.actualizarGraficos();
    });
  }

  ngOnInit(): void {
    this.medService.cargarMedicamentos();
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.crearGraficos(), 400);
  }

  // ── Helpers ─────────────────────────────────────────────────────────────
  getSemaforoClase(dias: number): string {
    if (dias < 0 || dias <= this.DIAS_DANGER) return 'row-danger';
    if (dias <= this.DIAS_WARN)               return 'row-warn';
    return 'row-ok';
  }

  // ── Charts ───────────────────────────────────────────────────────────────
  private crearGraficos(): void {
    this.crearBarChart();
    this.crearDonutChart();
  }

  private crearBarChart(): void {
    if (!this.stockBarChartCanvas?.nativeElement) return;
    const meds = this.medService.medicamentos();

    // Agrupar por forma farmacéutica
    const grupos: Record<string, number> = {};
    for (const m of meds) {
      const key = m.formaFarmaceutica ?? 'Otro';
      grupos[key] = (grupos[key] ?? 0) + (m.stockTotal ?? 0);
    }

    const isDark = document.body.classList.contains('dark-theme');
    const gridColor = isDark ? 'rgba(255,255,255,0.07)' : 'rgba(0,0,0,0.06)';
    const textColor = isDark ? '#94a3b8' : '#64748b';

    this.barChart = new Chart(this.stockBarChartCanvas.nativeElement, {
      type: 'bar',
      data: {
        labels: Object.keys(grupos),
        datasets: [{
          label: 'Unidades en stock',
          data: Object.values(grupos),
          backgroundColor: [
            'rgba(21,101,192,0.75)', 'rgba(46,125,50,0.75)',
            'rgba(123,31,162,0.75)', 'rgba(230,81,0,0.75)',
            'rgba(0,131,143,0.75)', 'rgba(183,28,28,0.75)',
            'rgba(245,127,23,0.75)'
          ],
          borderRadius: 8,
          borderSkipped: false,
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          y: { beginAtZero: true, grid: { color: gridColor }, ticks: { color: textColor } },
          x: { grid: { display: false }, ticks: { color: textColor, maxRotation: 30 } }
        }
      }
    });
  }

  private crearDonutChart(): void {
    if (!this.estadoDonutChartCanvas?.nativeElement) return;
    const ok   = this.productosOk();
    const warn = this.productosAdvertencia();
    const crit = this.productosCriticos();

    const isDark = document.body.classList.contains('dark-theme');
    const textColor = isDark ? '#94a3b8' : '#64748b';

    this.donutChart = new Chart(this.estadoDonutChartCanvas.nativeElement, {
      type: 'doughnut',
      data: {
        labels: ['Stock Óptimo', 'Stock Bajo', 'Agotado'],
        datasets: [{
          data: [ok, warn, crit],
          backgroundColor: ['#22c55e', '#eab308', '#ef4444'],
          borderWidth: 0,
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '68%',
        plugins: {
          legend: {
            position: 'bottom',
            labels: { padding: 16, font: { size: 12 }, color: textColor }
          }
        }
      }
    });
  }

  private actualizarGraficos(): void {
    if (!this.barChart || !this.donutChart) return;

    const meds  = this.medService.medicamentos();
    const grupos: Record<string, number> = {};
    for (const m of meds) {
      const key = m.formaFarmaceutica ?? 'Otro';
      grupos[key] = (grupos[key] ?? 0) + (m.stockTotal ?? 0);
    }
    this.barChart.data.labels = Object.keys(grupos);
    this.barChart.data.datasets[0].data = Object.values(grupos);
    this.barChart.update('none');

    this.donutChart.data.datasets[0].data = [
      this.productosOk(), this.productosAdvertencia(), this.productosCriticos()
    ];
    this.donutChart.update('none');
  }
}