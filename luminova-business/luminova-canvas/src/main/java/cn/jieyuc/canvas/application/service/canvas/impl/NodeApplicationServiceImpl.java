package cn.jieyuc.canvas.application.service.canvas.impl;

import cn.jieyuc.canvas.application.service.canvas.NodeApplicationService;
import cn.jieyuc.canvas.domain.model.canvas.CanvasNode;
import cn.jieyuc.canvas.domain.model.canvas.CanvasNodeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class NodeApplicationServiceImpl implements NodeApplicationService {

    @Override
    public CanvasNode addNode(String nodeId, String nodeName, CanvasNodeType nodeType) {
        return null;
    }

    @Override
    public boolean deleteNode(String nodeId) {
        return false;
    }

    @Override
    public List<CanvasNode> listNodes(String canvasId) {
        return List.of();
    }

    @Override
    public CanvasNode getNode(String nodeId) {
        return null;
    }
}
