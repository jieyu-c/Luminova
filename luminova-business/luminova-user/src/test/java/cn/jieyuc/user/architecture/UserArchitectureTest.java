package cn.jieyuc.user.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.mapstruct.Mapper;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * 账号上下文 DDD 依赖方向自动检查。
 */
@AnalyzeClasses(packages = "cn.jieyuc.user", importOptions = ImportOption.DoNotIncludeTests.class)
class UserArchitectureTest {

    @ArchTest
    static final ArchRule DOMAIN_MUST_NOT_DEPEND_ON_FRAMEWORKS = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "org.mapstruct..", "com.baomidou..", "org.apache.ibatis..",
                    "org.apache.dubbo..", "cn.dev33..", "..interfaces..", "..infrastructure..");

    @ArchTest
    static final ArchRule CONTROLLERS_MUST_NOT_DEPEND_ON_INFRASTRUCTURE = noClasses()
            .that().resideInAPackage("..interfaces.controller..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..domain..",
                    "..persistence..", "..mapper..");

    @ArchTest
    static final ArchRule INNER_LAYERS_MUST_NOT_USE_MYBATIS = noClasses()
            .that().resideInAnyPackage("..domain..", "..application..", "..interfaces..")
            .should().dependOnClassesThat().resideInAnyPackage("com.baomidou..", "org.apache.ibatis..");

    /** 端口接口与用例实现分开放。MapStruct Converter 随生成实现留在功能包。 */
    @ArchTest
    static final ArchRule APPLICATION_PORTS_LIVE_APART_FROM_USE_CASES = classes()
            .that().areInterfaces()
            .and().resideInAPackage("..application..")
            .and().areNotAnnotatedWith(Mapper.class)
            .should().resideInAPackage("..application.port..");

    /** 端口包只承载可替换契约，不放用例实现。 */
    @ArchTest
    static final ArchRule PORT_PACKAGES_CONTAIN_ONLY_INTERFACES = classes()
            .that().resideInAPackage("..application.port..")
            .should().beInterfaces();
}
