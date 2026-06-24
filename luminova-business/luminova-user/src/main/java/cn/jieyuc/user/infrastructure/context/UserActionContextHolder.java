package cn.jieyuc.user.infrastructure.context;

public final class UserActionContextHolder {
    private static final ThreadLocal<UserActionContext> CONTEXT = new ThreadLocal<>();

    private UserActionContextHolder() {
    }

    public static void set(UserActionContext context) {
        CONTEXT.set(context);
    }

    public static UserActionContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
