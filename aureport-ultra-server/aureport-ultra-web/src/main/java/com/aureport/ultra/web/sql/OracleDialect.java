package com.aureport.ultra.web.sql;

import com.aureport.ultra.web.sql.enums.DbType;

/**
 * oracle 数据库 分页语句组装
 * Since: 2016-01-23
 * @author hubin
 */
public class OracleDialect implements IPageDialect {
    public OracleDialect() {
    }

    /**
     * 获取数据库类型
     *
     * @return
     */
    @Override
    public DbType getDbType() {
        return DbType.ORACLE;
    }

    public String buildPaginationSql(String originalSql, long offset, long limit) {
        long endRow = offset + limit;
        return "SELECT * FROM ( SELECT TMP.*, ROWNUM ROW_ID FROM ( " + originalSql + " ) TMP WHERE ROWNUM <= " + endRow + ") WHERE ROW_ID > " + offset;
    }
}
