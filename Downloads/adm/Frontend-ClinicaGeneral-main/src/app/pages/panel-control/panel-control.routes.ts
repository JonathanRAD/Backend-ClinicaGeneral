import { Routes } from '@angular/router';
import { LayoutComponent } from './componentes-panel/layout/layout';
import { Inicio } from './paginas-panel/inicio/inicio';
import { GestionPacientes } from './paginas-panel/gestion-pacientes/gestion-pacientes';
import { CalendarioCitas } from './paginas-panel/calendario-citas/calendario-citas';
import { GestionTriaje } from './paginas-panel/gestion-triaje/gestion-triaje';
import { GestionFacturacion } from './paginas-panel/gestion-facturacion/gestion-facturacion';
import { GestionMedicos } from './paginas-panel/gestion-medicos/gestion-medicos';
import { GestionConfiguracion } from './paginas-panel/gestion-configuracion/gestion-configuracion';
import { HistoriaClinicaComponent } from './paginas-panel/historia-clinica/historia-clinica';
import { GestionReportesComponent } from './componentes-panel/gestion-reportes/gestion-reportes';
import { MiPerfilComponent } from './paginas-panel/mi-perfil/mi-perfil';
import { GestionUsuariosComponent } from './paginas-panel/gestion-usuarios/gestion-usuarios';
import { GestionConferencias } from './paginas-panel/gestion-conferencias/gestion-conferencias';
import { rolGuard } from '../../core/guards/rol-guard';
import { permisoGuard } from '../../core/guards/permiso-guard';

export default [
  {
    path: '',
    component: LayoutComponent,
    children: [
      // --- Rutas Públicas del Panel ---
      { path: 'inicio', component: Inicio },
      { path: 'configuracion', component: GestionConfiguracion },
      { path: 'mi-perfil', component: MiPerfilComponent },

      // --- Rutas Protegidas por Permiso ---
      { 
        path: 'pacientes', 
        component: GestionPacientes,
        canActivate: [permisoGuard],
        data: { permiso: 'VER_PACIENTES' }
      },
      { 
        path: 'pacientes/:id/historia', 
        component: HistoriaClinicaComponent,
        canActivate: [permisoGuard],
        data: { permiso: 'VER_HISTORIAL_CLINICO' }
      },
      { 
        path: 'citas', 
        component: CalendarioCitas,
        canActivate: [permisoGuard],
        data: { permiso: 'VER_CITAS' }
      },
      { 
        path: 'triaje', 
        component: GestionTriaje,
        canActivate: [permisoGuard],
        data: { permiso: 'VER_CITAS' }
      },
      { 
        path: 'facturacion', 
        component: GestionFacturacion,
        canActivate: [permisoGuard],
        data: { permiso: 'VER_FACTURACION' }
      },
      { 
        path: 'inventario',
        canActivate: [permisoGuard],
        data: { permiso: 'GESTIONAR_INVENTARIO' },
        loadChildren: () => import('./paginas-panel/gestion-inventario/inventario.routes').then(m => m.INVENTARIO_ROUTES)
      },
      { 
        path: 'medicos', 
        component: GestionMedicos,
        canActivate: [permisoGuard],
        data: { permiso: 'VER_MEDICOS' }
      },
      { 
        path: 'usuarios', 
        component: GestionUsuariosComponent,
        canActivate: [permisoGuard],
        data: { permiso: 'VER_USUARIOS' }
      },
      { 
        path: 'reportes', 
        component: GestionReportesComponent,
        canActivate: [permisoGuard],
        data: { permiso: 'GENERAR_REPORTES' }
      },
      { 
        path: 'conferencias', 
        component: GestionConferencias,
        canActivate: [permisoGuard],
        data: { permiso: 'VER_CITAS' }
      },
      
      // --- Redirección por defecto ---
      { path: '', redirectTo: 'inicio', pathMatch: 'full' }
    ]
  }
] as Routes;