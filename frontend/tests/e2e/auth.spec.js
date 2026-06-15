import { test, expect } from '@playwright/test';

test.describe('Authentication Flow', () => {
  test('should redirect to dashboard after successful login', async ({ page }) => {
    await page.goto('/login');

    await page.fill('input[name="username"]', 'testuser');
    await page.fill('input[name="password"]', 'password');
    await page.click('button[type="submit"]');

    await expect(page).toHaveURL('/dashboard');
    await expect(page.locator('body')).toContainText('Welcome');
  });

  test('should redirect to login when unauthorized', async ({ page }) => {
    // We assume the user is not logged in
    await page.goto('/dashboard');

    // The AuthGuard or Axios interceptor should redirect to /login
    await expect(page).toHaveURL('/login');
  });

  test('should successfully log out', async ({ page }) => {
    // Assuming we are already logged in for this test or we log in first
    await page.goto('/login');
    await page.fill('input[name="username"]', 'testuser');
    await page.fill('input[name="password"]', 'password');
    await page.click('button[type="submit"]');

    await expect(page).toHaveURL('/dashboard');

    // Find and click the logout button
    const logoutBtn = page.getByRole('button', { name: /logout/i });
    await logoutBtn.click();

    await expect(page).toHaveURL('/login');
  });
});
