package cn.jieyuc.luminova.datasource.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.Locale;

@AutoConfiguration
public class MybatisPlusAutoConfiguration {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(Environment environment) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(resolveDbType(environment)));
        return interceptor;
    }

    @Bean
    public MetaObjectHandler mybatisPlusMetaObjectHandler() {
        return new MybatisPlusMetaObjectHandler();
    }

    private DbType resolveDbType(Environment environment) {
        String datasourceType = environment.getProperty("luminova.datasource.type", "postgresql");
        String normalized = datasourceType.toLowerCase(Locale.ROOT).replace("-", "").replace("_", "");
        return switch (normalized) {
            case "pg", "pgsql", "postgres", "postgresql" -> DbType.POSTGRE_SQL;
            case "mysql" -> DbType.MYSQL;
            default -> throw new IllegalArgumentException(
                    "Unsupported luminova.datasource.type: " + datasourceType
                            + ". Supported values: postgresql, mysql"
            );
        };
    }
}
