package cn.jieyuc.user.domain.model.user;

import cn.hutool.core.lang.RegexPool;

import java.io.Serializable;

public record Phone(String value) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static Phone of(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone must not be blank");
        }
        if (!phone.matches(RegexPool.MOBILE)) {
            throw new IllegalArgumentException("Phone is invalid");
        }
        return new Phone(phone);
    }
}
