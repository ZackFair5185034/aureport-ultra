// In Vite, public path is configured via base in vite.config.ts
// Dynamic public path can be set via window.__aureport_public_path__ before the app loads
// This is handled through Vite's base configuration mechanism
if (typeof window !== 'undefined' && (window as any).__aureport_public_path__) {
  const link = document.createElement('link')
  link.rel = 'base'
  link.href = (window as any).__aureport_public_path__ as string
  document.head.appendChild(link)
}
