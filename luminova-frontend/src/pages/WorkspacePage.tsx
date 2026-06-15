import { Navigate } from 'react-router-dom';
import { AiDock } from '../components/workspace/AiDock';
import { ContinueProjectCard } from '../components/workspace/ContinueProjectCard';
import { CurrentProjectPanel } from '../components/workspace/CurrentProjectPanel';
import { ProjectsPanel } from '../components/workspace/ProjectsPanel';
import { SidePanel } from '../components/workspace/SidePanel';
import { WorkspaceRail } from '../components/workspace/WorkspaceRail';
import { Header } from '../components/Header';
import { useAuth } from '../contexts/AuthContext';
import { workspaceStats } from '../data/workspace';

export function WorkspacePage() {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <>
      <Header />
      <div className="app-shell">
        <WorkspaceRail />
        <div className="workspace-body">
          <div className="workspace-top">
            <div className="workspace-top__title">
              <h1>创作工作台</h1>
              <div className="workspace-top__chips">
                <span className="chip live">{workspaceStats.activeTasks} 任务进行中</span>
                <span className="chip review">{workspaceStats.pendingReview} 待审核</span>
              </div>
            </div>
          </div>

          <div className="workspace-hero">
            <ContinueProjectCard />
            <AiDock />
          </div>

          <div className="workspace-layout">
            <div className="workspace-main">
              <CurrentProjectPanel />
              <ProjectsPanel />
            </div>
            <SidePanel />
          </div>
        </div>
      </div>
    </>
  );
}
