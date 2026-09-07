import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { AutenticacionService } from '../../services/autenticacion';

export const autenticacionGuard: CanActivateFn = (route, state): boolean | UrlTree => {
  const authService = inject(AutenticacionService);
  const router = inject(Router);

  if (authService.estaLogueado()) {
    return true;
  }
  
  return router.createUrlTree(['/login']);
};