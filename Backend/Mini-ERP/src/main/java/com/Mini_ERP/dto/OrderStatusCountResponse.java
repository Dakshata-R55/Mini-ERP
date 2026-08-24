package com.Mini_ERP.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatusCountsResponse {

    @Builder.Default
    private int draft = 0;

    @Builder.Default
    private int confirmed = 0;

  /** PARTIALLY_DELIVERED / PARTIALLY_RECEIVED */
    @Builder.Default
    private int partial = 0;

  /** FULLY_DELIVERED / FULLY_RECEIVED / DONE */
    @Builder.Default
    private int completed = 0;

    @Builder.Default
    private int late = 0;

  /** Manufacturing only */
    @Builder.Default
    private int inProgress = 0;

  /** Manufacturing only */
    @Builder.Default
    private int toClose = 0;
}