package com.ruoyi.framework.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tomcat服务器配置 - 同时支持HTTP和HTTPS
 * 
 * @author ruoyi
 */
@Configuration
public class TomcatConfig
{
    @Bean
    public ServletWebServerFactory servletContainer()
    {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        // 添加额外的HTTP连接器(主连接器在application.yml中配置)
        tomcat.addAdditionalTomcatConnectors(createSslConnector());
        return tomcat;
    }

    /**
     * 创建SSL连接器,支持HTTPS访问
     */
    private Connector createSslConnector()
    {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("https");
        connector.setPort(8443);  // HTTPS端口
        connector.setSecure(true);
        connector.setProperty("keystoreFile", "src/main/resources/tomcat.keystore");
        connector.setProperty("keystorePass", "changeit");
        connector.setProperty("keyAlias", "tomcat");
        connector.setProperty("sslProtocol", "TLS");
        return connector;
    }
}
