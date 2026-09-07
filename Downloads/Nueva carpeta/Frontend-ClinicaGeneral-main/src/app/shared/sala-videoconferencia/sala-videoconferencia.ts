import { Component, ElementRef, OnInit, AfterViewInit, ViewChild, Input, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { Conferencia } from '../../core/models/conferencia';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';

declare var JitsiMeetExternalAPI: any;

@Component({
  selector: 'app-sala-videoconferencia',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule],
  templateUrl: './sala-videoconferencia.html',
  styleUrls: ['./sala-videoconferencia.css'],
  providers: [DatePipe]
})
export class SalaVideoconferencia implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('jitsiContainer', { static: false }) jitsiContainer!: ElementRef;
  @Input() conferencia!: Conferencia;
  @Input() isPaciente: boolean = false;
  @Input() nombreUsuario: string = 'Usuario';
  @Input() backRoute: string = '/';

  api: any;
  cargandoScript = true;

  // ── Estados de Telemedicina y Conexión ────────────────────────────────────
  pacienteEnLinea = false;
  duracionSegundos = 0;
  timerInterval: any;
  timerTexto = '00:00';

  constructor(
    private router: Router, 
    private datePipe: DatePipe,
    private cdRef: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    if (!window['JitsiMeetExternalAPI' as any]) {
      this.loadJitsiScript();
    } else {
      this.cargandoScript = false;
    }
  }

  ngAfterViewInit(): void {
    if (!this.cargandoScript) {
      this.initJitsi();
    }
  }

  ngOnDestroy(): void {
    this.limpiarRecursos();
  }

  private limpiarRecursos(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
    if (this.api) {
      this.api.dispose();
      this.api = null;
    }
  }

  loadJitsiScript(): void {
    const script = document.createElement('script');
    script.src = 'https://meet.jit.si/external_api.js';
    script.async = true;
    script.onload = () => {
      this.cargandoScript = false;
      this.initJitsi();
      this.cdRef.detectChanges();
    };
    document.body.appendChild(script);
  }

  iniciarTimer(): void {
    if (this.timerInterval) return;
    this.timerInterval = setInterval(() => {
      this.duracionSegundos++;
      const mins = Math.floor(this.duracionSegundos / 60).toString().padStart(2, '0');
      const secs = (this.duracionSegundos % 60).toString().padStart(2, '0');
      this.timerTexto = `${mins}:${secs}`;
      this.cdRef.detectChanges(); // Forzar detección en standalone
    }, 1000);
  }

  initJitsi(): void {
    if (!this.jitsiContainer || !this.conferencia) return;

    const fechaFormateada = this.datePipe.transform(this.conferencia.fechaProgramada, 'yyyy-MM-dd') || 'Fecha';
    const pacienteNombre = `${this.conferencia.paciente.nombres} ${this.conferencia.paciente.apellidos}`;
    const doctorNombre = `${this.conferencia.medico.nombres} ${this.conferencia.medico.apellidos}`;
    
    // Subject to be used as default local recording name
    const subject = `${fechaFormateada} - ${pacienteNombre} - ${doctorNombre}`;

    const options = {
      roomName: this.conferencia.nombreSala || 'SalaGenerica',
      width: '100%',
      height: '100%',
      parentNode: this.jitsiContainer.nativeElement,
      userInfo: {
        displayName: this.nombreUsuario
      },
      configOverwrite: {
        startWithAudioMuted: false,
        startWithVideoMuted: false,
        subject: subject,
        localRecording: {
          enabled: true,
          format: 'ogg'
        }
      },
      interfaceConfigOverwrite: {
        SHOW_JITSI_WATERMARK: false,
        TOOLBAR_BUTTONS: [
          'microphone', 'camera', 'closedcaptions', 'desktop', 'fullscreen',
          'fodeviceselection', 'hangup', 'profile', 'chat', 'recording',
          'livestreaming', 'etherpad', 'sharedvideo', 'settings', 'raisehand',
          'videoquality', 'filmstrip', 'invite', 'feedback', 'stats', 'shortcuts',
          'tileview', 'videobackgroundblur', 'download', 'help', 'mute-everyone',
          'security'
        ],
      }
    };

    this.api = new JitsiMeetExternalAPI('meet.jit.si', options);
    
    this.api.addEventListeners({
      videoConferenceJoined: () => {
        console.log('Te has unido a la conferencia.');
        this.iniciarTimer();
      },
      participantJoined: () => {
        this.pacienteEnLinea = true;
        this.cdRef.detectChanges();
      },
      participantLeft: () => {
        this.pacienteEnLinea = false;
        this.cdRef.detectChanges();
      },
      videoConferenceLeft: () => {
        console.log('Has dejado la conferencia.');
        this.salir();
      }
    });
  }

  salir(): void {
    this.limpiarRecursos();
    this.router.navigate([this.backRoute]);
  }
}
