import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms'; // Importar NgForm
import { RouterModule } from '@angular/router';
import { AutenticacionService } from '../../services/autenticacion'; // Importar servicio
import { Notificacion } from '../../services/notificacion'; // Importar notificaciones

@Component({
  selector: 'app-recuperar-contrasena',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './recuperar-contrasena.html',
  styleUrls: ['./recuperar-contrasena.css']
})
export class RecuperarContrasena {
  email: string = '';
  correoEnviado: string = '';
  solicitudEnviada = signal(false);
  cargando = signal(false);
  mensajeError = signal(''); // Para mostrar errores

  constructor(
    private authService: AutenticacionService,
    private notificacion: Notificacion
  ) {}

  enviarSolicitud(form: NgForm) { // Recibe NgForm
    if (form.invalid || !this.email) {
      this.mensajeError.set('Por favor, ingresa un correo electrónico válido.');
      return;
    }
    this.cargando.set(true);
    this.mensajeError.set(''); // Limpia errores previos
    this.correoEnviado = this.email; // Guarda el correo antes de la llamada

    this.authService.solicitarReseteoContrasena(this.email).subscribe({
      next: (respuesta) => {
        this.solicitudEnviada.set(true);
        // this.notificacion.mostrar(respuesta.message || 'Solicitud enviada.'); // Opcional: Mostrar mensaje de éxito
        this.cargando.set(false);
      },
      error: (error: Error) => { // Captura el error específico
        // Muestra el mensaje de error que viene del servicio (manejado en handleError)
        this.mensajeError.set(error.message || 'Ocurrió un error al enviar la solicitud.');
        this.cargando.set(false);
        this.solicitudEnviada.set(false); // Asegúrate de que no muestre la pantalla de éxito
      }
    });
  }
}