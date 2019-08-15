package demo;

import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomContainer implements
        WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Value("${keystore.file}")
    Resource keystoreFile;
    @Value("${keystore.alias}")
    String keystoreAlias;
    @Value("${keystore.type}")
    String keystoreType;
    @Value("${keystore.pass}")
    String keystorePass;
    @Value("${truststore.file}")
    Resource truststoreFile;
    @Value("${truststore.type}")
    String truststoreType;
    @Value("${truststore.pass}")
    String truststorePass;
    @Value("${tls.port}")
    int tlsPort;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.setContextPath("");
        factory.setPort(8080);

        factory.addConnectorCustomizers(connector -> {
            connector.setPort(tlsPort);
            connector.setSecure(true);
            connector.setScheme("https");
            connector.setAttribute("keyAlias", keystoreAlias);
            connector.setAttribute("keystorePass", keystorePass);
            String absoluteKeystoreFile;
            try {
                absoluteKeystoreFile = keystoreFile.getFile().getAbsolutePath();
                connector.setAttribute("keystoreFile", absoluteKeystoreFile);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot load keystore", e);
            }
            connector.setAttribute("clientAuth", "true");
            connector.setAttribute("truststorePass", truststorePass);
            String absoluteTruststoreFile;
            try {
                absoluteTruststoreFile = truststoreFile.getFile().getAbsolutePath();
                connector.setAttribute("truststoreFile", absoluteTruststoreFile);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot load truststore", e);
            }
            connector.setAttribute("sslProtocol", "TLS");
            connector.setAttribute("SSLEnabled", true);

            Http11NioProtocol proto = (Http11NioProtocol) connector.getProtocolHandler();
            proto.setSSLEnabled(true);
            proto.setKeystoreFile(absoluteKeystoreFile);
            proto.setKeystorePass(keystorePass);
            proto.setKeystoreType(keystoreType);
            proto.setKeyAlias(keystoreAlias);
            proto.setTruststoreFile(absoluteTruststoreFile);
            proto.setTruststorePass(truststorePass);
            proto.setTruststoreType(truststoreType);
        });
    }
}
