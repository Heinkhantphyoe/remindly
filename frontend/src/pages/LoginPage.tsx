import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { authApi, getApiErrorMessage } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { FormMessage } from '../components/FormMessage';

export function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { signIn } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname ?? '/dashboard';
  const login = useMutation({ mutationFn: () => authApi.login({ email, password }), onSuccess: ({ data }) => { signIn(data); navigate(from, { replace: true }); } });
  function submit(event: React.FormEvent<HTMLFormElement>) { event.preventDefault(); login.mutate(); }
  return <div className="auth-page"><div className="auth-intro"><span className="eyebrow">YOUR DAY, IN FOCUS</span><h1>Small reminders.<br /><em>Big relief.</em></h1><p>Keep the things that matter close, without keeping them all in your head.</p></div><form className="auth-card" onSubmit={submit}><div><span className="eyebrow">WELCOME BACK</span><h2>Sign in to Remindly</h2><p className="muted">Your personal space for what comes next.</p></div>{location.state?.registered && <p className="success-message">Account created. Check your email to verify it before signing in.</p>}{login.isError && <FormMessage message={getApiErrorMessage(login.error, 'Email or password is incorrect.')} />}<label>Email<input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required autoComplete="email" /></label><label>Password<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} required autoComplete="current-password" /></label><div className="password-help"><Link to="/forgot-password">Forgot password?</Link></div><button className="button button-primary" disabled={login.isPending}>{login.isPending ? 'Signing in…' : 'Sign in'}</button><p className="form-footer">New to Remindly? <Link to="/register">Create an account</Link></p></form></div>;
}
