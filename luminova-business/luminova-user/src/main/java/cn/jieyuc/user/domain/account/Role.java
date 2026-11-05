package cn.jieyuc.user.domain.account;

/**
 * MVP 用户角色。
 *
 * <p>角色是账号上下文中的授权属性，客户端提交的同名字段不能改变该值。</p>
 */
public enum Role {
    USER,
    ADMIN
}
