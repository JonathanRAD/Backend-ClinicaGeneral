import { Component, OnInit, Inject, ChangeDetectorRef  } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormControl, ReactiveFormsModule } from '@angular/forms';
import { CommonModule, TitleCasePipe } from '@angular/common';

import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';

import { AutenticacionService } from '../../../../services/autenticacion';
import { Notificacion } from '../../../../services/notificacion';
import { CreateUserPayload, UserProfile } from '../../../../core/models/usuario';
import { Rol } from '../../../../core/models/rol';
import { Permiso } from '../../../../core/models/permiso';
import { PermisoService } from '../../../../services/permiso.service';
import { UsuarioService } from '../../../../services/usuario';

@Component({
  selector: 'app-formulario-usuario',
  standalone: true, 
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCheckboxModule,
    MatIconModule,
    TitleCasePipe
  ],
  templateUrl: './formulario-usuario.html',
  styleUrls: ['./formulario-usuario.css']
})
export class FormularioUsuarioComponent implements OnInit {
  usuarioForm: FormGroup;
  esModoEdicion: boolean;
  rolesDisponibles = Object.values(Rol);
  permisosDisponibles: Permiso[] = [];

  // ── Diccionario de Permisos sugeridos por defecto según el Rol ───────────
  private permisosPorDefecto: Record<Rol, string[]> = {
    [Rol.ADMINISTRADOR]: [
      'VER_PACIENTES', 'GESTIONAR_PACIENTES',
      'VER_HISTORIAL_CLINICO', 'EDITAR_HISTORIAL_CLINICO',
      'VER_CITAS', 'GESTIONAR_CITAS',
      'VER_FACTURACION', 'GESTIONAR_FACTURACION',
      'VER_INVENTARIO', 'GESTIONAR_INVENTARIO',
      'VER_MEDICOS', 'GESTIONAR_MEDICOS',
      'VER_USUARIOS', 'GESTIONAR_USUARIOS', 'GENERAR_REPORTES'
    ],
    [Rol.MEDICO]: [
      'VER_PACIENTES',
      'VER_HISTORIAL_CLINICO', 'EDITAR_HISTORIAL_CLINICO',
      'VER_CITAS', 'GESTIONAR_CITAS',
      'VER_INVENTARIO',
      'VER_MEDICOS'
    ],
    [Rol.RECEPCIONISTA]: [
      'VER_PACIENTES', 'GESTIONAR_PACIENTES',
      'VER_CITAS', 'GESTIONAR_CITAS',
      'VER_INVENTARIO',
      'VER_MEDICOS'
    ],
    [Rol.CAJERO]: [
      'VER_PACIENTES',
      'VER_FACTURACION', 'GESTIONAR_FACTURACION',
      'VER_INVENTARIO'
    ],
    [Rol.ENFERMERA]: [
      'VER_PACIENTES',
      'VER_HISTORIAL_CLINICO',
      'VER_CITAS', 'GESTIONAR_CITAS',
      'VER_INVENTARIO'
    ],
    [Rol.PACIENTE]: [
      'VER_CITAS'
    ]
  };

  // ── Categorización descriptiva de permisos para la interfaz visual ───────
  categoriasPermisos = [
    {
      titulo: 'Módulo de Pacientes',
      icon: 'person',
      permisos: [
        { nombre: 'VER_PACIENTES', label: 'Visualizar listado de pacientes' },
        { nombre: 'GESTIONAR_PACIENTES', label: 'Crear, editar y dar de baja pacientes' }
      ]
    },
    {
      titulo: 'Historias Clínicas',
      icon: 'history_edu',
      permisos: [
        { nombre: 'VER_HISTORIAL_CLINICO', label: 'Ver historial clínico completo del paciente' },
        { nombre: 'EDITAR_HISTORIAL_CLINICO', label: 'Registrar consultas y editar historia clínica' }
      ]
    },
    {
      titulo: 'Citas y Triaje Vital',
      icon: 'event',
      permisos: [
        { nombre: 'VER_CITAS', label: 'Visualizar agenda y calendario de citas' },
        { nombre: 'GESTIONAR_CITAS', label: 'Crear citas, registrar triajes y signos vitales' }
      ]
    },
    {
      titulo: 'Facturación y Caja',
      icon: 'payments',
      permisos: [
        { nombre: 'VER_FACTURACION', label: 'Ver listado de facturas emitidas' },
        { nombre: 'GESTIONAR_FACTURACION', label: 'Registrar cobros, facturas y enviar comprobantes' }
      ]
    },
    {
      titulo: 'Farmacia e Inventario',
      icon: 'inventory_2',
      permisos: [
        { nombre: 'VER_INVENTARIO', label: 'Ver inventario de medicamentos' },
        { nombre: 'GESTIONAR_INVENTARIO', label: 'Gestionar stock de medicamentos y lotes' }
      ]
    },
    {
      titulo: 'Personal Médico',
      icon: 'badge',
      permisos: [
        { nombre: 'VER_MEDICOS', label: 'Ver listado de personal médico' },
        { nombre: 'GESTIONAR_MEDICOS', label: 'Crear y editar fichas de médicos' }
      ]
    },
    {
      titulo: 'Administración Global',
      icon: 'admin_panel_settings',
      permisos: [
        { nombre: 'VER_USUARIOS', label: 'Ver listado de usuarios del sistema' },
        { nombre: 'GESTIONAR_USUARIOS', label: 'Crear, editar y eliminar usuarios y roles' },
        { nombre: 'GENERAR_REPORTES', label: 'Exportar reportes en PDF y Excel' }
      ]
    }
  ];

