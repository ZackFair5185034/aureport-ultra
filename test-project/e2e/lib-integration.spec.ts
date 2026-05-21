import { test, expect } from '@playwright/test'

test('aureport-designer Web Component loads correctly', async ({ page }) => {
  const errors: string[] = []
  page.on('console', msg => {
    if (msg.type() === 'error') errors.push(msg.text())
  })
  page.on('pageerror', err => errors.push(err.message))

  await page.goto('http://localhost:5173/')

  // Wait for the Web Component to be defined
  await page.waitForFunction(() =>
    customElements.get('aureport-designer') !== undefined
  , { timeout: 10000 })

  // Verify aureport-designer element exists in DOM
  const designer = page.locator('aureport-designer')
  await expect(designer).toBeAttached()

  // Verify the inner container div was rendered (no Shadow DOM, plain container)
  const container = designer.locator('.aureport-designer-container')
  await expect(container).toBeAttached()

  // Log any errors found
  if (errors.length > 0) {
    console.log('Console errors:', errors)
  }
  expect(errors.length).toBe(0)
})
