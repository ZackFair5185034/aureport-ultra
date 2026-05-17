const styles = {}

function addCss(cssList, el) {
  const css = styles[el.tag]
  css && !cssList.includes(css) && cssList.push(css)
  if (el.children && Array.isArray(el.children)) {
    for (const el2 of el.children) addCss(cssList, el2)
  }
}

export function makeUpCss(conf) {
  const cssList = []
  if (conf.fields && Array.isArray(conf.fields)) {
    for (const el of conf.fields) addCss(cssList, el)
  }

  return cssList.join('\n')
}
