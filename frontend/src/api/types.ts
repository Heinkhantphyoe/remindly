export type User = {
  id: string;
  email: string;
  displayName: string;
  timezone: string;
  createdAt: string;
};

export type LoginResponse = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: User;
};

export type RegisterRequest = {
  email: string;
  password: string;
  displayName: string;
  timezone: string;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type Reminder = {
  id: string;
  title: string;
  description: string | null;
  category: string | null;
  dueAt: string;
  advanceOffsetMinutes: number | null;
  completed: boolean;
  completedAt: string | null;
  createdAt: string;
  updatedAt: string;
};

export type ReminderInput = {
  title: string;
  description?: string;
  category?: string;
  dueAt: string;
  advanceOffsetMinutes?: number;
};

export type ReminderPage = {
  content: Reminder[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
};
