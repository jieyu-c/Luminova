package cn.jieyuc.luminova.base.mapping;

import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.mapstruct.ap.spi.DefaultAccessorNamingStrategy;

/**
 * 让 MapStruct 识别领域对象上与字段同名的读取方法。
 *
 * <p>聚合和实体使用 {@code id()}、{@code nickname()}，不是 JavaBean 的 {@code getId()}。
 * 默认策略只认 get/is 前缀，漏掉这些读取器后就无法在不增加 setter 的前提下走构造器映射。
 * 这里只补充公开、无参、非 void、方法名等于本类字段的方法。带参数的行为方法、记录和枚举
 * 仍走 MapStruct 原有规则，避免把 {@code disable(operator, now)} 当成属性。</p>
 */
public class FluentDomainAccessorNamingStrategy extends DefaultAccessorNamingStrategy {
    private static final Set<String> EXCLUDED = Set.of("toString", "hashCode", "clone", "getClass");

    @Override
    public boolean isGetterMethod(ExecutableElement method) {
        return super.isGetterMethod(method) || isFluentGetter(method);
    }

    @Override
    public String getPropertyName(ExecutableElement getterOrSetterMethod) {
        if (isFluentGetter(getterOrSetterMethod)) {
            return getterOrSetterMethod.getSimpleName().toString();
        }
        return super.getPropertyName(getterOrSetterMethod);
    }

    private boolean isFluentGetter(ExecutableElement method) {
        if (!method.getParameters().isEmpty() || method.getReturnType().getKind() == TypeKind.VOID) {
            return false;
        }
        if (!method.getModifiers().contains(Modifier.PUBLIC) || method.getModifiers().contains(Modifier.STATIC)) {
            return false;
        }
        String name = method.getSimpleName().toString();
        if (EXCLUDED.contains(name) || method.getEnclosingElement().getKind() != ElementKind.CLASS) {
            return false;
        }
        for (Element enclosed : method.getEnclosingElement().getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD && enclosed.getSimpleName().contentEquals(name)) {
                return true;
            }
        }
        return false;
    }
}
