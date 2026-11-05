package cn.jieyuc.luminovaauth.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

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
}
