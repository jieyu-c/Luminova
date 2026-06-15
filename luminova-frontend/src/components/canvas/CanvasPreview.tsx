import { previewWires as wires } from '../../data/canvas-preview';

type PreviewNode = {
  kind: 'script' | 'role' | 'shot' | 'prompt' | 'scene' | 'video';
  title: string;
  detail: string;
};

const flowNodes: PreviewNode[] = [
  { kind: 'script', title: '剧本', detail: '开场 / 反转' },
  { kind: 'role', title: '角色', detail: '林夏 · 已锁定' },
  { kind: 'shot', title: '分镜 03', detail: '近景推镜' },
  { kind: 'prompt', title: 'Prompt', detail: '角色 + 运镜' },
  { kind: 'scene', title: '场景', detail: '雨夜便利店' },
  { kind: 'video', title: '视频 V3', detail: '首选版本' },
];

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
