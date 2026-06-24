import { useCallback, useEffect, useRef, useState, type CSSProperties } from 'react';
import {
  Background,
  BackgroundVariant,
  ReactFlow,
  ReactFlowProvider,
  type Connection,
  type EdgeTypes,
  type NodeTypes,
  type OnSelectionChangeParams,
  useEdgesState,
  useNodesState,
  useReactFlow,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import {
  initialCanvasEdges,
  initialCanvasNodes,
  type CanvasEdge,
  type CanvasNode,
} from '../data/canvas';
import { recentProjects } from '../data/workspace';
import { FlowNode } from '../components/canvas/FlowNode';
import { CanvasHeader } from '../components/canvas/CanvasHeader';
import { CanvasToolbox } from '../components/canvas/CanvasToolbox';
import { ShotSettingsPanel } from '../components/canvas/ShotSettingsPanel';
import { CanvasTimeline } from '../components/canvas/CanvasTimeline';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { takeStudioTransition, type StudioTransitionSnapshot } from '../lib/studioTransition';

const nodeTypes = {
  luminovaNode: FlowNode,
} satisfies NodeTypes;

const edgeTypes = {} satisfies EdgeTypes;

function getFallbackTransition(search: string): StudioTransitionSnapshot | null {
  const searchParams = new URLSearchParams(search);
  const studioProject = recentProjects.find((project) => project.id === searchParams.get('studio'));

  if (!studioProject) return null;

  return {
    title: studioProject.title,
    accent: studioProject.accent,
    origin: 'fallback',
    rect: {
      left: Math.max(28, Math.min(window.innerWidth * 0.18, 260)),
      top: Math.max(130, Math.min(window.innerHeight * 0.26, 230)),
      width: Math.max(280, Math.min(window.innerWidth * 0.28, 420)),
      height: 132,
    },
  };
}

function resolveStudioTransition(
  search: string,
  routeState: {
    playedStudioTransition?: boolean;
    studioTransition?: StudioTransitionSnapshot;
  } | null,
) {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return null;
  if (routeState?.playedStudioTransition) return null;

  return routeState?.studioTransition ?? takeStudioTransition() ?? getFallbackTransition(search);
}

function CanvasWorkspace() {
  const [nodes, , onNodesChange] = useNodesState<CanvasNode>(initialCanvasNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState<CanvasEdge>(
    initialCanvasEdges.map((edge) => ({ ...edge, type: 'smoothstep', animated: true })),
  );
  const [activeTool, setActiveTool] = useState('shot');
  const [zoom, setZoom] = useState(0.86);
  const { fitView, setViewport, getViewport } = useReactFlow<CanvasNode, CanvasEdge>();
  const location = useLocation();
  const [studioTransition, setStudioTransition] = useState<StudioTransitionSnapshot | null>(() => {
    const routeState = location.state as {
      playedStudioTransition?: boolean;
      studioTransition?: StudioTransitionSnapshot;
    } | null;
    return resolveStudioTransition(location.search, routeState);
  });

  useEffect(() => {
    const routeState = location.state as {
      playedStudioTransition?: boolean;
      studioTransition?: StudioTransitionSnapshot;
    } | null;
    const snapshot = resolveStudioTransition(location.search, routeState);
    if (!snapshot) return;
    setStudioTransition(snapshot);
  }, [location.key, location.search, location.state]);

  useEffect(() => {
    if (!studioTransition) return;

    const timer = window.setTimeout(() => setStudioTransition(null), 1120);

    return () => window.clearTimeout(timer);
  }, [studioTransition]);

  const onConnect = useCallback(
    (connection: Connection) => {
      setEdges((currentEdges) => [
        ...currentEdges,
        {
          ...connection,
          id: `${connection.source}-${connection.target}-${Date.now()}`,
          type: 'smoothstep',
          animated: true,
        },
      ]);
    },
    [setEdges],
  );

  const onSelectionChange = useCallback((_selection: OnSelectionChangeParams<CanvasNode, CanvasEdge>) => {}, []);

  const handleFitView = useCallback(() => {
    fitView({ padding: 0.22, duration: 280 });
  }, [fitView]);

  const handleZoomChange = useCallback(
    (nextZoom: number) => {
      const viewport = getViewport();
      setZoom(nextZoom);
      setViewport({ ...viewport, zoom: nextZoom }, { duration: 180 });
    },
    [getViewport, setViewport],
  );

  const zoomRef = useRef(zoom);
  zoomRef.current = zoom;

  const onMoveEnd = useCallback(() => {
    const viewport = getViewport();
    setZoom(viewport.zoom);
  }, [getViewport]);

  return (
    <div className={studioTransition ? 'canvas-page canvas-page--entering' : 'canvas-page'}>
      {studioTransition ? (
        <div className={`studio-transition studio-transition--${studioTransition.origin}`} aria-hidden="true">
          <div className="studio-transition__wash" />
          <div className="studio-transition__grid" />
          <div className="studio-transition__beam" />
          <div
            className="studio-transition__portal"
            style={
              {
                '--from-left': `${studioTransition.rect.left}px`,
                '--from-top': `${studioTransition.rect.top}px`,
                '--from-width': `${studioTransition.rect.width}px`,
                '--from-height': `${studioTransition.rect.height}px`,
                '--studio-accent': studioTransition.accent,
              } as CSSProperties
            }
          >
            <div className="studio-transition__portal-map" />
            <div className="studio-transition__portal-node studio-transition__portal-node--a" />
            <div className="studio-transition__portal-node studio-transition__portal-node--b" />
            <div className="studio-transition__portal-node studio-transition__portal-node--c" />
            <span>{studioTransition.title}</span>
          </div>
          <div className="studio-transition__aperture" />
          <div className="studio-transition__label">进入创作台</div>
        </div>
      ) : null}

      <CanvasHeader />

      <main className="canvas-shell">
        <CanvasToolbox activeTool={activeTool} onToolChange={setActiveTool} />

        <section className="canvas-flow-wrap" aria-label="节点画布">
          <ReactFlow
            nodes={nodes}
            edges={edges}
            nodeTypes={nodeTypes}
            edgeTypes={edgeTypes}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onConnect={onConnect}
            onSelectionChange={onSelectionChange}
            onMoveEnd={onMoveEnd}
            defaultViewport={{ x: 0, y: 0, zoom: 0.86 }}
            minZoom={0.4}
            maxZoom={1.4}
            snapToGrid
            snapGrid={[18, 18]}
            onlyRenderVisibleElements
            deleteKeyCode={null}
            proOptions={{ hideAttribution: true }}
          >
            <Background variant={BackgroundVariant.Dots} gap={22} size={1.2} color="rgba(148,163,184,0.18)" />
          </ReactFlow>
        </section>

        <ShotSettingsPanel />
      </main>

      <CanvasTimeline zoom={zoom} onZoomChange={handleZoomChange} onFitView={handleFitView} />
    </div>
  );
}

export function CanvasPage() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return null;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <ReactFlowProvider>
      <CanvasWorkspace />
    </ReactFlowProvider>
  );
}
