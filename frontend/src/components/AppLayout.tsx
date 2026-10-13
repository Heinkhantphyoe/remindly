import { Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { NotificationBell } from './NotificationBell';

export function AppLayout() {
  const { user, signOut } = useAuth();
  const navigate = useNavigate();
  async function handleSignOut() { await signOut(); navigate('/login', { replace: true }); }
  return <div className="app-frame"><header className="topbar"><div><span className="brand-mark">R</span><span className="brand-name">Remindly</span></div><div className="topbar-actions"><NotificationBell /><span className="user-name">{user?.displayName}</span><button className="button button-quiet" onClick={handleSignOut}>Sign out</button></div></header><main className="content-area"><Outlet /></main></div>;
}
