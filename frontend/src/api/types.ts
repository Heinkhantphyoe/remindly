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
