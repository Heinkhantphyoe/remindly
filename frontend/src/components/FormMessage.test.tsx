import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { FormMessage } from './FormMessage';

describe('FormMessage', () => {
  it('renders an accessible error message', () => {
    render(<FormMessage message="Email is invalid" />);
    expect(screen.getByRole('alert')).toHaveTextContent('Email is invalid');
  });
});
