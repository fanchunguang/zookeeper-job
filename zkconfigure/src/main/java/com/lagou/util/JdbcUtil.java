package com.lagou.util;

import com.lagou.ZkconfigureApplication;

import java.sql.*;

/**
 * JDBC连接
 */
public class JdbcUtil {

    public static Connection getConnection() throws SQLException {
        Connection conn= ZkconfigureApplication.dataSource.getConnection();
        return conn;
    }

    /**
     * 关闭连接
     */
    public static void closeConnection(Connection conn){
        try {
            if(conn!=null) {
                conn.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
