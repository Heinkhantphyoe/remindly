import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import './styles.css';

const queryClient = new QueryClient();
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '/api';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <main className="app-shell">
          <h1>Remindly</h1>
          <p>Reminder management will be added in the next implementation steps.</p>
          <small>API base URL: {apiBaseUrl}</small>
        </main>
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
);

