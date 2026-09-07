import { Routes } from '@angular/router';
import { LayoutPortalComponent } from './componentes-portal/layout-portal/layout-portal';
import { MisCitas } from './paginas-portal/mis-citas/mis-citas';
import { AgendarCita } from './paginas-portal/agendar-cita/agendar-cita';
import { MiHistorial } from './paginas-portal/mi-historial/mi-historial';
import { MiPerfil } from './paginas-portal/mi-perfil/mi-perfil';
import { MisConferencias } from './paginas-portal/mis-conferencias/mis-conferencias';

import { InicioPortalComponent } from './paginas-portal/inicio-portal/inicio-portal';
import { NuestrosMedicosComponent } from './paginas-portal/nuestros-medicos/nuestros-medicos';
import { ServiciosComponent } from './paginas-portal/servicios/servicios';

export const PORTAL_ROUTES: Routes = [
  {
    path: '',
    component: LayoutPortalComponent,
    children: [
      { path: 'inicio', component: InicioPortalComponent },
      { path: 'nuestros-medicos', component: NuestrosMedicosComponent },
      { path: 'servicios', component: ServiciosComponent },
      { path: 'mis-citas', component: MisCitas },
      { path: 'agendar-cita', component: AgendarCita },
      { path: 'mi-historial', component: MiHistorial },
      { path: 'mi-perfil', component: MiPerfil },
      { path: 'mis-conferencias', component: MisConferencias },
      { path: '', redirectTo: 'inicio', pathMatch: 'full' }
    ]
  }
];