  constructor(
    private fb: FormBuilder,
    private authService: AutenticacionService,
    private usuarioService: UsuarioService,
    private notificacion: Notificacion,
    private permisoService: PermisoService,
    public dialogRef: MatDialogRef<FormularioUsuarioComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { usuario: UserProfile },
    private cdRef: ChangeDetectorRef
  ) {
    this.esModoEdicion = !!this.data.usuario; 

    this.usuarioForm = this.fb.group({
      nombres: ['', Validators.required],
      apellidos: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      rol: [Rol.PACIENTE, Validators.required],
      password: ['', this.esModoEdicion ? [] : [Validators.required, Validators.minLength(8)]],
      permisos: this.fb.group({}) // Se poblará dinámicamente como sub-FormGroup
    });

    if (this.esModoEdicion && this.data.usuario) {
      this.usuarioForm.patchValue({
        nombres: this.data.usuario.nombres,
        apellidos: this.data.usuario.apellidos,
        email: this.data.usuario.email,
        rol: this.data.usuario.rol
      });
    }
  }

  ngOnInit(): void {
    this.cargarPermisos();
  }

  cargarPermisos(): void {
    this.permisoService.getAllPermisos().subscribe(permisos => {
      this.permisosDisponibles = permisos;
      this.inicializarPermisosGroup();

      if (this.esModoEdicion && this.data.usuario?.permisos) {
        this.marcarPermisosActuales(this.data.usuario.permisos);
      } else {
        // En creación, aplicar de entrada los permisos sugeridos para el rol inicial (PACIENTE)
        this.aplicarPermisosSugeridos(this.usuarioForm.get('rol')?.value);
      }

      // Activar la escucha reactiva tras completar la inicialización
      this.iniciarEscuchaRol();
      this.cdRef.detectChanges(); 
    });
  }

  private inicializarPermisosGroup() {
    const groupControls: any = {};
    this.permisosDisponibles.forEach(p => {
      groupControls[p.nombre] = new FormControl(false);
    });
    this.usuarioForm.setControl('permisos', this.fb.group(groupControls));
  }

  private marcarPermisosActuales(permisosDelUsuario: string[]) {
    const updateValues: any = {};
    this.permisosDisponibles.forEach(p => {
      updateValues[p.nombre] = permisosDelUsuario.includes(p.nombre);
    });
    this.usuarioForm.get('permisos')?.patchValue(updateValues);
  }

  private iniciarEscuchaRol() {
    this.usuarioForm.get('rol')?.valueChanges.subscribe((nuevoRol: Rol) => {
      this.aplicarPermisosSugeridos(nuevoRol);
      this.notificacion.mostrar(`Se aplicaron los permisos sugeridos para el rol de ${nuevoRol.toLowerCase()}`, 'exito');
    });
  }

  aplicarPermisosSugeridos(rol: Rol): void {
    const sugeridos = this.permisosPorDefecto[rol] || [];
    const updateValues: any = {};
    
    this.permisosDisponibles.forEach(p => {
      updateValues[p.nombre] = sugeridos.includes(p.nombre);
    });
    
    this.usuarioForm.get('permisos')?.patchValue(updateValues);
    this.cdRef.detectChanges();
  }

  tienePermisoDisponible(nombre: string): boolean {
    return this.permisosDisponibles.some(p => p.nombre === nombre);
  }

  cancelar(): void {
    this.dialogRef.close();
  }

  guardar(): void {
    if (this.usuarioForm.invalid) {
      this.usuarioForm.markAllAsTouched();
      return; 
    }

    if (this.esModoEdicion) {
      this.actualizarUsuarioExistente();
    } else {
      this.crearNuevoUsuario();
    }
  }

  private actualizarUsuarioExistente(): void {
    const permisosGroupValue = this.usuarioForm.value.permisos;
    const selectedPermisosNombres = Object.keys(permisosGroupValue)
      .filter(key => permisosGroupValue[key]);

    const payload: UserProfile = {
      ...this.data.usuario, 
      nombres: this.usuarioForm.value.nombres,
      apellidos: this.usuarioForm.value.apellidos,
      email: this.usuarioForm.value.email,
      rol: this.usuarioForm.value.rol,
      permisos: selectedPermisosNombres,
    };
    
    if (this.data.usuario.id) {
      this.usuarioService.actualizarUsuario(this.data.usuario.id, payload).subscribe({
        next: () => {
          this.notificacion.mostrar('Usuario actualizado correctamente', 'exito');
          this.dialogRef.close(true);
        },
        error: (err) => {
          const mensajeError = err.error?.message || 'Ocurrió un error desconocido';
          this.notificacion.mostrar('Error al actualizar el usuario: ' + mensajeError, 'error');
        }
      });
    }
  }
  
  private crearNuevoUsuario(): void {
    const permisosGroupValue = this.usuarioForm.value.permisos;
    const selectedPermisosNombres = Object.keys(permisosGroupValue)
      .filter(key => permisosGroupValue[key]);

    const nuevoUsuario: CreateUserPayload = {
      nombres: this.usuarioForm.value.nombres,
      apellidos: this.usuarioForm.value.apellidos,
      email: this.usuarioForm.value.email,
      password: this.usuarioForm.value.password,
      rol: this.usuarioForm.value.rol,
      permisos: selectedPermisosNombres
    };

    this.authService.crearUsuarioPorAdmin(nuevoUsuario).subscribe({
      next: () => {
        this.notificacion.mostrar('Usuario creado correctamente', 'exito');
        this.dialogRef.close(true); 
      },
      error: (err) => {
        const mensajeError = err.error?.message || 'Ocurrió un error desconocido';
        this.notificacion.mostrar('Error al crear el usuario: ' + mensajeError, 'error');
      }
    });
  }
}