package com.clinicabienestar.api.repository;

import com.clinicabienestar.api.model.LoteMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LoteMedicamentoRepository extends JpaRepository<LoteMedicamento, Long> {
    List<LoteMedicamento> findByMedicamentoId(Long medicamentoId);

    // FEFO: Primero expira, primero se despacha. Solo lotes con stock > 0
    @Query("SELECT l FROM LoteMedicamento l WHERE l.medicamento.id = :medicamentoId AND l.stock > 0 ORDER BY l.fechaVencimiento ASC")
    List<LoteMedicamento> findLotesConStockOrdenadosPorVencimiento(Long medicamentoId);
}
