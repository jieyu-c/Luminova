package cn.jieyuc.canvas.domain.model.canvas;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CanvasDomainTest {

    @Test
    void shouldRejectSelfConnectedEdge() {
        CanvasNodeId nodeId = CanvasNodeId.of(10L);

        assertThrows(
                IllegalArgumentException.class,
                () -> CanvasEdge.connect(
                        CanvasId.of(1L),
                        "self-edge",
                        nodeId,
                        nodeId,
                        CanvasEdgeType.DEFAULT
                )
        );
    }

    @Test
    void shouldMoveCanvasNode() {
        CanvasNode node = CanvasNode.create(
                CanvasId.of(1L),
                "shot-01",
                CanvasNodeType.SHOT,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        node.moveTo(new BigDecimal("120.5"), new BigDecimal("240.5"));

        assertEquals(new BigDecimal("120.5"), node.getPositionX());
        assertEquals(new BigDecimal("240.5"), node.getPositionY());
    }

}
