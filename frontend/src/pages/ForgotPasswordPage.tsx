import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { authApi, getApiErrorMessage } from '../api/client';
import { FormMessage } from '../components/FormMessage';

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const request = useMutation({ mutationFn: () => authApi.requestPasswordReset(email) });
  function submit(event: React.FormEvent<HTMLFormElement>) { event.preventDefault(); request.mutate(); }
  return <div className="auth-page auth-page-register"><form className="auth-card" onSubmit={submit}><div><span className="eyebrow">ACCOUNT RECOVERY</span><h2>Reset your password</h2><p className="muted">Enter your email and we’ll send you a secure reset link.</p></div>{request.isError && <FormMessage message={getApiErrorMessage(request.error)} />}{request.isSuccess ? <><p className="success-message">If an account exists for that email, a reset link is on its way.</p><Link className="button button-primary" to="/login">Back to sign in</Link></> : <><label>Email<input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required autoComplete="email" /></label><button className="button button-primary" disabled={request.isPending}>{request.isPending ? 'Sending…' : 'Send reset link'}</button><p className="form-footer"><Link to="/login">Back to sign in</Link></p></>}</form></div>;
}
