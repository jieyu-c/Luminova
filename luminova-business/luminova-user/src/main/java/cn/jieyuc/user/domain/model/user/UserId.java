package cn.jieyuc.user.domain.model.user;

import java.io.Serializable;

public record UserId(Long value) implements Serializable {

    private static final long serialVersionUID = 1L;

    public static UserId of(Long userId) {
        return new UserId(userId);
    }
}
