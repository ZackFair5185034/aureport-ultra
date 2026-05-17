/**
 * Created by Jacky.Gao on 2017-03-22.
 */
import { $t } from '@/locales'
import { getContext } from '@/utils/contextActions'

export function renderRowHeader(hot) {
  const countRows = hot.countRows()
  const headers = []
  const context = getContext()
  const rowHeaders = context.rowHeaders
  for (let i = 1; i <= countRows; i++) {
    let type = ''
    for (const header of rowHeaders) {
      if (header.rowNumber === (i - 1)) {
        switch (header.band) {
          case 'headerrepeat': {
            type = `<span style='color:blue;font-size: 10px' title='${$t('table.header.hr')}'>HR</span>`

            break
          }

          case 'footerrepeat': {
            type = `<span style='color:#d30a16;font-size: 10px' title='${$t('table.header.fr')}'>FR</span>`

            break
          }

          case 'title': {
            type = `<span style='color:#d30a16;font-size: 10px' title='${$t('table.header.t')}'>T</span>`

            break
          }

          case 'summary': {
            type = `<span style='color:#d30a16;font-size: 10px' title='${$t('table.header.s')}'>S</span>`

            break
          }
        // No default
        }

        break
      }
    }

    headers.push(i + type)
  }

  hot.updateSettings({
    rowHeaders: headers,
  })
}

;
