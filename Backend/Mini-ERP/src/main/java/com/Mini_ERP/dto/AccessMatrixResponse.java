package com.Mini_ERP.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AccessMatrixResponse {

    private List<AccessMatrixTab> tabs;

    @Data
    @Builder
    public static class AccessMatrixTab {
        private String key;
        private String label;
        private List<AccessMatrixRow> rows;
    }

    @Data
    @Builder
    public static class AccessMatrixRow {
        private String field;
        private String create;
        private String view;
        private String edit;
        private String delete;
    }
}
