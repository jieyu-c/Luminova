package cn.jieyuc.user.infrastructure.persistence.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

/**
 * 把 Java 字符串绑定为 PostgreSQL {@code inet}。
 *
 * <p>驱动不会自动把 varchar 参数转换成 inet。无效地址在进入本类之前已被仓储丢弃，
 * 这里只负责类型转换，不判断 IP 是否合法。</p>
 */
public class PostgreSqlInetTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement statement, int index, String parameter, JdbcType jdbcType)
            throws SQLException {
        PGobject inet = new PGobject();
        inet.setType("inet");
        inet.setValue(parameter);
        statement.setObject(index, inet);
    }

    @Override
    public String getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        return read(resultSet.getObject(columnName));
    }

    @Override
    public String getNullableResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return read(resultSet.getObject(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement statement, int columnIndex) throws SQLException {
        return read(statement.getObject(columnIndex));
    }

    private String read(Object value) {
        if (value == null) {
            return null;
        }
        return value instanceof PGobject pgObject ? pgObject.getValue() : value.toString();
    }
}
