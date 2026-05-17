export function setNodeChecked(node: Record<string, unknown>, checked: boolean) {
  if (!node.disabled) {
    node.checked = checked
    node.indeterminate = false
  }

  if (node.children && (node.children as unknown[]).length > 0) {
    ;for (const cell of (node.children as Record<string, unknown>[])) {
      setNodeChecked(cell, checked)
    }
  }
}
