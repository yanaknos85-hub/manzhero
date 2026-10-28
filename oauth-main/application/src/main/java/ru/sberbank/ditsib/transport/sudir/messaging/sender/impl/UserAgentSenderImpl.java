package ru.sberbank.ditsib.transport.sudir.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.user.agent.messages.UserAgentMessage;
import ru.sberbank.ditsib.transport.sudir.business.model.Account;
import ru.sberbank.ditsib.transport.sudir.messaging.sender.UserAgentSender;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@RequiredArgsConstructor
@Component
@Slf4j
class UserAgentSenderImpl implements UserAgentSender {

    @Qualifier("userAgentOutput")
    private final ObjectProvider<OutputBridge> userAgentOutput;

    @Qualifier("userAgentOutputSsl")
    private final ObjectProvider<OutputBridge> userAgentOutputSsl;

    private static final String AUTH_TYPE = "SUDIR";

    @Override
    public void send(Account account, String userAgent, String clientType, String requestUrl) {
        var readableUserAgent = UADetectorServiceFactory.getResourceModuleParser().parse(userAgent);
        var userAgentMessage = new UserAgentMessage();
        userAgentMessage.setUserId(account.getId());
        userAgentMessage.setEntranceTime(LocalDateTime.now(ZoneOffset.UTC));
        userAgentMessage.setBrowser(readableUserAgent.getName());
        userAgentMessage.setOperatingSystem(readableUserAgent.getOperatingSystem().getName());
        userAgentMessage.setDevice(readableUserAgent.getDeviceCategory().getName()) ;
        userAgentMessage.setClientType(clientType);
        userAgentMessage.setUserAgentValue(userAgent);
        if(requestUrl==null){
            var servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
            var request = servletRequestAttributes != null ? servletRequestAttributes.getRequest() : null;
            userAgentMessage.setUrl(request!=null ? request.getRequestURL().toString() : null);
        } else {
            userAgentMessage.setUrl(requestUrl);
        }
        userAgentMessage.setAuthType(AUTH_TYPE);
        userAgentOutput.ifAvailable(ob -> ob.send(userAgentMessage));
        userAgentOutputSsl.ifAvailable(ob -> ob.send(userAgentMessage));
    }
}
