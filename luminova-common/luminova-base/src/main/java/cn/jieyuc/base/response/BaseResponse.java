package cn.jieyuc.base.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@ToString
public class BaseResponse {
    private Boolean success;
    private Error error;
}
