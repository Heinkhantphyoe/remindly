import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { DashboardPage } from './DashboardPage';
import { reminderApi } from '../api/client';

vi.mock('../api/client', async () => {
  const actual = await vi.importActual<typeof import('../api/client')>('../api/client');
  return { ...actual, reminderApi: { list: vi.fn(), create: vi.fn(), update: vi.fn(), setCompleted: vi.fn(), delete: vi.fn() } };
});

describe('DashboardPage', () => {
  it('loads and displays reminders from the API query', async () => {
    vi.mocked(reminderApi.list).mockResolvedValue({ data: {
      content: [{ id: '1', title: 'Prepare demo', description: 'Bring slides', category: 'work', dueAt: '2026-10-20T10:00:00Z', advanceOffsetMinutes: null, completed: false, completedAt: null, createdAt: '2026-10-13T10:00:00Z', updatedAt: '2026-10-13T10:00:00Z' }],
      totalPages: 1, totalElements: 1, number: 0, size: 8,
    }, status: 200, statusText: 'OK', headers: {}, config: {} } as never);
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });

    render(<QueryClientProvider client={client}><DashboardPage /></QueryClientProvider>);

    await waitFor(() => expect(screen.getByText('Prepare demo')).toBeInTheDocument());
    expect(screen.getByText('Bring slides')).toBeInTheDocument();
  });
});
