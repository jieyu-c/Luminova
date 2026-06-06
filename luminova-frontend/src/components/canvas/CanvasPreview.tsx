import { flowNodes } from '../../data/home';

const wires = [
  { left: 132, top: 139, width: 54, rotate: 18 },
  { left: 274, top: 140, width: 52, rotate: -18 },
  { left: 144, top: 260, width: 54, rotate: -18 },
  { left: 276, top: 264, width: 52, rotate: 14 },
  { left: 330, top: 205, width: 54, rotate: 88 },
] as const;

export function CanvasPreview() {
  return (
    <div className="product-stage product-stage--hero">
      <div className="canvas-preview canvas-preview--mini" aria-label="Generation Graph 功能预览">
        <div className="preview-header">
          <span className="chip tech">Generation Graph</span>
          <div className="chips">
            <span className="chip">9:16</span>
            <span className="chip tech">cinematic-v3</span>
          </div>
        </div>

        <div className="preview-graph" aria-hidden="true">
          {wires.map((wire) => (
            <div
              className="wire"
              key={`${wire.left}-${wire.top}`}
              style={{
                left: `${wire.left}px`,
                top: `${wire.top}px`,
                width: `${wire.width}px`,
                transform: `rotate(${wire.rotate}deg)`,
              }}
            />
          ))}

          {flowNodes.map((node) => (
            <article className={`node ${node.kind}`} key={node.kind}>
              <b>{node.title}</b>
              <p>{node.detail}</p>
            </article>
          ))}
        </div>

        <div className="agent-chip">
          <small>AGENT · TRACE</small>
          自动拆分分镜并继承角色与场景上下文。
        </div>
      </div>
    </div>
  );
}
