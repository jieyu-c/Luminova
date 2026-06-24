package cn.jieyuc.user.interfaces.dto.request;

public record ChangePasswordRequest(String oldPassword, String newPassword) {
}
