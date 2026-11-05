package cn.jieyuc.user.infrastructure.persistence.mapper;

import cn.jieyuc.user.infrastructure.persistence.entity.LoginIdentityEntity;
import cn.jieyuc.user.infrastructure.persistence.entity.PasswordCredentialEntity;
import cn.jieyuc.user.infrastructure.persistence.entity.UserAccountEntity;
import cn.jieyuc.user.infrastructure.persistence.entity.AdminUserRow;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户聚合 MyBatis Mapper。
 *
 * <p>只负责 SQL 映射，不承载注册、认证或账号状态规则。</p>
 */
@Mapper
public interface UserAccountMapper {

    @Select("""
            SELECT id, nickname, avatar_url AS avatarUrl, role, status,
                   last_login_at AS lastLoginAt, created_at AS createdAt,
                   updated_at AS updatedAt, version
              FROM user_account WHERE id = #{id}
            """)
    UserAccountEntity findAccountById(long id);

    @Select("""
            SELECT a.id, a.nickname, a.avatar_url AS avatarUrl, a.role, a.status,
                   a.last_login_at AS lastLoginAt, a.created_at AS createdAt,
                   a.updated_at AS updatedAt, a.version
              FROM user_account a
              JOIN user_identity i ON i.user_id = a.id
             WHERE i.identity_type = #{type} AND i.provider = #{provider}
               AND i.identifier = #{identifier}
            """)
    UserAccountEntity findAccountByIdentity(@Param("type") String type,
                                            @Param("provider") String provider,
                                            @Param("identifier") String identifier);

    @Select("""
            SELECT id, user_id AS userId, identity_type AS identityType, provider,
                   identifier, identifier_masked AS identifierMasked,
                   verified_at AS verifiedAt, created_at AS createdAt, updated_at AS updatedAt
              FROM user_identity WHERE user_id = #{userId} ORDER BY id
            """)
    List<LoginIdentityEntity> findIdentities(long userId);

    @Select("""
            SELECT identity_id AS identityId, password_hash AS passwordHash,
                   password_changed_at AS passwordChangedAt,
                   created_at AS createdAt, updated_at AS updatedAt
              FROM password_credential WHERE identity_id = #{identityId}
            """)
    PasswordCredentialEntity findPasswordCredential(long identityId);

    @Insert("""
            INSERT INTO user_account
            (id, nickname, avatar_url, role, status, last_login_at, created_at, updated_at, version)
            VALUES (#{id}, #{nickname}, #{avatarUrl}, #{role}, #{status}, #{lastLoginAt},
                    #{createdAt}, #{updatedAt}, #{version})
            """)
    void insertAccount(UserAccountEntity entity);

    @Update("""
            UPDATE user_account
               SET nickname = #{nickname}, avatar_url = #{avatarUrl}, role = #{role},
                   status = #{status}, last_login_at = #{lastLoginAt},
                   updated_at = #{updatedAt}, version = version + 1
             WHERE id = #{id} AND version = #{version}
            """)
    int updateAccount(UserAccountEntity entity);

    @Insert("""
            INSERT INTO user_identity
            (id, user_id, identity_type, provider, identifier, identifier_masked,
             verified_at, created_at, updated_at)
            VALUES (#{id}, #{userId}, #{identityType}, #{provider}, #{identifier},
                    #{identifierMasked}, #{verifiedAt}, #{createdAt}, #{updatedAt})
            """)
    void insertIdentity(LoginIdentityEntity entity);

    @Insert("""
            INSERT INTO password_credential
            (identity_id, password_hash, password_changed_at, created_at, updated_at)
            VALUES (#{identityId}, #{passwordHash}, #{passwordChangedAt}, #{createdAt}, #{updatedAt})
            """)
    void insertPasswordCredential(PasswordCredentialEntity entity);

    @Update("""
            UPDATE password_credential
               SET password_hash = #{passwordHash}, password_changed_at = #{passwordChangedAt},
                   updated_at = #{updatedAt}
             WHERE identity_id = #{identityId}
            """)
    int updatePasswordCredential(PasswordCredentialEntity entity);

    @Select("""
            <script>
            SELECT a.id AS userId, i.identifier_masked AS maskedPhone, a.nickname,
                   a.role, a.status, a.created_at AS createdAt
              FROM user_account a
              LEFT JOIN user_identity i ON i.user_id = a.id
                    AND i.identity_type = 'PHONE' AND i.provider = 'LOCAL'
             WHERE 1 = 1
             <if test='status != null and status != ""'>AND a.status = #{status}</if>
             <if test='userId != null'>AND a.id = #{userId}</if>
             <if test='phone != null and phone != ""'>AND i.identifier = #{phone}</if>
             ORDER BY a.created_at DESC, a.id DESC
             LIMIT #{size} OFFSET #{offset}
            </script>
            """)
    List<AdminUserRow> queryAdminUsers(@Param("status") String status,
                                       @Param("userId") Long userId,
                                       @Param("phone") String phone,
                                       @Param("size") int size,
                                       @Param("offset") int offset);

    @Select("""
            <script>
            SELECT COUNT(DISTINCT a.id)
              FROM user_account a
              LEFT JOIN user_identity i ON i.user_id = a.id
                    AND i.identity_type = 'PHONE' AND i.provider = 'LOCAL'
             WHERE 1 = 1
             <if test='status != null and status != ""'>AND a.status = #{status}</if>
             <if test='userId != null'>AND a.id = #{userId}</if>
             <if test='phone != null and phone != ""'>AND i.identifier = #{phone}</if>
            </script>
            """)
    long countAdminUsers(@Param("status") String status,
                         @Param("userId") Long userId,
                         @Param("phone") String phone);
}
