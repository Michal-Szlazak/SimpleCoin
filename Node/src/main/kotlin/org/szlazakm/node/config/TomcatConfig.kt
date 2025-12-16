package org.szlazakm.node.config

import jakarta.servlet.ServletContainerInitializer
import jakarta.servlet.ServletContext
import org.apache.catalina.Context
import org.apache.tomcat.websocket.server.WsServerContainer
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class TomcatConfig {

    @Bean
    fun tomcatCustomizer(): WebServerFactoryCustomizer<TomcatServletWebServerFactory?> {
        return WebServerFactoryCustomizer { factory: TomcatServletWebServerFactory? ->
            factory!!.addContextCustomizers(TomcatContextCustomizer { context: Context? ->
                context!!.addServletContainerInitializer(ServletContainerInitializer { sci: MutableSet<Class<*>?>?, servletContext: ServletContext? ->
                    if (servletContext != null) {
                        val container =
                            servletContext.getAttribute("jakarta.websocket.server.ServerContainer") as WsServerContainer?

                        if (container != null) {
                            val bufferSize = 100 * 1024 * 1024 // 10MB
                            container.defaultMaxBinaryMessageBufferSize = bufferSize
                            container.defaultMaxTextMessageBufferSize = bufferSize
                        }
                    }
                }, null)
            }
            )
        }
    }

}