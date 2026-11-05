package cn.jieyuc.user.domain.account;

import java.util.regex.Pattern;

/**
 * 规范化后的 E.164 手机号。
 *
 * <p>MVP 接受中国大陆 11 位号码或带 +86 的号码，并统一保存为 +86 格式。完整值属于
 * 敏感个人信息，禁止直接写入日志。</p>
 */
public record PhoneNumber(String value) {
    private static final Pattern MAINLAND = Pattern.compile("1[3-9]\\d{9}");

    /**
     * 规范化手机号。
     *
     * @param raw 用户输入手机号
     * @return 规范化手机号
     */
    public static PhoneNumber normalize(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        String compact = raw.trim().replace(" ", "").replace("-", "");
        String local = compact.startsWith("+86") ? compact.substring(3) : compact;
        if (!MAINLAND.matcher(local).matches()) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        return new PhoneNumber("+86" + local);
    }

    /**
     * 返回用于界面和审计日志的脱敏手机号。
     *
     * @return 脱敏手机号
     */
    public String masked() {
        String local = value.substring(3);
        return "+86" + local.substring(0, 3) + "****" + local.substring(7);
    }
}
