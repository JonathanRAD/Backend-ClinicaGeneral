import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AutenticacionService } from '../../services/autenticacion';

export const pacienteGuard: CanActivateFn = () => {
  const authService = inject(AutenticacionService);
  const router = inject(Router);
  const rol = authService.rolUsuario();
  if (rol === 'PACIENTE') {
    return true;
  }

  return router.createUrlTree(['/panel']);
};