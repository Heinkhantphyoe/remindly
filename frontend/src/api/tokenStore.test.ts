import { beforeEach, describe, expect, it } from 'vitest';
import { tokenStore } from './tokenStore';

describe('tokenStore', () => {
  beforeEach(() => localStorage.clear());

  it('stores and retrieves both session tokens', () => {
    tokenStore.setTokens('access-token', 'refresh-token');
    expect(tokenStore.getAccessToken()).toBe('access-token');
    expect(tokenStore.getRefreshToken()).toBe('refresh-token');
  });

  it('clears the session tokens', () => {
    tokenStore.setTokens('access-token', 'refresh-token');
    tokenStore.clear();
    expect(tokenStore.getAccessToken()).toBeNull();
    expect(tokenStore.getRefreshToken()).toBeNull();
  });
});
