package org.example.expert.domain.logging.aop;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.assertj.core.api.Assertions;
import org.example.expert.domain.manager.controller.JwtAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@Slf4j
@ExtendWith(MockitoExtension.class)
class AccessLoggingAopTest {

    @InjectMocks
    AccessLoggingAop accessLoggingAop;

    @Mock
    JwtAuthService jwtAuthService;

    ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void appendLog() {
        listAppender = new ListAppender<>();

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger(AccessLoggingAop.class);

        listAppender.setContext(context);
        logger.addAppender(listAppender);
        listAppender.start();
    }

    @Test
    void logAccess() {
        //given
        HttpServletRequest request = mock(HttpServletRequest.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        JoinPoint joinPoint = mock(JoinPoint.class);
        Signature signature = mock(Signature.class);

        given(joinPoint.getSignature()).willReturn(signature);
        given(signature.getName()).willReturn("mockSignature");

        //request.getHeader() 호출 시 null이면 안 됨.
        given(request.getHeader("Authorization")).willReturn("Bearer TestLogAccess");
        //request.getRequestURL() 호출 시
        given(request.getRequestURL()).willReturn(new StringBuffer("http://localhost8080/admin/comments/1"));
        //userId 필요
        given(jwtAuthService.getUserIdByToken(anyString())).willReturn(1L);

        //when
        accessLoggingAop.logAccess(joinPoint);

        //then
        List<String> collect = listAppender.list.stream().map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList());
        Assertions.assertThat(collect).contains(
                "호출된 메서드명=mockSignature",
                "요청한 사용자의 ID=1",
                "API 요청 시각=" + LocalTime.now().withNano(0).toString(),
                "API 요청 URL=http://localhost8080/admin/comments/1"
        );
    }
}