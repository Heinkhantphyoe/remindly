const accessTokenKey = 'remindly.accessToken';
const refreshTokenKey = 'remindly.refreshToken';

export const tokenStore = {
  getAccessToken: () => localStorage.getItem(accessTokenKey),
  getRefreshToken: () => localStorage.getItem(refreshTokenKey),
  setTokens: (accessToken: string, refreshToken: string) => {
    localStorage.setItem(accessTokenKey, accessToken);
    localStorage.setItem(refreshTokenKey, refreshToken);
  },
  clear: () => {
    localStorage.removeItem(accessTokenKey);
    localStorage.removeItem(refreshTokenKey);
  },
};
