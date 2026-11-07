package cn.jieyuc.canvas.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** 画布上下文 DDD 依赖方向检查。 */
@AnalyzeClasses(packages = "cn.jieyuc.canvas", importOptions = ImportOption.DoNotIncludeTests.class)
class CanvasArchitectureTest {
    @ArchTest
    static final ArchRule DOMAIN_MUST_NOT_DEPEND_ON_FRAMEWORKS = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "org.mapstruct..", "com.baomidou..", "org.apache.ibatis..",
                    "cn.dev33..", "..interfaces..", "..infrastructure..");

    @ArchTest
    static final ArchRule CONTROLLERS_MUST_NOT_DEPEND_ON_INNER_OR_INFRASTRUCTURE = noClasses()
            .that().resideInAPackage("..interfaces.controller..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..domain..");

    @ArchTest
    static final ArchRule INNER_LAYERS_MUST_NOT_USE_MYBATIS = noClasses()
            .that().resideInAnyPackage("..domain..", "..application..", "..interfaces..")
            .should().dependOnClassesThat().resideInAnyPackage("com.baomidou..", "org.apache.ibatis..");
}
