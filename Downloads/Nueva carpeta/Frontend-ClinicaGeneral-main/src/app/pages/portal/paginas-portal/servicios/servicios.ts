import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';

interface Servicio {
  icon: string;
  nombre: string;
  desc: string;
  imageUrl: string;
  datoDestacado: { valor: string; etiqueta: string };
  condiciones: { icon: string; nombre: string }[];
  procedimientos: { icon: string; nombre: string }[];
}

@Component({
  selector: 'app-servicios',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatButtonModule],
  templateUrl: './servicios.html',
  styleUrls: ['./servicios.css']
})
export class ServiciosComponent {
  servicios: Servicio[] = [
    { 
      icon: 'monitor_heart', 
      nombre: 'Cardiología', 
      desc: `Nos especializamos en el estudio, diagnóstico y tratamiento de las enfermedades del corazón y del sistema circulatorio.

      Qué tratamos: Hipertensión arterial, arritmias, colesterol alto, infartos de miocardio, angina de pecho e insuficiencia cardíaca.
      
      Nuestros Servicios: Electrocardiograma (ECG), ecocardiograma, pruebas de esfuerzo, monitoreo Holter 24h.
      
      Cuándo visitarnos: Si experimentas dolor en el pecho, palpitaciones, falta de aire, mareos o tienes antecedentes familiares de enfermedades cardíacas.`, 
      imageUrl: 'https://images.pexels.com/photos/4167541/pexels-photo-4167541.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1',
      datoDestacado: { valor: '+5,000', etiqueta: 'Chequeos Cardíacos' },
      condiciones: [
        { icon: 'monitor_heart', nombre: 'Hipertensión Arterial' },
        { icon: 'show_chart', nombre: 'Colesterol Alto' },
        { icon: 'bolt', nombre: 'Arritmias' }
      ],
      procedimientos: [
        { icon: 'assessment', nombre: 'Pruebas de Esfuerzo' },
        { icon: 'graphic_eq', nombre: 'Ecocardiogramas' },
        { icon: 'timer', nombre: 'Monitoreo Holter 24h' }
      ]
    },
    { 
      icon: 'child_care', 
      nombre: 'Pediatría', 
      desc: 'Ofrecemos un cuidado médico integral para lactantes, niños y adolescentes, acompañándolos en todas sus etapas de crecimiento.', 
      imageUrl: 'https://images.pexels.com/photos/3957987/pexels-photo-3957987.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1',
      datoDestacado: { valor: '+10,000', etiqueta: 'Niños Atendidos' },
      condiciones: [
        { icon: 'sick', nombre: 'Infecciones Comunes' },
        { icon: 'allergies', nombre: 'Alergias y Asma' },
        { icon: 'insights', nombre: 'Problemas de Desarrollo' }
      ],
      procedimientos: [
        { icon: 'vaccines', nombre: 'Esquema de Vacunación' },
        { icon: 'scale', nombre: 'Controles de Crecimiento' },
        { icon: 'restaurant_menu', nombre: 'Asesoría Nutricional' }
      ]
    },
    { 
      icon: 'medical_services', 
      nombre: 'Medicina General', 
      desc: 'Somos tu primer punto de contacto para el cuidado de tu salud. Nos enfocamos en la atención primaria, la prevención y el diagnóstico inicial.', 
      imageUrl: 'https://images.pexels.com/photos/4021775/pexels-photo-4021775.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1',
      datoDestacado: { valor: '98%', etiqueta: 'Satisfacción del Paciente' },
      condiciones: [
        { icon: 'ac_unit', nombre: 'Resfriados y Gripe' },
        { icon: 'healing', nombre: 'Heridas y Lesiones Menores' },
        { icon: 'check_circle', nombre: 'Chequeos Preventivos' }
      ],
      procedimientos: [
        { icon: 'assignment', nombre: 'Diagnóstico de Síntomas' },
        { icon: 'bloodtype', nombre: 'Análisis de Laboratorio' },
        { icon: 'transfer_within_a_station', nombre: 'Derivación a Especialistas' }
      ]
    },
    { 
      icon: 'masks', 
      nombre: 'Dermatología', 
      desc: 'Cuidamos la salud integral de tu piel, cabello y uñas, ofreciendo soluciones tanto médicas como estéticas.', 
      imageUrl: 'https://images.pexels.com/photos/668300/pexels-photo-668300.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1',
      datoDestacado: { valor: '+30', etiqueta: 'Tratamientos Innovadores' },
      condiciones: [
        { icon: 'flare', nombre: 'Acné y Rosácea' },
        { icon: 'grain', nombre: 'Psoriasis y Eczema' },
        { icon: 'visibility', nombre: 'Cáncer de Piel' }
      ],
      procedimientos: [
        { icon: 'search', nombre: 'Mapeo de Lunares' },
        { icon: 'biotech', nombre: 'Biopsias de Piel' },
        { icon: 'face_retouching_natural', nombre: 'Peelings Químicos' }
      ]
    },
    { 
      icon: 'pregnant_woman', 
      nombre: 'Ginecología', 
      desc: 'Brindamos atención especializada e integral en todas las etapas de la vida de la mujer, desde la adolescencia hasta la postmenopausia.', 
      imageUrl: 'https://images.pexels.com/photos/7089394/pexels-photo-7089394.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1',
      datoDestacado: { valor: '+2,000', etiqueta: 'Nacimientos Atendidos' },
      condiciones: [
        { icon: 'calendar_month', nombre: 'Trastornos Menstruales' },
        { icon: 'family_restroom', nombre: 'Planificación Familiar' },
        { icon: 'woman', nombre: 'Menopausia' }
      ],
      procedimientos: [
        { icon: 'summarize', nombre: 'Control Anual y Papanicolau' },
        { icon: 'baby_changing_station', nombre: 'Seguimiento de Embarazo' },
        { icon: 'ultrasound', nombre: 'Ecografías Pélvicas' }
      ]
    },
    { 
      icon: 'psychology', 
      nombre: 'Neurología', 
      desc: 'Nos dedicamos al diagnóstico y tratamiento de las enfermedades que afectan al sistema nervioso: el cerebro, la médula espinal y los nervios periféricos.', 
      imageUrl: 'https://images.pexels.com/photos/7659567/pexels-photo-7659567.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1',
      datoDestacado: { valor: '15+', etiqueta: 'Años de Experiencia' },
      condiciones: [
        { icon: 'severe_cold', nombre: 'Migrañas y Cefaleas' },
        { icon: 'hdr_strong', nombre: 'Epilepsia' },
        { icon: 'elderly', nombre: 'Enf. de Parkinson' }
      ],
      procedimientos: [
        { icon: 'memory', nombre: 'Evaluación de Memoria' },
        { icon: 'hearing', nombre: 'Estudios del Sueño' },
        { icon: 'personal_injury', nombre: 'Tratamiento del Dolor Crónico' }
      ]
    }
  ];

  servicioActivo = signal<Servicio>(this.servicios[0]);

  constructor(private router: Router) {}

  verMedicosPorEspecialidad(especialidad: string): void {
    this.router.navigate(['/portal/nuestros-medicos'], { 
      queryParams: { especialidad: especialidad } 
    });
  }
}