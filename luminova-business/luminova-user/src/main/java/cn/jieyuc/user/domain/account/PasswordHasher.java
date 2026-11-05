package cn.jieyuc.user.domain.account;

/**
 * 密码哈希 seam。
 *
 * <p>领域层通过该小接口隐藏具体算法和参数。实现必须使用不可逆慢哈希，并且不能记录
 * 原始密码或哈希。</p>
 */
public interface PasswordHasher {

    /**
     * 将原始密码转换为不可逆哈希。
     */
    String hash(String rawPassword);

    /**
     * 校验原始密码是否匹配已有哈希。
     */
    boolean matches(String rawPassword, String encodedPassword);
}
