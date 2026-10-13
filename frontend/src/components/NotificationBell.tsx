import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { getApiErrorMessage, notificationApi } from '../api/client';

function formatNotificationDate(value: string) {
  return new Intl.DateTimeFormat(undefined, { month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit' }).format(new Date(value));
}

export function NotificationBell() {
  const [open, setOpen] = useState(false);
  const queryClient = useQueryClient();
  const count = useQuery({ queryKey: ['notifications', 'unread-count'], queryFn: () => notificationApi.unreadCount().then((response) => response.data), refetchInterval: 30_000 });
  const history = useQuery({ queryKey: ['notifications', 'history'], queryFn: () => notificationApi.list().then((response) => response.data), enabled: open, refetchInterval: open ? 30_000 : false });
  const refresh = () => { queryClient.invalidateQueries({ queryKey: ['notifications'] }); };
  const markRead = useMutation({ mutationFn: (id: string) => notificationApi.markRead(id), onSuccess: refresh });
  const markAllRead = useMutation({ mutationFn: () => notificationApi.markAllRead(), onSuccess: refresh });

  return <div className="notification-wrap"><button className="notification-button" aria-label="Notifications" onClick={() => setOpen((current) => !current)}><span className="bell-icon">♢</span>{(count.data?.count ?? 0) > 0 && <span className="unread-badge">{count.data?.count}</span>}</button>{open && <div className="notification-panel"><div className="notification-heading"><div><span className="eyebrow">INBOX</span><h3>Notifications</h3></div>{(count.data?.count ?? 0) > 0 && <button onClick={() => markAllRead.mutate()}>Mark all read</button>}</div>{history.isPending ? <p className="notification-state">Loading…</p> : history.isError ? <p className="notification-state error">{getApiErrorMessage(history.error)}</p> : history.data?.content.length === 0 ? <p className="notification-state">You’re all caught up.</p> : <div className="notification-list">{history.data?.content.map((notification) => <button key={notification.id} className={`notification-item ${notification.readAt ? 'read' : ''}`} onClick={() => { if (!notification.readAt) markRead.mutate(notification.id); }}><span className={`notification-dot ${notification.readAt ? 'read' : ''}`} /><span><strong>{notification.notificationType === 'DUE' ? 'Reminder due' : 'Upcoming reminder'}</strong><small>{formatNotificationDate(notification.createdAt)}</small></span></button>)}</div>}</div>}</div>;
}
