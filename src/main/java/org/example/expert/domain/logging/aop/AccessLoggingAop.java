package org.example.expert.domain.logging.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.expert.domain.manager.controller.JwtAuthService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalTime;
import java.util.Objects;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AccessLoggingAop {

    private final JwtAuthService jwtAuthService;

    @Pointcut(
            """
            execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..)) ||
            execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))
            """
    )
    public void CommentAdminControllerMethod() {
    }

    @After("CommentAdminControllerMethod()")
    public void logAccess(JoinPoint joinPoint) {

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (!Objects.isNull(requestAttributes)) {

            HttpServletRequest request = requestAttributes.getRequest();

            String authorization = request.getHeader("Authorization");

            if (!authorization.isEmpty()) {
                long userId = jwtAuthService.getUserIdByToken(authorization);

                log.info("호출된 메서드명={}", joinPoint.getSignature().getName());
                log.info("요청한 사용자의 ID={}", userId);
                log.info("API 요청 시각={}", LocalTime.now().withNano(0));
                log.info("API 요청 URL={}", request.getRequestURL().toString());
            }
        }
    }
}
