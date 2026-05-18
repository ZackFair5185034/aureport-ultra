// ====== 报表核心类型定义 ======

/** 报表上下文 */
export interface ReportContext {
  cellsMap: Map<string, CellDef>
  rowHeaders: RowHeader[]
  LETTERS: string[]
  reportDef: ReportDef
}

/** 单元格定义 */
export interface CellDef {
  rowNumber: number
  columnNumber: number
  expand: ExpandType | string
  cellStyle: CellStyle
  value: CellValue
  leftParentCellName?: string
  topParentCellName?: string
  fillBlankRows?: boolean
  multiple?: number
  linkUrl?: string
  linkTargetWindow?: string
  linkParameters?: LinkParameter[]
  tooltip?: string
  conditionPropertyItems?: ConditionPropertyItem[]
  crossTabWidget?: unknown
}

/** 单元格值 */
export interface CellValue {
  type: 'simple' | 'expression' | 'dataset' | 'image' | 'slash' | 'zxing' | 'chart' | 'richtext'
  value?: string
  // dataset
  datasetName?: string
  property?: string
  aggregate?: AggregateType | string
  order?: SortType | string
  groupHead?: boolean
  groupFoot?: boolean
  mappingType?: string
  mappingDataset?: string
  mappingKeyProperty?: string
  mappingValueProperty?: string
  nestProperty?: string
  conditions?: Condition[]
  groupItems?: GroupItem[]
  mappingItems?: MappingItem[]
  // image
  source?: string
  width?: number
  height?: number
  // slash
  slashes?: SlashItem[]
  base64Data?: string
  // zxing
  category?: string
  format?: string
  // chart
  chart?: ChartConfig
  // zxing
  data?: string
  // dataset value (array type for select)
  values?: string[]
}

/** 单元格样式 */
export interface CellStyle {
  fontSize?: number
  fontSizeScope?: string
  forecolor?: string
  forecolorScope?: string
  fontFamily?: string
  fontFamilyScope?: string
  bgcolor?: string
  bgcolorScope?: string
  format?: string
  bold?: boolean
  boldScope?: string
  italic?: boolean
  italicScope?: string
  underline?: boolean
  underlineScope?: string
  wrapCompute?: boolean
  align?: string
  alignScope?: string
  valign?: string
  valignScope?: string
  lineHeight?: number
  leftBorder?: BorderDef
  rightBorder?: BorderDef
  topBorder?: BorderDef
  bottomBorder?: BorderDef
  renderer?: string
}

/** 边框定义 */
export type BorderDef = {
  width: number
  style: string
  color: string
} | string

/** 行头 */
export interface RowHeader {
  rowNumber: number
  band: BandType
}

/** 报表定义 */
export interface ReportDef {
  header?: HeaderFooter
  footer?: HeaderFooter
  datasources: Datasource[]
  paper: Paper
  searchForm?: Record<string, unknown>
}

/** 页眉页脚 */
export interface HeaderFooter {
  left?: string
  center?: string
  right?: string
  fontFamily?: string
  fontSize?: number
  forecolor?: string
  bold?: boolean
  italic?: boolean
  underline?: boolean
  margin?: number
}

/** 数据源 */
export interface Datasource {
  name: string
  type: 'jdbc' | 'spring' | 'buildin'
  username?: string
  password?: string
  url?: string
  driver?: string
  beanId?: string
  datasets: Dataset[]
}

/** 数据集 */
export interface Dataset {
  name: string
  type: 'sql' | 'bean'
  sql?: string
  method?: string
  clazz?: string
  fields: Field[]
  parameters: Parameter[]
}

/** 字段 */
export interface Field {
  name: string
}

/** 参数 */
export interface Parameter {
  name: string
  type: string
  defaultValue: string
}

/** 纸张设置 */
export interface Paper {
  paperType: string
  leftMargin: number
  rightMargin: number
  topMargin: number
  bottomMargin: number
  pagingMode: string
  fixRows: number
  width: number
  height: number
  orientation: string
  htmlReportAlign: string
  bgImage?: string
  htmlIntervalRefreshValue: number
  columnEnabled: boolean
  columnCount?: number
  columnMargin?: number
}

/** 条件 */
export interface Condition {
  property?: string
  operation: string
  id: string
  join?: string
  type?: string
  left?: string
  right: string
}

/** 分组项 */
export interface GroupItem {
  name: string
  conditions: Condition[]
}

/** 映射项 */
export interface MappingItem {
  value: string
  label: string
}

/** 链接参数 */
export interface LinkParameter {
  name: string
  value: string
}

/** 条件属性项 */
export interface ConditionPropertyItem {
  name: string
  rowHeight?: number
  colWidth?: number
  newValue?: string
  linkUrl?: string
  linkTargetWindow?: string
  paging?: { position: string, line: number }
  linkParameters?: LinkParameter[]
  cellStyle?: CellStyle
  conditions: Condition[]
}

/** 斜线项 */
export interface SlashItem {
  text: string
  x: number
  y: number
  degree: number
}

/** 图表配置 */
export interface ChartConfig {
  dataset: ChartDataset
  xaxes?: ChartAxis
  yaxes?: ChartAxis
  options?: ChartOption[]
  plugins?: ChartPlugin[]
}

/** 图表数据集 */
export interface ChartDataset {
  datasetName?: string
  type?: string
  categoryProperty?: string
  seriesProperty?: string
  seriesType?: string
  seriesText?: string
  valueProperty?: string
  rProperty?: string
  xProperty?: string
  yProperty?: string
  collectType?: string
  format?: string
}

/** 图表轴 */
export interface ChartAxis {
  rotation?: number
  scaleLabel?: { display?: string | boolean, labelString?: string }
  ticks?: { beginAtZero?: boolean, stepSize?: number, suggestedMax?: number, max?: number }
}

/** 图表选项 */
export interface ChartOption {
  type: string
  position?: string
  display?: string | boolean
  duration?: number
  easing?: string
  text?: string
  labels?: string[]
  padding?: { top?: number, right?: number, bottom?: number, left?: number }
  layout?: { top?: number, bottom?: number, left?: number, right?: number }
}

/** 图表插件 */
export interface ChartPlugin {
  name: string
  display: string | boolean
}

// ====== 枚举/联合类型 ======

export type ExpandType = 'None' | 'Down' | 'Right'
export type SortType = 'none' | 'asc' | 'desc'
export type AggregateType = 'select' | 'group' | 'customgroup' | 'sum' | 'count' | 'max' | 'min' | 'avg'
export type BandType = 'header' | 'footer' | 'detail' | 'title' | 'summary'

// ====== API 类型 ======

export interface ProviderItem {
  name: string
  prefix: string
  disabled: boolean
}

export interface ReportFile {
  name: string
  fullName: string
  modificationDate: string
}

export interface DatasourceItem {
  name: string
  type: string
  driver?: string
  url?: string
  username?: string
  password?: string
  beanId?: string
  datasets: Dataset[]
}
