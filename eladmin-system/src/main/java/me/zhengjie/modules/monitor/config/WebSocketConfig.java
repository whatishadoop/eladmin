package me.zhengjie.modules.monitor.config;

import lombok.extern.slf4j.Slf4j;
import me.zhengjie.modules.monitor.domain.LogMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;

/**
 * 配置WebSocket消息代理端点，即stomp服务端
 * @author jie
 * @date 2018-12-24
 */
@Slf4j
@Configuration
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 在什么情况下使用线程池
     * 1.单个任务处理的时间比较短
     * 2.需处理的任务的数量大
     *
     * 使用线程池的好处:
     * 1.减少在创建和销毁线程上所花的时间以及系统资源的开销
     * 2.如不使用线程池，有可能造成系统创建大量线程而导致消耗完系统内存
     */
    @Autowired
    private ExecutorService executorService; // java线程池ExecutorService

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket")
                .setAllowedOrigins("*")
                .withSockJS();
    }

    // WebSocketStompConfig还通过重载configureMessageBroker（）方法配置了一个简单的消息代理。这个方法是可选的，如果不重载它的话，将会自动配置一个简单的内存消息代理，用它来处理以“/topic”为前缀的消息
    //@Override
    //public void configureMessageBroker(MessageBrokerRegistry registry)
    //{
    //
    //    //表明在topic、queue、users这三个域上可以向客户端发消息。
    //    registry.enableSimpleBroker("/topic","/queue","/users");
    //    //客户端向服务端发起请求时，需要以/app为前缀。
    //    registry.setApplicationDestinationPrefixes("/app");
    //    //给指定用户发送一对一的消息前缀是/users/。
    //    registry.setUserDestinationPrefix("/users/");
    //}


    /**
     * 推送日志到/topic/pullLogger
     */
    @PostConstruct  // 项目启动时，初始websocket服务
    public void pushLogger(){
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        // 若队列中没有实例则阻塞
                        LogMessage log = LoggerQueue.getInstance().poll();
                        if(log!=null){
                            // 格式化异常堆栈信息
                            if("ERROR".equals(log.getLevel()) && "me.zhengjie.common.exception.handler.GlobalExceptionHandler".equals(log.getClassName())){
                                log.setBody("<pre>"+log.getBody()+"</pre>");
                            }
                            if(log.getClassName().equals("jdbc.resultsettable")){
                                log.setBody("<br><pre>"+log.getBody()+"</pre>");
                            }
                            if(messagingTemplate!=null){
// Spring的SimpMessagingTemplate能够在应用的任何地方发送消息，甚至不必以首先接收一条消息作为前提。//使用SimpMessagingTemplate的最简单方式是将它（或者其接口SimpMessageSendingOperations）自动装配到所需的对象中，默认内存队列为topic
                                messagingTemplate.convertAndSend("/topic/logMsg",log);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        executorService.submit(runnable);
    }
}