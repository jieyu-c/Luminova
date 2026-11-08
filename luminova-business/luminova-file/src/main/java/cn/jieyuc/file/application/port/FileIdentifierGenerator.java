package cn.jieyuc.file.application.port;

/** 为云文件和上传会话分配全局稳定标识。 */
public interface FileIdentifierGenerator {
    /** 生成下一个正数标识。 */
    long nextId();
}
