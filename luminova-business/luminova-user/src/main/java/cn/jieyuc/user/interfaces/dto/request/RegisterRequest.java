package cn.jieyuc.user.interfaces.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public record RegisterRequest(String phone, String password, String verifyCode) {
}
