import { memo } from 'react';
import { Handle, Position, type NodeProps } from '@xyflow/react';
import { Copy, Play, Sparkles } from 'lucide-react';
import type { CanvasNode } from '../../data/canvas';

const statusText: Record<CanvasNode['data']['status'], string> = {
  ready: '就绪',
  running: '生成中',
  draft: '草稿',
};

function ColorPalette({ colors }: { colors: string[] }) {
  return (
    <div className="flow-node__palette">
      {colors.map((color) => (
        <span key={color} style={{ background: color }} />
      ))}
    </div>
  );
}

function ShotHeroNode({ data, selected }: NodeProps<CanvasNode>) {
  return (
    <article className={`flow-node flow-node--shot-hero ${selected ? 'is-selected' : ''}`}>
      <Handle className="flow-handle flow-handle--in" type="target" position={Position.Left} />
      <header className="flow-node__header">
        <span className="flow-node__badge">{data.label}</span>
        <strong>{data.title}</strong>
        <i data-status={data.status}>{statusText[data.status]}</i>
      </header>

      <div className="flow-node__preview">
        <img src={data.previewImage} alt="" />
        <button className="flow-node__play" type="button" aria-label="播放预览">
          <Play size={18} fill="currentColor" />
        </button>
        <span className="flow-node__time">00:00 / 00:06</span>
      </div>

      <div className="flow-node__prompt">
        <div className="flow-node__prompt-head">
          <span>提示词</span>
          <button type="button" aria-label="复制提示词">
            <Copy size={14} />
          </button>
        </div>
        <p>{data.prompt}</p>
      </div>

      <div className="flow-node__quick">
        <span>{data.duration}</span>
        <span>{data.camera}</span>
        <span>{data.shotSize}</span>
      </div>

      <button className="flow-node__generate" type="button">
        <Sparkles size={16} />
        生成镜头
      </button>

      <footer className="flow-node__footer">
        <button type="button">变体生成</button>
        <button type="button">镜头编辑</button>
      </footer>

      <Handle className="flow-handle flow-handle--out" type="source" position={Position.Right} />
    </article>
  );
}

function CharacterNode({ data, selected }: NodeProps<CanvasNode>) {
  return (
    <article className={`flow-node flow-node--character ${selected ? 'is-selected' : ''}`}>
      <Handle className="flow-handle flow-handle--out" type="source" position={Position.Right} />
      <header className="flow-node__header">
        <span className="flow-node__badge">{data.label}</span>
        <strong>{data.title}</strong>
      </header>
      <div className="flow-node__split">
        <img src={data.avatar} alt="" className="flow-node__avatar" />
        {data.palette ? <ColorPalette colors={data.palette} /> : null}
      </div>
    </article>
  );
}

function SceneRefNode({ data, selected }: NodeProps<CanvasNode>) {
  return (
    <article className={`flow-node flow-node--scene-ref ${selected ? 'is-selected' : ''}`}>
      <Handle className="flow-handle flow-handle--out" type="source" position={Position.Right} />
      <header className="flow-node__header">
        <span className="flow-node__badge">{data.label}</span>
        <strong>{data.title}</strong>
      </header>
      <div className="flow-node__thumb">
        <img src={data.image} alt="" />
      </div>
      {data.palette ? <ColorPalette colors={data.palette} /> : null}
    </article>
  );
}

function DialogueNode({ data, selected }: NodeProps<CanvasNode>) {
  return (
    <article className={`flow-node flow-node--dialogue ${selected ? 'is-selected' : ''}`}>
      <Handle className="flow-handle flow-handle--out" type="source" position={Position.Right} />
      <header className="flow-node__header">
        <span className="flow-node__badge">{data.label}</span>
        <strong>{data.title}</strong>
      </header>
      <p className="flow-node__dialogue">{data.dialogue}</p>
      <span className="flow-node__time-range">{data.timeRange}</span>
    </article>
  );
}

function StyleRefNode({ data, selected }: NodeProps<CanvasNode>) {
  return (
    <article className={`flow-node flow-node--style-ref ${selected ? 'is-selected' : ''}`}>
      <Handle className="flow-handle flow-handle--in" type="target" position={Position.Left} />
      <header className="flow-node__header">
        <span className="flow-node__badge">{data.label}</span>
        <strong>{data.title}</strong>
      </header>
      <div className="flow-node__thumb">
        <img src={data.image} alt="" />
      </div>
    </article>
  );
}

function ShotContextNode({ data, selected }: NodeProps<CanvasNode>) {
  return (
    <article className={`flow-node flow-node--shot-context ${selected ? 'is-selected' : ''}`}>
      <Handle className="flow-handle flow-handle--in" type="target" position={Position.Left} />
      <header className="flow-node__header">
        <span className="flow-node__badge">{data.label}</span>
        <strong>{data.title}</strong>
      </header>
      <div className="flow-node__context-pair">
        {data.prevShot ? (
          <div className="flow-node__context-item">
            <img src={data.prevShot.image} alt="" />
            <span>{data.prevShot.label}</span>
          </div>
        ) : null}
        {data.nextShot ? (
          <div className="flow-node__context-item">
            <img src={data.nextShot.image} alt="" />
            <span>{data.nextShot.label}</span>
          </div>
        ) : null}
      </div>
    </article>
  );
}

export const FlowNode = memo(function FlowNode(props: NodeProps<CanvasNode>) {
  switch (props.data.kind) {
    case 'shot':
      return <ShotHeroNode {...props} />;
    case 'character':
      return <CharacterNode {...props} />;
    case 'sceneRef':
      return <SceneRefNode {...props} />;
    case 'dialogue':
      return <DialogueNode {...props} />;
    case 'styleRef':
      return <StyleRefNode {...props} />;
    case 'shotContext':
      return <ShotContextNode {...props} />;
    default:
      return null;
  }
});
