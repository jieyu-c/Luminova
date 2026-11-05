package cn.jieyuc.user.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

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
                    "org.springframework..", "com.baomidou..", "org.apache.ibatis..",
                    "org.apache.dubbo..", "cn.dev33..", "..interfaces..", "..infrastructure..");

    @ArchTest
    static final ArchRule CONTROLLERS_MUST_NOT_DEPEND_ON_INFRASTRUCTURE = noClasses()
            .that().resideInAPackage("..interfaces.controller..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..domain..",
                    "..persistence..", "..mapper..");
}
