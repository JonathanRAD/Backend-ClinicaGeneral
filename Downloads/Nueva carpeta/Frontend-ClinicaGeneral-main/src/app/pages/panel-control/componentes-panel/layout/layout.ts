import { Component, OnInit, signal, WritableSignal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AutenticacionService } from '../../../../services/autenticacion';
import { UsuarioService } from '../../../../services/usuario';
import { UserProfile } from '../../../../core/models/usuario';
import { ThemeService } from '../../../../services/theme.service';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatIconModule
  ],
  templateUrl: './layout.html',
  styleUrls: ['./layout.css']
})
export class LayoutComponent implements OnInit {
  usuario: WritableSignal<UserProfile | null> = signal(null);

  constructor(
    public authService: AutenticacionService,
    private usuarioService: UsuarioService,
    public themeService: ThemeService
  ) {}

  puedeVer(permiso: string): boolean {
    return this.authService.tienePermiso(permiso);
  }

  ngOnInit(): void {
    this.usuarioService.getMiPerfil().subscribe(perfil => {
      this.usuario.set(perfil);
    });
  }

  cerrarSesion(): void {
    this.authService.logout();
  }
}