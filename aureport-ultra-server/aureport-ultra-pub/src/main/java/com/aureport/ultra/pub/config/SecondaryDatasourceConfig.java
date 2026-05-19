package com.aureport.ultra.pub.config;

import com.aureport.ultra.core.definition.datasource.BuildinDatasource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 内建数据源示例
 *
 * 内建数据源由服务端注册，前端选择使用，无需输入连接参数。
 * 实现 BuildinDatasource 接口的 Spring Bean 会自动注册，
 * 前端"添加内置数据源"时会从 /datasource/loadBuildinDatasources 接口列出。
 */
@Configuration
public class SecondaryDatasourceConfig {

    private static final Logger log = LoggerFactory.getLogger(SecondaryDatasourceConfig.class);

    /**
     * 示例一：复用主库 DataSource（最简单方式）
     *
     * 在前端选择内置数据源时显示为 "reportArchiveDatasource"，
     * 使用与主数据源相同的数据库。只需注入 DataSource 并注册为 BuildinDatasource。
     *
     * 注意：如果有多个 @Bean 返回 BuildinDatasource，前端会列出所有。
     *      如果 name() 相同，后面的会覆盖前面的。
     */
    @Bean
    public BuildinDatasource reportArchiveDatasource(DataSource primaryDataSource) {
        return new BuildinDatasource() {
            @Override
            public String name() {
                return "reportArchiveDatasource";
            }

            @Override
            public Connection getConnection() {
                try {
                    return primaryDataSource.getConnection();
                } catch (SQLException e) {
                    log.error("报表归档数据源获取连接失败", e);
                    return null;
                }
            }
        };
    }

    /**
     * 示例二：独立连接池（另一个 MySQL 库）
     *
     * 连接其他数据库，使用独立的 HikariCP 连接池。
     * HikariDataSource 创建时不立即校验连接，延迟到首次 getConnection()，
     * 因此即使数据库不可用也不会影响应用启动。
     *
     * 前端添加内置数据源时选择对应的 name 即可。
     */
    @Bean
    public BuildinDatasource erpDatasource() {
        HikariDataSource dataSource = createDataSource(
            "jdbc:mysql://192.168.101.188:3306/erp?serverTimezone=Asia/Shanghai",
            "erp_user",
            "erp_password",
            "erp-pool"
        );

        return new BuildinDatasource() {
            @Override
            public String name() {
                return "erpDatasource";
            }

            @Override
            public Connection getConnection() {
                try {
                    return dataSource.getConnection();
                } catch (SQLException e) {
                    log.error("ERP 数据源获取连接失败", e);
                    return null;
                }
            }
        };
    }

    private HikariDataSource createDataSource(String url, String username, String password, String poolName) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setPoolName(poolName);
        // 不校验连接池可用性，延迟到首次使用
        config.setInitializationFailTimeout(-1);
        return new HikariDataSource(config);
    }
}
