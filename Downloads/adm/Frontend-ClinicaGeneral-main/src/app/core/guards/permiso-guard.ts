import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AutenticacionService } from '../../services/autenticacion';

export const permisoGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AutenticacionService);
  const router = inject(Router);

  const permisoRequerido = route.data['permiso'];

  if (!permisoRequerido) {
    console.error('No se ha definido un permiso para esta ruta.');
    router.navigate(['/panel/inicio']);
    return false;
  }

  if (authService.tienePermiso(permisoRequerido)) {
    return true;
  } else {
    console.warn(`Acceso denegado. Se requiere el permiso: ${permisoRequerido}`);
    router.navigate(['/panel/inicio']);
    return false;
  }
};