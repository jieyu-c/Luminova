import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { AccountModal } from '../components/account/AccountModal';
import { Header } from '../components/Header';
import { useAuth } from '../contexts/AuthContext';

export function AccountPage() {
  const navigate = useNavigate();
  const { isAuthenticated, isLoading } = useAuth();
  const [isAccountOpen, setIsAccountOpen] = useState(true);

  if (isLoading) {
    return null;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  const closeAccount = () => {
    setIsAccountOpen(false);
    navigate('/workspace', { replace: true });
  };

  return (
    <>
      <Header />
      <main className="account-route-shell" aria-hidden="true" />
      <AccountModal open={isAccountOpen} onClose={closeAccount} />
    </>
  );
}
