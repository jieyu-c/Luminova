package cn.jieyuc.luminovanacos.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NacosPrompt {

    /**
     * Prompt key in Nacos AI Registry / Prompt Registry.
     */
    String key();

    /**
     * Optional prompt version. Do not use together with label unless your
     * business rule explicitly needs version to win.
     */
    String version() default "";

    /**
     * Optional prompt label, such as dev, test, prod.
     */
    String label() default "";

    /**
     * Fallback prompt used when Nacos is unavailable or returns no template.
     */
    String defaultValue() default "";

    /**
     * Whether to call Nacos synchronously during bean initialization.
     * Keep this false for faster startup; the background refresher will load it.
     */
    @Deprecated(since = "0.0.1", forRemoval = false)
    boolean loadOnStartup() default false;

    /**
     * Whether this field should be updated by the scheduled refresher.
     */
    boolean refresh() default true;
}
