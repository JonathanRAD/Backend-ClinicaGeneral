// RUTA: src/main/java/com/clinicabienestar/api/service/CitaService.java
package com.clinicabienestar.api.service;

import com.clinicabienestar.api.dto.CitaDTO;
import com.clinicabienestar.api.dto.AgendarCitaPacienteDTO;
import com.clinicabienestar.api.exception.ForbiddenException;
import com.clinicabienestar.api.exception.ResourceNotFoundException;
import com.clinicabienestar.api.model.Cita;
import com.clinicabienestar.api.model.Medico;
import com.clinicabienestar.api.model.Paciente;
import com.clinicabienestar.api.model.Usuario;
import com.clinicabienestar.api.repository.CitaRepository;
import com.clinicabienestar.api.repository.MedicoRepository;
import com.clinicabienestar.api.repository.PacienteRepository;
import com.clinicabienestar.api.model.AccionAudit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CitaService {

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final AuditService auditService;
    private final EmailService emailService;

    @Value("${google.oauth.email-from}")
    private String adminEmail;

    private Usuario getUsuarioActual() {
        return (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Transactional(readOnly = true)
    public List<Cita> obtenerTodasLasCitas() {
        return citaRepository.listarTodasSP();
    }
    
    @Transactional(readOnly = true)
    public List<Cita> obtenerMisCitas() {
        Long usuarioId = getUsuarioActual().getId();
        return citaRepository.findByPacienteUsuarioId(usuarioId);
    }
    
    public Cita agendarCitaPaciente(CitaDTO citaDTO) {
        Long usuarioId = getUsuarioActual().getId();
        Paciente paciente = pacienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de paciente no encontrado para el usuario actual"));
        
        return crearNuevaCita(citaDTO, paciente);
    }
    public Cita agendarCitaPaciente(AgendarCitaPacienteDTO citaDTO) {
        Long usuarioId = getUsuarioActual().getId();
        Paciente paciente = pacienteRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de paciente no encontrado para el usuario actual"));
        
        CitaDTO citaCompleta = new CitaDTO();
        citaCompleta.setPacienteId(paciente.getId());
        citaCompleta.setMedicoId(citaDTO.getMedicoId());
        citaCompleta.setFechaHora(citaDTO.getFechaHora());
        citaCompleta.setMotivo(citaDTO.getMotivo());

        return crearNuevaCita(citaCompleta, paciente);
    }

    public Cita crearCita(CitaDTO citaDTO) {
        Paciente paciente = pacienteRepository.buscarPorIdSP(citaDTO.getPacienteId()); // Optimizado con SP
        if(paciente == null) throw new ResourceNotFoundException("Paciente no encontrado");
        
        return crearNuevaCita(citaDTO, paciente);
    }

    public Cita actualizarCita(Long id, CitaDTO citaDTO) {
        // USANDO SP para buscar
        Cita cita = citaRepository.buscarPorIdSP(id);
        if(cita == null) throw new ResourceNotFoundException("Cita no encontrada con ID: " + id);
        
        Paciente paciente = pacienteRepository.buscarPorIdSP(citaDTO.getPacienteId());
        if(paciente == null) throw new ResourceNotFoundException("Paciente no encontrado");

        Medico medico = medicoRepository.buscarPorIdSP(citaDTO.getMedicoId());
        if(medico == null) throw new ResourceNotFoundException("Médico no encontrado");

        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setFechaHora(LocalDateTime.ofInstant(citaDTO.getFechaHora(), ZoneId.systemDefault()));
        cita.setMotivo(citaDTO.getMotivo());
        cita.setConsultorio(asignarConsultorio(medico.getEspecialidad()));

        String anteriorJson = null;
        try {
            anteriorJson = auditService.toJson(cita);
        } catch (Exception e) {}

        Cita guardada = citaRepository.save(cita);
        try {
            auditService.registrarEvento(AccionAudit.ACTUALIZAR, "CITA", guardada.getId(), "Actualización de cita médica", anteriorJson, auditService.toJson(guardada));
        } catch (Exception e) {}
        return guardada;
    }

    public void cancelarMiCita(Long id) {
        Long usuarioId = getUsuarioActual().getId();
        // Usamos JPA normal aquí para acceder a las relaciones anidadas de forma segura
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + id));

        if (!cita.getPaciente().getUsuario().getId().equals(usuarioId)) {
            throw new ForbiddenException("No tiene permiso para cancelar una cita que no es suya.");
        }

        String anteriorJson = null;
        try {
            anteriorJson = auditService.toJson(cita);
        } catch (Exception e) {}

        // USANDO SP para eliminar
        citaRepository.eliminarCitaSP(id);
        try {
            auditService.registrarEvento(AccionAudit.ELIMINAR, "CITA", id, "Paciente canceló/eliminó su propia cita médica", anteriorJson, null);
        } catch (Exception e) {}
    }

    public void eliminarCita(Long id) {
        Cita cita = citaRepository.buscarPorIdSP(id);
        if (cita == null) {
            throw new ResourceNotFoundException("Cita no encontrada con ID: " + id);
        }
        String anteriorJson = null;
        try {
            anteriorJson = auditService.toJson(cita);
        } catch (Exception e) {}

        // USANDO SP
        citaRepository.eliminarCitaSP(id);
        try {
            auditService.registrarEvento(AccionAudit.ELIMINAR, "CITA", id, "Eliminación de cita médica por administración", anteriorJson, null);
        } catch (Exception e) {}
    }

    public Cita registrarTriaje(Long id, com.clinicabienestar.api.dto.TriajeDTO triajeDTO) {
        // Usar findById para poder cargar y guardar anidados con JPA
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + id));

        com.clinicabienestar.api.model.Triaje triaje = cita.getTriaje() != null ? cita.getTriaje() : new com.clinicabienestar.api.model.Triaje();
        
        triaje.setPeso(triajeDTO.getPeso());
        triaje.setAltura(triajeDTO.getAltura());
        triaje.setTemperatura(triajeDTO.getTemperatura());
        triaje.setPresionArterial(triajeDTO.getPresionArterial());
        triaje.setRitmoCardiaco(triajeDTO.getRitmoCardiaco());
        triaje.setSaturacionOxigeno(triajeDTO.getSaturacionOxigeno());
        triaje.setNivelAzucar(triajeDTO.getNivelAzucar());
        triaje.setMotivoConsulta(triajeDTO.getMotivoConsulta());
        triaje.setNotasOpcionales(triajeDTO.getNotasOpcionales());
        
        if (triaje.getFechaRegistro() == null) {
            triaje.setFechaRegistro(LocalDateTime.now());
        }
        
        triaje.setCita(cita);
        cita.setTriaje(triaje);
        cita.setEstado("lista_consulta");

        String anteriorJson = null;
        try {
            anteriorJson = auditService.toJson(cita);
        } catch (Exception e) {}

        Cita guardada = citaRepository.save(cita);
        try {
            auditService.registrarEvento(AccionAudit.CAMBIAR_ESTADO, "CITA", guardada.getId(), "Registro de triaje para la cita. Estado cambiado a lista_consulta.", anteriorJson, auditService.toJson(guardada));
        } catch (Exception e) {}
        return guardada;
    }

    // --- Métodos privados de lógica de negocio ---

    private Cita crearNuevaCita(CitaDTO citaDTO, Paciente paciente) {
        Medico medico = medicoRepository.buscarPorIdSP(citaDTO.getMedicoId());
        if(medico == null) throw new ResourceNotFoundException("Médico no encontrado");

        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setMedico(medico);

        LocalDateTime fechaHoraLocal = LocalDateTime.ofInstant(citaDTO.getFechaHora(), ZoneId.systemDefault());
        cita.setFechaHora(fechaHoraLocal);

        cita.setMotivo(citaDTO.getMotivo());
        cita.setEstado("programada");
        cita.setConsultorio(asignarConsultorio(medico.getEspecialidad()));
        cita.setNumeroTurno(calcularNumeroTurno(medico.getId(), fechaHoraLocal.toLocalDate()));

        Cita citaGuardada = citaRepository.save(cita);

        try {
            auditService.registrarEvento(AccionAudit.CREAR, "CITA", citaGuardada.getId(), "Creación de cita médica para el paciente: " + paciente.getNombres() + " " + paciente.getApellidos() + " con el médico: Dr(a). " + medico.getNombres() + " " + medico.getApellidos(), null, auditService.toJson(citaGuardada));
        } catch (Exception e) {}

        // --- Notificaciones por email (asíncronas, no bloquean la respuesta) ---
        String nombrePacienteEmail = paciente.getNombres() + " " + paciente.getApellidos();
        String nombreMedicoEmail = "Dr(a). " + medico.getNombres() + " " + medico.getApellidos();

        // Correo al paciente (solo si tiene usuario con email registrado)
        if (paciente.getUsuario() != null && paciente.getUsuario().getEmail() != null) {
            emailService.sendCitaConfirmationEmail(
                    paciente.getUsuario().getEmail(),
                    nombrePacienteEmail,
                    nombreMedicoEmail,
                    medico.getEspecialidad(),
                    citaGuardada.getFechaHora(),
                    citaGuardada.getMotivo(),
                    citaGuardada.getConsultorio(),
                    citaGuardada.getNumeroTurno(),
                    false // esAdmin = false → correo personalizado para el paciente
            );
        }

        // Correo al administrador
        emailService.sendCitaConfirmationEmail(
                adminEmail,
                nombrePacienteEmail,
                nombreMedicoEmail,
                medico.getEspecialidad(),
                citaGuardada.getFechaHora(),
                citaGuardada.getMotivo(),
                citaGuardada.getConsultorio(),
                citaGuardada.getNumeroTurno(),
                true // esAdmin = true → aviso para el admin
        );

        return citaGuardada;
    }

    private String asignarConsultorio(String especialidad) {
        if (especialidad == null) return "Piso 1, Admisión";
        return switch (especialidad.toLowerCase()) {
            case "medicina general" -> "Piso 1, Consultorio 102";
            case "dermatología" -> "Piso 1, Consultorio 105";
            case "pediatría" -> "Piso 2, Consultorio 205";
            case "ginecología" -> "Piso 2, Consultorio 210";
            case "cardiología" -> "Piso 3, Consultorio 301";
            case "neurología" -> "Piso 3, Consultorio 304";
            case "traumatología" -> "Piso 3, Consultorio 308";
            default -> "Piso 1, Admisión";
        };
    }

    private int calcularNumeroTurno(Long medicoId, LocalDate fechaCita) {
        LocalDateTime startOfDay = fechaCita.atStartOfDay();
        LocalDateTime endOfDay = fechaCita.plusDays(1).atStartOfDay();
        long citasDelDia = citaRepository.countByMedicoAndDateRange(medicoId, startOfDay, endOfDay);
        return (int) citasDelDia + 1;
    }

}