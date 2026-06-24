package cn.jieyuc.luminova.base.response;

import java.io.Serializable;

import lombok.*;

@Data
@Getter
@Setter
@ToString
@AllArgsConstructor
public class Error {
    private String errCode;
    private String errMSg;
}
