import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Link, useNavigate } from 'react-router-dom';
import { authApi, getApiErrorMessage } from '../api/client';
import { FormMessage } from '../components/FormMessage';

export function RegisterPage() {
  const [form, setForm] = useState({ email: '', password: '', displayName: '', timezone: Intl.DateTimeFormat().resolvedOptions().timeZone || 'UTC' });
  const navigate = useNavigate();
  const register = useMutation({ mutationFn: () => authApi.register(form), onSuccess: () => navigate('/login', { state: { registered: true } }) });
  function update(field: keyof typeof form, value: string) { setForm((current) => ({ ...current, [field]: value })); }
  function submit(event: React.FormEvent<HTMLFormElement>) { event.preventDefault(); register.mutate(); }
  return <div className="auth-page auth-page-register"><form className="auth-card" onSubmit={submit}><div><span className="eyebrow">GET STARTED</span><h2>Create your account</h2><p className="muted">A calmer way to remember what matters.</p></div>{register.isError && <FormMessage message={getApiErrorMessage(register.error)} />}<label>Name<input value={form.displayName} onChange={(event) => update('displayName', event.target.value)} required maxLength={120} autoComplete="name" /></label><label>Email<input type="email" value={form.email} onChange={(event) => update('email', event.target.value)} required autoComplete="email" /></label><label>Password<input type="password" value={form.password} onChange={(event) => update('password', event.target.value)} required minLength={8} autoComplete="new-password" /></label><label>Timezone<input value={form.timezone} onChange={(event) => update('timezone', event.target.value)} required /></label><button className="button button-primary" disabled={register.isPending}>{register.isPending ? 'Creating account…' : 'Create account'}</button><p className="form-footer">Already have an account? <Link to="/login">Sign in</Link></p></form></div>;
}
