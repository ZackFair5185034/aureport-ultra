export function setNodeChecked(node: Record<string, unknown>, checked: boolean) {
  if (!node.disabled) {
    node.checked = checked
    node.indeterminate = false
  }
  if (node.children && (node.children as unknown[]).length > 0) {
    ;(node.children as Record<string, unknown>[]).forEach(cell => {
      setNodeChecked(cell, checked)
    })
  }
}
