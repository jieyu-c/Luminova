import { useState } from 'react';
import { Navigate } from 'react-router-dom';
import { AiDock } from '../components/workspace/AiDock';
import { ContinueProjectCard } from '../components/workspace/ContinueProjectCard';
import { CurrentProjectPanel } from '../components/workspace/CurrentProjectPanel';
import { ProjectsPanel } from '../components/workspace/ProjectsPanel';
import { SidePanel } from '../components/workspace/SidePanel';
import { WorkspaceRail } from '../components/workspace/WorkspaceRail';
import type { WorkspaceView } from '../components/workspace/WorkspaceRail';
import { CanvasLibrary } from '../components/workspace/CanvasLibrary';
import { Header } from '../components/Header';
import { useAuth } from '../contexts/AuthContext';
import { useWorkspaceProjects } from '../hooks/useWorkspaceProjects';

export function WorkspacePage() {
  const { isAuthenticated, isLoading } = useAuth();
  const [activeView, setActiveView] = useState<WorkspaceView>('projects');
  const {
    projects,
    currentProject,
    stats,
    isLoading: isProjectLoading,
    isCreating,
    error: projectError,
    refreshProjects,
    createWorkspaceProject,
  } = useWorkspaceProjects({ enabled: isAuthenticated });

  if (isLoading) {
    return null;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <>
      <Header />
      <div className="app-shell">
        <WorkspaceRail
          activeView={activeView}
          onViewChange={(view) => {
            setActiveView(view);
            window.scrollTo({ top: 0, behavior: 'smooth' });
          }}
        />
        <div className={activeView === 'canvases' ? 'workspace-body workspace-body--canvas-library' : 'workspace-body'}>
          {activeView === 'canvases' ? (
            <CanvasLibrary />
          ) : (
            <>
              <div className="workspace-top">
                <div className="workspace-top__title">
                  <h1>创作工作台</h1>
                  <div className="workspace-top__chips">
                    <span className="chip live">进行中项目 {stats.activeTasks}</span>
                    <span className="chip review">待审核画布 {stats.pendingReview}</span>
                    <span className="chip canvas">生成任务 0</span>
                    <span className="chip export">最近导出 0</span>
                  </div>
                </div>
              </div>

              <div className="workspace-hero">
                <ContinueProjectCard project={currentProject} isLoading={isProjectLoading} />
                <AiDock
                  isCreating={isCreating}
                  error={projectError}
                  onCreateProject={createWorkspaceProject}
                />
              </div>

              <div className="workspace-layout">
                <div className="workspace-main">
                  <CurrentProjectPanel project={currentProject} />
                  <ProjectsPanel
                    projects={projects}
                    isLoading={isProjectLoading}
                    error={projectError}
                    onRetry={refreshProjects}
                  />
                </div>
                <SidePanel hasProjects={projects.length > 0} />
              </div>
            </>
          )}
        </div>
      </div>
    </>
  );
}
