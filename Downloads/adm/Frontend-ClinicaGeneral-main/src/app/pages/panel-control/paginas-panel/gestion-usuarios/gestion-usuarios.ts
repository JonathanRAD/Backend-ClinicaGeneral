// RUTA: src/app/pages/panel-control/paginas-panel/gestion-usuarios/gestion-usuarios.ts
import { Component, OnInit, Signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UsuarioService } from '../../../../services/usuario';
import { UserProfile } from '../../../../core/models/usuario';
import { TablaGenerica, ColumnConfig } from '../../../../shared/tabla-generica/tabla-generica';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormularioUsuarioComponent } from '../../componentes-panel/formulario-usuario/formulario-usuario';
import { DialogoConfirmacion } from '../../componentes-panel/dialogo-confirmacion/dialogo-confirmacion';
import { AutenticacionService } from '../../../../services/autenticacion';

@Component({
  selector: 'app-gestion-usuarios',
  standalone: true,
  imports: [CommonModule, TablaGenerica],
  templateUrl: './gestion-usuarios.html',
  styleUrls: ['./gestion-usuarios.css']
})
export class GestionUsuariosComponent implements OnInit {
  usuarios: Signal<UserProfile[]>;

  columnas: ColumnConfig[] = [
    { name: 'nombres', header: 'Nombres' },
    { name: 'apellidos', header: 'Apellidos' },
    { name: 'email', header: 'Correo Electrónico' },
    { name: 'rol', header: 'Rol' }
  ];

  constructor(
    private usuarioService: UsuarioService,
    public dialog: MatDialog,
    private snackBar: MatSnackBar,
    public authService: AutenticacionService
  ) {
    this.usuarios = this.usuarioService.usuarios;
  }

  ngOnInit(): void {
    this.usuarioService.getAllUsuarios().subscribe();
  }

  onAgregar(): void {
    const dialogRef = this.dialog.open(FormularioUsuarioComponent, {
      width: '500px',
      disableClose: true,
      panelClass: 'custom-dialog-container',
      data: { usuario: null } 
    });

    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado === true) {
        this.usuarioService.getAllUsuarios().subscribe();
        this.snackBar.open('Usuario creado con éxito', 'Cerrar', { duration: 3000 });
      }
    });
  }

  onEditar(usuario: UserProfile): void {
    const dialogRef = this.dialog.open(FormularioUsuarioComponent, {
      width: '500px',
      disableClose: true,
      panelClass: 'custom-dialog-container',
      data: { usuario: usuario }
    });

    dialogRef.afterClosed().subscribe(resultado => {
      if (resultado === true) {
        this.usuarioService.getAllUsuarios().subscribe();
        this.snackBar.open('Usuario actualizado con éxito', 'Cerrar', { duration: 3000 });
      }
    });
  }

  onEliminar(usuario: UserProfile): void {
    const dialogRef = this.dialog.open(DialogoConfirmacion, {
      width: '450px',
      data: {
        titulo: 'Confirmar Eliminación',
        mensaje: `¿Estás seguro de eliminar a ${usuario.nombres} ${usuario.apellidos}? Esta acción es permanente.`
      }
    });

    dialogRef.afterClosed().subscribe(confirmado => {
      if (confirmado && usuario.id) {
        this.usuarioService.eliminarUsuario(usuario.id).subscribe({
          next: () => {
            this.usuarioService.getAllUsuarios().subscribe();
            this.snackBar.open('Usuario eliminado', 'Cerrar', { duration: 3000 });
          },
          error: (err) => this.snackBar.open(err.error?.message || 'No se pudo eliminar al usuario', 'Cerrar', { duration: 3000 })
        });
      }
    });
  }
}