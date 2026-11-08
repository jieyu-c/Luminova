package cn.jieyuc.file.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class FileArchitectureTest {
    @Test
    void domainMustNotDependOnFrameworks() {
        var classes = new ClassFileImporter().importPackages("cn.jieyuc.file");

        noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..", "com.baomidou..", "software.amazon..", "cn.dev33..")
                .check(classes);
    }

    @Test
    void interfacesMustNotDependOnPersistenceOrStorageAdapters() {
        var classes = new ClassFileImporter().importPackages("cn.jieyuc.file");

        noClasses().that().resideInAPackage("..interfaces..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..infrastructure.persistence..", "..infrastructure.storage..")
                .check(classes);
    }
}
