import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { reminderApi, getApiErrorMessage } from '../api/client';
import type { Reminder, ReminderInput } from '../api/types';
import { FormMessage } from '../components/FormMessage';

type Filter = 'all' | 'open' | 'completed';
type FormState = { title: string; description: string; category: string; dueAt: string; advanceOffsetMinutes: string };
const emptyForm: FormState = { title: '', description: '', category: '', dueAt: '', advanceOffsetMinutes: '' };

function localInputFromIso(value: string) {
  const date = new Date(value);
  return new Date(date.getTime() - date.getTimezoneOffset() * 60_000).toISOString().slice(0, 16);
}
function isoFromLocalInput(value: string) { return new Date(value).toISOString(); }
function formatDueAt(value: string) { return new Intl.DateTimeFormat(undefined, { weekday: 'short', month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit' }).format(new Date(value)); }
function isOverdue(reminder: Reminder) { return !reminder.completed && new Date(reminder.dueAt).getTime() < Date.now(); }
function formFromReminder(reminder: Reminder): FormState { return { title: reminder.title, description: reminder.description ?? '', category: reminder.category ?? '', dueAt: localInputFromIso(reminder.dueAt), advanceOffsetMinutes: reminder.advanceOffsetMinutes?.toString() ?? '' }; }
function toPayload(form: FormState): ReminderInput { return { title: form.title.trim(), description: form.description.trim() || undefined, category: form.category.trim() || undefined, dueAt: isoFromLocalInput(form.dueAt), advanceOffsetMinutes: form.advanceOffsetMinutes ? Number(form.advanceOffsetMinutes) : undefined }; }
function startOfDayIso(value: string) { return value ? new Date(`${value}T00:00:00`).toISOString() : undefined; }
function endOfDayIso(value: string) { return value ? new Date(`${value}T23:59:59.999`).toISOString() : undefined; }

export function DashboardPage() {
  const queryClient = useQueryClient();
  const [filter, setFilter] = useState<Filter>('open');
  const [category, setCategory] = useState('');
  const [dateFrom, setDateFrom] = useState('');
  const [dateTo, setDateTo] = useState('');
  const [page, setPage] = useState(0);
  const [editor, setEditor] = useState<Reminder | 'new' | null>(null);
  const completed = filter === 'all' ? undefined : filter === 'completed';
  const reminders = useQuery({
    queryKey: ['reminders', { page, filter, category, dateFrom, dateTo }],
    queryFn: () => reminderApi.list({ page, size: 8, completed, category: category || undefined, dueFrom: startOfDayIso(dateFrom), dueTo: endOfDayIso(dateTo) }).then((response) => response.data),
  });
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['reminders'] });
  const complete = useMutation({ mutationFn: ({ id, value }: { id: string; value: boolean }) => reminderApi.setCompleted(id, value), onSuccess: invalidate });
  const remove = useMutation({ mutationFn: (id: string) => reminderApi.delete(id), onSuccess: invalidate });
  const save = useMutation({ mutationFn: ({ id, input }: { id?: string; input: ReminderInput }) => id ? reminderApi.update(id, input) : reminderApi.create(input), onSuccess: () => { setEditor(null); invalidate(); } });
  const visibleReminders = reminders.data?.content ?? [];
  const categories = useMemo(() => [...new Set(visibleReminders.map((item) => item.category).filter(Boolean) as string[])].sort(), [visibleReminders]);
  function changeFilter(next: Filter) { setFilter(next); setPage(0); }

  return <section className="dashboard">
    <div className="dashboard-heading"><div><span className="eyebrow">YOUR SPACE</span><h1>Good things are<br /><em>worth remembering.</em></h1></div><button className="button button-primary add-button" onClick={() => setEditor('new')}>+ Add reminder</button></div>
    <div className="dashboard-toolbar"><div className="filter-tabs">{(['open', 'all', 'completed'] as Filter[]).map((item) => <button key={item} className={filter === item ? 'active' : ''} onClick={() => changeFilter(item)}>{item === 'open' ? 'Upcoming' : item[0].toUpperCase() + item.slice(1)}</button>)}</div><div className="filter-fields"><select value={category} onChange={(event) => { setCategory(event.target.value); setPage(0); }}><option value="">All categories</option>{categories.map((item) => <option key={item} value={item}>{item}</option>)}</select><input type="date" aria-label="Due from" value={dateFrom} onChange={(event) => { setDateFrom(event.target.value); setPage(0); }} /><input type="date" aria-label="Due to" value={dateTo} onChange={(event) => { setDateTo(event.target.value); setPage(0); }} /></div></div>
    {(reminders.isError || save.isError || complete.isError || remove.isError) && <FormMessage message={getApiErrorMessage(reminders.error ?? save.error ?? complete.error ?? remove.error)} />}
    {reminders.isPending ? <div className="page-state compact">Loading reminders…</div> : visibleReminders.length === 0 ? <div className="empty-card dashboard-empty"><span className="empty-icon">✦</span><strong>{filter === 'completed' ? 'Nothing completed yet.' : 'Your space is clear.'}</strong><span>Add a reminder when something important comes up.</span><button className="button button-primary" onClick={() => setEditor('new')}>Add your first reminder</button></div> : <div className="reminder-list">{visibleReminders.map((reminder) => <ReminderCard key={reminder.id} reminder={reminder} onComplete={(value) => complete.mutate({ id: reminder.id, value })} onEdit={() => setEditor(reminder)} onDelete={() => { if (window.confirm('Delete this reminder?')) remove.mutate(reminder.id); }} />)}</div>}
    {(reminders.data?.totalPages ?? 0) > 1 && <div className="pagination"><button className="button button-quiet" disabled={page === 0} onClick={() => setPage((current) => current - 1)}>← Previous</button><span>Page {page + 1} of {reminders.data?.totalPages}</span><button className="button button-quiet" disabled={page + 1 >= (reminders.data?.totalPages ?? 0)} onClick={() => setPage((current) => current + 1)}>Next →</button></div>}
    {editor && <ReminderEditor value={editor === 'new' ? null : editor} pending={save.isPending} onClose={() => setEditor(null)} onSave={(input) => save.mutate({ id: editor === 'new' ? undefined : editor.id, input })} />}
  </section>;
}

function ReminderCard({ reminder, onComplete, onEdit, onDelete }: { reminder: Reminder; onComplete: (value: boolean) => void; onEdit: () => void; onDelete: () => void }) {
  return <article className={`reminder-card ${reminder.completed ? 'is-completed' : ''}`}><button className={`completion-toggle ${reminder.completed ? 'checked' : ''}`} aria-label={reminder.completed ? 'Reopen reminder' : 'Complete reminder'} onClick={() => onComplete(!reminder.completed)}>{reminder.completed ? '✓' : ''}</button><div className="reminder-body"><div className="reminder-meta"><span className={isOverdue(reminder) ? 'due overdue' : 'due'}>{reminder.completed ? `Completed ${formatDueAt(reminder.completedAt ?? reminder.dueAt)}` : formatDueAt(reminder.dueAt)}</span>{reminder.category && <span className="category-chip">{reminder.category}</span>}</div><h3>{reminder.title}</h3>{reminder.description && <p>{reminder.description}</p>}</div><div className="reminder-actions"><button onClick={onEdit} aria-label="Edit reminder">Edit</button><button onClick={onDelete} aria-label="Delete reminder">Delete</button></div></article>;
}

function ReminderEditor({ value, pending, onClose, onSave }: { value: Reminder | null; pending: boolean; onClose: () => void; onSave: (input: ReminderInput) => void }) {
  const [form, setForm] = useState<FormState>(value ? formFromReminder(value) : emptyForm);
  function update(field: keyof FormState, next: string) { setForm((current) => ({ ...current, [field]: next })); }
  function submit(event: React.FormEvent<HTMLFormElement>) { event.preventDefault(); onSave(toPayload(form)); }
  return <div className="modal-backdrop" role="presentation" onMouseDown={(event) => { if (event.currentTarget === event.target) onClose(); }}><form className="modal-card" onSubmit={submit}><div className="modal-heading"><div><span className="eyebrow">{value ? 'EDIT REMINDER' : 'NEW REMINDER'}</span><h2>{value ? 'Make it just right.' : 'What needs remembering?'}</h2></div><button type="button" className="close-button" onClick={onClose}>×</button></div><label>Title<input autoFocus value={form.title} onChange={(event) => update('title', event.target.value)} required maxLength={200} /></label><label>Description<textarea value={form.description} onChange={(event) => update('description', event.target.value)} rows={3} maxLength={10000} /></label><div className="form-grid"><label>Due date and time<input type="datetime-local" value={form.dueAt} onChange={(event) => update('dueAt', event.target.value)} required /></label><label>Category<input value={form.category} onChange={(event) => update('category', event.target.value)} maxLength={80} placeholder="Work, personal…" /></label></div><label>Advance notice <span className="label-hint">minutes before due time</span><input type="number" min="1" value={form.advanceOffsetMinutes} onChange={(event) => update('advanceOffsetMinutes', event.target.value)} placeholder="Optional" /></label><div className="modal-actions"><button type="button" className="button button-quiet" onClick={onClose}>Cancel</button><button className="button button-primary" disabled={pending}>{pending ? 'Saving…' : value ? 'Save changes' : 'Create reminder'}</button></div></form></div>;
}
