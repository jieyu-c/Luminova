import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { CanvasPage } from './pages/CanvasPage';
import { AccountPage } from './pages/AccountPage';
import { WorkspacePage } from './pages/WorkspacePage';
import { AgentPage } from './pages/AgentPage';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route
            path="/"
            element={
              <div className="site-shell site-shell--home">
                <div className="ambient" aria-hidden="true" />
                <HomePage />
              </div>
            }
          />
          <Route
            path="/workspace"
            element={
              <div className="site-shell">
                <div className="ambient" aria-hidden="true" />
                <WorkspacePage />
              </div>
            }
          />
          <Route path="/agent" element={<AgentPage />} />
          <Route path="/canvas" element={<CanvasPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/account" element={<AccountPage />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
