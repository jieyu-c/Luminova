import {
  Clapperboard,
  Film,
  Image,
  Layers,
  Maximize2,
  MessageSquare,
  Music,
  Palette,
  Plus,
  Sparkles,
  Type,
  Users,
} from 'lucide-react';
import { canvasToolboxItems } from '../../data/canvas';

const iconMap = {
  assets: Layers,
  character: Users,
  scene: Image,
  style: Palette,
  dialogue: MessageSquare,
  shot: Clapperboard,
  music: Music,
  effects: Sparkles,
  text: Type,
} as const;

type CanvasToolboxProps = {
  activeTool: string;
  onToolChange: (tool: string) => void;
};

export function CanvasToolbox({ activeTool, onToolChange }: CanvasToolboxProps) {
  return (
    <aside className="canvas-toolbox" aria-label="画布工具">
      {canvasToolboxItems.map((item) => {
        const Icon = iconMap[item.id];
        return (
          <button
            key={item.id}
            className={`canvas-toolbox__button ${activeTool === item.id ? 'is-active' : ''}`}
            type="button"
            title={item.label}
            onClick={() => onToolChange(item.id)}
          >
            <Icon size={18} />
            <span>{item.label}</span>
          </button>
        );
      })}

      <span className="canvas-toolbox__gap" />

      <button className="canvas-toolbox__button canvas-toolbox__button--add" type="button" title="添加节点">
        <Plus size={18} />
      </button>
      <button className="canvas-toolbox__button" type="button" title="全屏">
        <Maximize2 size={18} />
      </button>
      <button className="canvas-toolbox__button" type="button" title="影片">
        <Film size={18} />
      </button>
    </aside>
  );
}
