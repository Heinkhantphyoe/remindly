import { useEffect } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Link, useSearchParams } from 'react-router-dom';
import { authApi, getApiErrorMessage } from '../api/client';
import { FormMessage } from '../components/FormMessage';

export function VerifyEmailPage() {
  const [params] = useSearchParams();
  const token = params.get('token') ?? '';
  const verify = useMutation({ mutationFn: () => authApi.verifyEmail(token) });
  useEffect(() => { if (token) verify.mutate(); }, [token]);
  return <div className="auth-page auth-page-register"><div className="auth-card"><div><span className="eyebrow">EMAIL VERIFICATION</span><h2>{verify.isPending ? 'Verifying your email…' : verify.isSuccess ? 'Email verified' : 'Verification failed'}</h2><p className="muted">{verify.isSuccess ? 'Your account is ready. You can sign in now.' : verify.isPending ? 'Just a moment.' : 'This link may be invalid or expired.'}</p></div>{verify.isError && <FormMessage message={getApiErrorMessage(verify.error, 'This verification link is invalid or expired.')} />}<Link className="button button-primary" to="/login">Continue to sign in</Link></div></div>;
}
