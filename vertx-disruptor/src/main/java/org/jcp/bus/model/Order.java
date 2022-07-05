package org.jcp.bus.model;

public record Order(Long orderId,
                    Double amount,
                    Double limitPrice,
                    Double avgPrice,
                    String status,
                    String refId,
                    String asset,
                    String remark) {
}
