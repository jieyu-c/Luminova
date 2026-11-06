package cn.jieyuc.user.infrastructure.persistence.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import cn.jieyuc.user.domain.account.AccountStatus;
import cn.jieyuc.user.domain.account.IdentityKey;
import cn.jieyuc.user.domain.account.IdentityType;
import cn.jieyuc.user.domain.account.LoginIdentity;
import cn.jieyuc.user.domain.account.PasswordCredential;
import cn.jieyuc.user.domain.account.Role;
import cn.jieyuc.user.domain.account.UserAccount;
import cn.jieyuc.user.domain.account.UserId;
import cn.jieyuc.user.infrastructure.persistence.entity.LoginIdentityEntity;
import cn.jieyuc.user.infrastructure.persistence.entity.PasswordCredentialEntity;
import cn.jieyuc.user.infrastructure.persistence.entity.UserAccountEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/**
 * 用户主体与持久化行的结构转换。
 */
class UserAccountPersistenceConverterTest {
    private final UserAccountPersistenceConverter converter = Mappers.getMapper(UserAccountPersistenceConverter.class);

    @Test
    @DisplayName("聚合写入行时保留空头像，并把凭证时间对齐到身份验证时间和密码变更时间")
    void shouldMapAggregateOntoRows() {
        OffsetDateTime created = OffsetDateTime.parse("2026-11-05T01:00:00Z");
        OffsetDateTime changed = created.plusHours(2);
        UserAccount account = account(created, changed);

        UserAccountEntity accountEntity = converter.toAccountEntity(account);
        LoginIdentityEntity identityEntity = converter.toIdentityEntity(account, account.identities().getFirst());
        PasswordCredentialEntity credentialEntity = converter.toCredentialEntity(account.identities().getFirst());

        assertEquals(8L, accountEntity.getId());
        assertEquals("昵称", accountEntity.getNickname());
        assertNull(accountEntity.getAvatarUrl());
        assertEquals(Role.ADMIN.name(), accountEntity.getRole());
        assertEquals(AccountStatus.DISABLED.name(), accountEntity.getStatus());
        assertNull(accountEntity.getLastLoginAt());
        assertEquals(created, accountEntity.getCreatedAt());
        assertEquals(changed, accountEntity.getUpdatedAt());
        assertEquals(4, accountEntity.getVersion());
        assertEquals(9L, identityEntity.getId());
        assertEquals(8L, identityEntity.getUserId());
        assertEquals(IdentityType.PHONE.name(), identityEntity.getIdentityType());
        assertEquals(IdentityKey.LOCAL_PROVIDER, identityEntity.getProvider());
        assertEquals("13800138000", identityEntity.getIdentifier());
        assertEquals("138****8000", identityEntity.getIdentifierMasked());
        assertEquals(created, identityEntity.getVerifiedAt());
        assertEquals(created, identityEntity.getCreatedAt());
        assertEquals(changed, identityEntity.getUpdatedAt());
        assertEquals(9L, credentialEntity.getIdentityId());
        assertEquals("hash-value", credentialEntity.getPasswordHash());
        assertEquals(changed, credentialEntity.getPasswordChangedAt());
        assertEquals(created, credentialEntity.getCreatedAt());
        assertEquals(changed, credentialEntity.getUpdatedAt());
    }

    @Test
    @DisplayName("从表行装载聚合时还原值对象，没有凭证的身份保持无密码")
    void shouldRestoreAggregateFromRows() {
        OffsetDateTime created = OffsetDateTime.parse("2026-11-05T01:00:00Z");
        OffsetDateTime changed = created.plusHours(2);
        UserAccountEntity accountEntity = converter.toAccountEntity(account(created, changed));
        LoginIdentityEntity identityEntity = converter.toIdentityEntity(account(created, changed),
                account(created, changed).identities().getFirst());
        PasswordCredentialEntity credentialEntity = converter.toCredentialEntity(account(created, changed).identities().getFirst());

        LoginIdentity withPassword = converter.toIdentity(identityEntity, credentialEntity);
        LoginIdentity withoutPassword = converter.toIdentity(identityEntity, null);
        UserAccount restored = converter.toAccount(accountEntity, List.of(withPassword));

        assertEquals(new UserId(8), restored.id());
        assertEquals(Role.ADMIN, restored.role());
        assertEquals(AccountStatus.DISABLED, restored.status());
        assertNull(restored.avatarUrl());
        assertNull(restored.lastLoginAt());
        assertEquals(4, restored.version());
        assertEquals(IdentityKey.LOCAL_PROVIDER, withPassword.key().provider());
        assertEquals("hash-value", withPassword.passwordCredential().passwordHash());
        assertEquals(changed, withPassword.passwordCredential().changedAt());
        assertNull(withoutPassword.passwordCredential());
    }

    @Test
    @DisplayName("toDomain 按身份主键对齐凭证，没有凭证行的身份保持无密码")
    void shouldAssembleAggregateFromLoadedRows() {
        OffsetDateTime created = OffsetDateTime.parse("2026-11-05T01:00:00Z");
        OffsetDateTime changed = created.plusHours(2);
        UserAccount source = account(created, changed);
        UserAccountEntity accountEntity = converter.toAccountEntity(source);
        LoginIdentityEntity identityEntity = converter.toIdentityEntity(source, source.identities().getFirst());
        PasswordCredentialEntity credentialEntity = converter.toCredentialEntity(source.identities().getFirst());

        UserAccount restored = converter.toDomain(accountEntity, List.of(identityEntity), List.of(credentialEntity));
        UserAccount withoutPassword = converter.toDomain(accountEntity, List.of(identityEntity), List.of());

        assertEquals(new UserId(8), restored.id());
        assertEquals("hash-value", restored.identities().getFirst().passwordCredential().passwordHash());
        assertNull(withoutPassword.identities().getFirst().passwordCredential());
    }

    private UserAccount account(OffsetDateTime created, OffsetDateTime changed) {
        // 小写提供方应被身份键规范成统一的大写代号。
        LoginIdentity identity = new LoginIdentity(9,
                new IdentityKey(IdentityType.PHONE, IdentityKey.LOCAL_PROVIDER.toLowerCase(Locale.ROOT),
                        "13800138000"),
                "138****8000", created, new PasswordCredential("hash-value", changed));
        return new UserAccount(new UserId(8), "昵称", null, Role.ADMIN, AccountStatus.DISABLED,
                null, created, changed, 4, List.of(identity));
    }
}
