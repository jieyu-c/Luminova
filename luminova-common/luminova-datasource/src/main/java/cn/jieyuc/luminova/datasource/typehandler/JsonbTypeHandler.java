package cn.jieyuc.luminova.datasource.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(JsonNode.class)
@MappedJdbcTypes(JdbcType.OTHER)
public class JsonbTypeHandler extends BaseTypeHandler<JsonNode> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    @Override
    public void setNonNullParameter(
            PreparedStatement preparedStatement,
            int index,
            JsonNode parameter,
            JdbcType jdbcType
    ) throws SQLException {
        PGobject jsonb = new PGobject();
        jsonb.setType("jsonb");
        jsonb.setValue(parameter.toString());
        preparedStatement.setObject(index, jsonb);
    }

    @Override
    public JsonNode getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        return parse(resultSet.getObject(columnName));
    }

    @Override
    public JsonNode getNullableResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return parse(resultSet.getObject(columnIndex));
    }

    @Override
    public JsonNode getNullableResult(CallableStatement callableStatement, int columnIndex) throws SQLException {
        return parse(callableStatement.getObject(columnIndex));
    }

    private JsonNode parse(Object value) throws SQLException {
        if (value == null) {
            return null;
        }
        String json = value instanceof PGobject pgObject ? pgObject.getValue() : value.toString();
        if (json == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException exception) {
            throw new SQLException("Failed to parse PostgreSQL JSONB value", exception);
        }
    }
}
