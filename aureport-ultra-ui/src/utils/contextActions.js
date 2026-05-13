/**
 * Context 操作方法集合
 *
 * 架构说明：
 * - 所有操作方法都通过 Pinia store 调用
 * - 保持了对 context 数据的集中管理
 *
 * 使用示例：
 * import { addCell, removeCell } from '@/utils/contextActions.js';
 *
 * // 在组件或函数中调用
 * addCell(cell);
 * removeCell(cell);
 */

import { useReportStore } from '@/stores/report';

/**
 * 获取 context
 */
export function getContext() {
  const store = useReportStore();
  return store.context;
}

/**
 * 添加单元格
 * @param {Object} cell - 单元格定义
 */
export function addCell(cell) {
  const store = useReportStore();
  store.addCell(cell);
}

/**
 * 移除单元格
 * @param {Object} cell - 单元格
 */
export function removeCell(cell) {
  const store = useReportStore();
  store.removeCell(cell);
}

/**
 * 设置单元格（按行列号）
 * @param {number} rowIndex - 行索引（从 0 开始）
 * @param {number} colIndex - 列索引（从 0 开始）
 * @param {Object} cell - 单元格定义
 */
export function setCell(rowIndex, colIndex, cell) {
  const store = useReportStore();
  rowIndex++;
  colIndex++;
  store.setCell(rowIndex, colIndex, cell);
}

/**
 * 删除单元格（按行列号）
 * @param {number} rowNumber - 行号
 * @param {number} columnNumber - 列号
 */
export function deleteCell(rowNumber, columnNumber) {
  const store = useReportStore();
  store.deleteCell(rowNumber, columnNumber);
}

/**
 * 获取单元格
 * @param {number} rowIndex - 行索引（从 0 开始）
 * @param {number} colIndex - 列索引（从 0 开始）
 * @returns {Object|null}
 */
export function getCell(rowIndex, colIndex) {
  const store = useReportStore();
  return store.getCell(rowIndex, colIndex);
}

/**
 * 获取 cellsMap
 * @returns {Map|null}
 */
export function getCellsMap() {
  const store = useReportStore();
  return store.getCellsMap();
}

/**
 * 添加行头
 * @param {number} row - 行号
 * @param {string} band - 带类型
 */
export function addRowHeader(row, band) {
  const store = useReportStore();
  store.addRowHeader(row, band);
}

/**
 * 调整插入行头
 * @param {number} row - 行号
 */
export function adjustInsertRowHeaders(row) {
  const store = useReportStore();
  store.adjustInsertRowHeaders(row);
}

/**
 * 调整删除行头
 * @param {number} row - 行号
 */
export function adjustDelRowHeaders(row) {
  const store = useReportStore();
  store.adjustDelRowHeaders(row);
}

/**
 * 获取单元格名称
 * @param {number|null} rowIndex - 行索引（从 0 开始，可为 null）
 * @param {number} colIndex - 列索引（从 0 开始）
 * @returns {string}
 */
export function getCellName(rowIndex, colIndex) {
  const store = useReportStore();
  return store.getCellName(rowIndex, colIndex);
}

/**
 * 设置 context（仅在初始化时使用）
 * @param {Object} ctx - Context 实例
 */
export function setContext(ctx) {
  const store = useReportStore();
  store.setContext(ctx);
}

/**
 * 更新 context.reportDef
 * @param {Object} reportDef - 报表定义对象
 */
export function updateReportDef(reportDef) {
  const store = useReportStore();
  store.updateReportDef(reportDef);
}

/**
 * 更新 context 的任意属性
 * @param {string} property - 属性名
 * @param {any} value - 属性值
 */
export function updateProperty(property, value) {
  const store = useReportStore();
  store.updateProperty(property, value);
}

// 默认导出所有方法
export default {
  getContext,
  addCell,
  removeCell,
  setCell,
  deleteCell,
  getCell,
  getCellsMap,
  addRowHeader,
  adjustInsertRowHeaders,
  adjustDelRowHeaders,
  getCellName,
  setContext,
  updateReportDef,
  updateProperty
};
