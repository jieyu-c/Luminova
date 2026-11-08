package cn.jieyuc.file.domain;

/** 文件所属的业务上下文和业务对象。 */
public record BusinessOwnership(String contextCode, String objectType, String objectId) {
    /** 创建业务归属并校验每一段都有稳定值。 */
    public BusinessOwnership {
        if (blank(contextCode) || blank(objectType) || blank(objectId)) {
            throw new IllegalArgumentException("业务归属不能为空");
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
