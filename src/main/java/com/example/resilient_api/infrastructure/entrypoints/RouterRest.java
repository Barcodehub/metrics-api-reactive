package com.example.resilient_api.infrastructure.entrypoints;

import com.example.resilient_api.infrastructure.entrypoints.handler.BootcampReportHandlerImpl;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    @Bean
    @RouterOperations({
        @RouterOperation(path = "/metrics/bootcamp/report", method = RequestMethod.POST, beanClass = BootcampReportHandlerImpl.class, beanMethod = "registerBootcampReport"),
        @RouterOperation(path = "/metrics/bootcamp/most-popular", method = RequestMethod.GET, beanClass = BootcampReportHandlerImpl.class, beanMethod = "getMostPopularBootcamp")
    })
    public RouterFunction<ServerResponse> routerFunction(BootcampReportHandlerImpl bootcampReportHandler) {
        return route(POST("/metrics/bootcamp/report"), bootcampReportHandler::registerBootcampReport)
            .andRoute(GET("/metrics/bootcamp/most-popular"), bootcampReportHandler::getMostPopularBootcamp);
    }

}
