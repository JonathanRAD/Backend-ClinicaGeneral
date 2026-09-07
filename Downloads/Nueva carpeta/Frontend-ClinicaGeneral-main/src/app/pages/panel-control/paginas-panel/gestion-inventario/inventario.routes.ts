// ── inventario.routes.ts ─────────────────────────────────────────────────
import { Routes } from '@angular/router';
import { DashboardInventario } from './dashboard-inventario/dashboard-inventario';
import { GestionProducto } from './gestion-producto/gestion-producto';
import { GestionLote } from './gestion-lote/gestion-lote';
import { GestionProveedor } from './gestion-proveedor/gestion-proveedor';

export const INVENTARIO_ROUTES: Routes = [
  {
    path: '',
    component: DashboardInventario
  },
  {
    path: 'producto',
    component: GestionProducto
  },
  {
    path: 'lote',
    component: GestionLote
  },
  {
    path: 'proveedor',
    component: GestionProveedor
  }
];