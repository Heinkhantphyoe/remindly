import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Link, useSearchParams } from 'react-router-dom';
import { authApi, getApiErrorMessage } from '../api/client';
import { FormMessage } from '../components/FormMessage';

export function ResetPasswordPage() {
  const [params] = useSearchParams();
  const token = params.get('token') ?? '';
  const [password, setPassword] = useState('');
  const reset = useMutation({ mutationFn: () => authApi.resetPassword(token, password) });
  function submit(event: React.FormEvent<HTMLFormElement>) { event.preventDefault(); reset.mutate(); }
  return <div className="auth-page auth-page-register"><form className="auth-card" onSubmit={submit}><div><span className="eyebrow">ACCOUNT RECOVERY</span><h2>Choose a new password</h2><p className="muted">Your new password must be at least 8 characters.</p></div>{reset.isError && <FormMessage message={getApiErrorMessage(reset.error, 'This reset link is invalid or expired.')} />}{reset.isSuccess ? <><p className="success-message">Your password has been updated.</p><Link className="button button-primary" to="/login">Sign in</Link></> : <><label>New password<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} required minLength={8} autoComplete="new-password" /></label><button className="button button-primary" disabled={reset.isPending || !token}>{reset.isPending ? 'Updating…' : 'Update password'}</button><p className="form-footer"><Link to="/login">Back to sign in</Link></p></>}</form></div>;
}
