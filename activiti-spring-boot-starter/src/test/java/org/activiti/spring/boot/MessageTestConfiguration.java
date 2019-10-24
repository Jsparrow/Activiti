package org.activiti.spring.boot;

import java.util.ArrayList;
import java.util.List;

import org.activiti.api.process.model.events.BPMNMessageEvent;
import org.activiti.api.process.model.events.BPMNMessageReceivedEvent;
import org.activiti.api.process.model.events.BPMNMessageSentEvent;
import org.activiti.api.process.model.events.BPMNMessageWaitingEvent;
import org.activiti.api.process.runtime.events.listener.BPMNElementEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageTestConfiguration {

    public static List<BPMNMessageEvent> messageEvents = new ArrayList<>();

    @Bean
    public BPMNElementEventListener<BPMNMessageSentEvent> messageSentEventListener() {
        return messageEvents::add;
    }
    
    @Bean
    public BPMNElementEventListener<BPMNMessageReceivedEvent> messageReceivedEventListener() {
        return messageEvents::add;
    }
    
    @Bean
    public BPMNElementEventListener<BPMNMessageWaitingEvent> messageWaitingEventListener() {
        return messageEvents::add;
    }  
 
}
