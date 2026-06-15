# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: auth.spec.js >> Authentication Flow >> should successfully log out
- Location: tests\e2e\auth.spec.js:23:3

# Error details

```
Error: expect(page).toHaveURL(expected) failed

Expected: "http://localhost:5178/dashboard"
Received: "http://localhost:5178/login"
Timeout:  5000ms

Call log:
  - Expect "toHaveURL" with timeout 5000ms
    14 × unexpected value "http://localhost:5178/login"

```

```yaml
- heading "Login to Support System" [level=1]
- text: Username
- textbox "admin"
- text: Password
- textbox "admin123"
- button "Sign In"
```

# Test source

```ts
  1  | import { test, expect } from '@playwright/test';
  2  | 
  3  | test.describe('Authentication Flow', () => {
  4  |   test('should redirect to dashboard after successful login', async ({ page }) => {
  5  |     await page.goto('/login');
  6  | 
  7  |     await page.fill('input[name="username"]', 'testuser');
  8  |     await page.fill('input[name="password"]', 'password');
  9  |     await page.click('button[type="submit"]');
  10 | 
  11 |     await expect(page).toHaveURL('/dashboard');
  12 |     await expect(page.locator('body')).toContainText('Welcome');
  13 |   });
  14 | 
  15 |   test('should redirect to login when unauthorized', async ({ page }) => {
  16 |     // We assume the user is not logged in
  17 |     await page.goto('/dashboard');
  18 | 
  19 |     // The AuthGuard or Axios interceptor should redirect to /login
  20 |     await expect(page).toHaveURL('/login');
  21 |   });
  22 | 
  23 |   test('should successfully log out', async ({ page }) => {
  24 |     // Assuming we are already logged in for this test or we log in first
  25 |     await page.goto('/login');
  26 |     await page.fill('input[name="username"]', 'testuser');
  27 |     await page.fill('input[name="password"]', 'password');
  28 |     await page.click('button[type="submit"]');
  29 | 
> 30 |     await expect(page).toHaveURL('/dashboard');
     |                        ^ Error: expect(page).toHaveURL(expected) failed
  31 | 
  32 |     // Find and click the logout button
  33 |     const logoutBtn = page.getByRole('button', { name: /logout/i });
  34 |     await logoutBtn.click();
  35 | 
  36 |     await expect(page).toHaveURL('/login');
  37 |   });
  38 | });
  39 | 
```