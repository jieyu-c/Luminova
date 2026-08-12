package cn.jieyuc.canvas.application.service.canvas;

import cn.jieyuc.canvas.domain.model.canvas.CanvasNode;
import cn.jieyuc.canvas.domain.model.canvas.CanvasNodeType;

import java.util.List;

public interface NodeApplicationService {
    /**
     * 添加节点
     */
    CanvasNode addNode(String nodeId, String nodeName, CanvasNodeType nodeType);

    /**
     * 删除节点
     * @param nodeId 节点ID
     * @return 是否删除成功
     */
    boolean deleteNode(String nodeId);

    /**
     * 列举画布中的节点
     * @param canvasId 画布ID
     * @return 画布中的所有节点
     */
    List<CanvasNode> listNodes(String canvasId);

    /**
     * 获取节点信息
     * @param nodeId 节点的ID
     * @return 节点信息
     */
    CanvasNode getNode(String nodeId);
}
