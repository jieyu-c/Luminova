package cn.jieyuc.luminova.messaging.core;

/**
 * Adapter 对高级消息语义的明确能力声明。
 *
 * @param fifo 是否支持局部顺序
 * @param delayedDelivery 是否支持延迟投递
 * @param nativeDeadLetter 是否具有原生死信能力
 * @param brokerFiltering 是否支持 Broker 过滤
 * @param transactionalMessage 是否支持平台原生事务消息
 */
public record TransportCapabilities(
        boolean fifo,
        boolean delayedDelivery,
        boolean nativeDeadLetter,
        boolean brokerFiltering,
        boolean transactionalMessage) {
}
