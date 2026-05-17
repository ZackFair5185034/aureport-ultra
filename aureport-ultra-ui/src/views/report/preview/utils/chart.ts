import { Chart } from 'chart.js'
import { storeChartData } from '@/api/preview'

export function convertChartConfig(chartJson: Record<string, any>): Record<string, any> {
  if (!chartJson || !chartJson.options) {
    return chartJson
  }

  const options = chartJson.options

  if (options.scales) {
    if (options.scales.xAxes && options.scales.xAxes.length > 0) {
      options.scales.x = options.scales.xAxes[0]
      delete options.scales.xAxes
    }

    if (options.scales.yAxes && options.scales.yAxes.length > 0) {
      options.scales.y = options.scales.yAxes[0]
      delete options.scales.yAxes
    }
  }

  if (options.title) {
    options.plugins = options.plugins || {}
    options.plugins.title = options.title
    delete options.title
  }

  if (options.legend) {
    options.plugins = options.plugins || {}
    options.plugins.legend = options.legend
    delete options.legend
  }

  if (chartJson.type === 'horizontalBar') {
    chartJson.type = 'bar'
    options.indexAxis = 'y'
  }

  return chartJson
}

export function buildChartDatas(chartData: Array<{ id: string, json: string }>): void {
  if (!chartData)
    return
  for (const d of chartData) {
    let json: any = d.json
    if (json) {
      json = JSON.parse(json, (_k: string, v: any) => {
        if (v && typeof v === 'string' && v.includes('function')) {
          return eval(`(function(){return ${v} })()`)
        }

        return v
      }) as Record<string, any>
    }

    buildChart(d.id, json)
  }
}

export async function buildChart(canvasId: string, chartJson: Record<string, any>): Promise<Chart | undefined> {
  const ctx = document.getElementById(canvasId) as HTMLCanvasElement | null
  if (!ctx)
    return

  chartJson = convertChartConfig(chartJson)

  const options = chartJson.options || {}
  chartJson.options = options
  const animation = options.animation || {}
  options.animation = animation

  animation.onComplete = async (context: { chart: Chart }) => {
    try {
      const chart = context.chart
      const base64Image = chart.toBase64Image()
      const urlParameters = window.location.search
      const canvas = document.getElementById(canvasId) as HTMLCanvasElement | null
      if (!canvas)
        return

      const width = parseInt(canvas.style.width) || canvas.width
      const height = parseInt(canvas.style.height) || canvas.height

      const formData = new FormData()
      formData.append('_base64Data', base64Image)
      formData.append('_chartId', canvasId)
      formData.append('_width', String(width))
      formData.append('_height', String(height))

      const params = new URLSearchParams(urlParameters.slice(1))
      for (const [key, value] of params.entries()) {
        formData.append(key, value)
      }

      await storeChartData(formData)
    }
    catch (error) {
      console.error('存储图表数据失败:', error)
    }
  }

  return new Chart(ctx, chartJson as any)
}
