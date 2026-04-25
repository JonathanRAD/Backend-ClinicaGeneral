package com.clinicabienestar.api.dto;

import lombok.Data;
import java.util.List;

@Data
public class DespachoDTO {
    private List<ItemDespacho> items;

    @Data
    public static class ItemDespacho {
        private Long medicamentoId;
        private Integer cantidad;
    }
}
