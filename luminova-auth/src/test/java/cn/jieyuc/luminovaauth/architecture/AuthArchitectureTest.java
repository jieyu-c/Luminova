package cn.jieyuc.luminovaauth.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * 认证上下文 DDD 依赖方向自动检查。
 */
@AnalyzeClasses(packages = "cn.jieyuc.luminovaauth", importOptions = ImportOption.DoNotIncludeTests.class)
class AuthArchitectureTest {

    @ArchTest
    static final ArchRule DOMAIN_MUST_NOT_DEPEND_ON_FRAMEWORKS = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "org.apache.dubbo..", "cn.dev33..",
                    "..interfaces..", "..infrastructure..");

    @ArchTest
    static final ArchRule APPLICATION_MUST_NOT_DEPEND_ON_INFRASTRUCTURE = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..");

    /** 端口接口与用例实现分开放，避免抽象和实现挤在同一个包。 */
    @ArchTest
    static final ArchRule APPLICATION_PORTS_LIVE_APART_FROM_USE_CASES = classes()
            .that().areInterfaces()
            .and().resideInAPackage("..application..")
            .should().resideInAPackage("..application.port..");

    /** 端口包只承载可替换契约，不放用例实现。 */
    @ArchTest
    static final ArchRule PORT_PACKAGES_CONTAIN_ONLY_INTERFACES = classes()
            .that().resideInAPackage("..application.port..")
            .should().beInterfaces();
}
