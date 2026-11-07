package cn.jieyuc.canvas.application.port.session;

/** 创作条目 ID 生成端口。 */
public interface CreativeEntryIdentifierGenerator {
    /** 生成全局唯一正数 ID。 */
    long nextId();
}
