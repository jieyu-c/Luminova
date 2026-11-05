package cn.jieyuc.user.application.admin;

import java.util.List;

/**
 * 管理员用户分页结果。
 */
public record AdminUserPage(List<AdminUserSummary> items, long total, int page, int size) {
}
