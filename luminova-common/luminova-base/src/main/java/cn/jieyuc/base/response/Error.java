package cn.jieyuc.base.response;

import java.io.Serializable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@ToString
public class Error {
    private Integer errCode;
    private String errMSg;
}
