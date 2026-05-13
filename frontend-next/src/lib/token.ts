const LOGGED_IN_KEY = 'myos_is_logged_in';

export const isLoggedIn = (): boolean => {
  if (typeof window === 'undefined') return false;
  return localStorage.getItem(LOGGED_IN_KEY) === 'true';
};

export const setLoggedIn = (value: boolean): void => {
  if (typeof window !== 'undefined') {
    localStorage.setItem(LOGGED_IN_KEY, value.toString());
  }
};

export const clearLoginState = (): void => {
  if (typeof window !== 'undefined') {
    localStorage.removeItem(LOGGED_IN_KEY);
  }
};
