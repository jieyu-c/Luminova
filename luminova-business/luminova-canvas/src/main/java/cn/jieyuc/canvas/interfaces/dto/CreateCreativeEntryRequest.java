package cn.jieyuc.canvas.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 创建文件夹或画布请求。 */
public record CreateCreativeEntryRequest(Long parentId,
                                         @NotBlank @Size(max = 100) String name) {
}
