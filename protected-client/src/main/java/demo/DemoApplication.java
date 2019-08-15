package demo;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    @Value("${protected-server.url:https://localhost:8888/secure/secure}")
    private String protectedServerURL;

    @Value("${trustStoreResource:}")
    private String trustStoreResource;

    @Value("${trustStorePassword:}")
    private String trustStorePassword;

    @Value("${keyStoreResource:}")
    private String keyStoreResource;

    @Value("${keyStorePassword:}")
    private String keyStorePassword;

    @Autowired
    private RestTemplate restTemplate;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        String quote = restTemplate.getForObject(protectedServerURL,
                String.class);
        System.out.println(quote.toString());
    }

    @Bean
    public RestTemplate restTemplate() throws GeneralSecurityException,
            IOException {
        RestTemplate restTemplate = new RestTemplate(clientRequestFactory());
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestFactory clientRequestFactory()
            throws GeneralSecurityException, IOException {
        return new HttpComponentsClientHttpRequestFactory(httpClient());
    }

    @Bean
    public HttpClient httpClient() throws GeneralSecurityException, IOException {
        SSLContextBuilder sslcontextBuilder = SSLContexts.custom()
                .loadTrustMaterial(getResourceUrl(trustStoreResource), trustStorePassword.toCharArray());

        SSLContext sslcontext = sslcontextBuilder
                .loadKeyMaterial(getResourceUrl(keyStoreResource), keyStorePassword.toCharArray(), keyStorePassword.toCharArray())
                .build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new DefaultHostnameVerifier());
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslsf).build();

        return httpClient;
    }

    private URL getResourceUrl(String path) throws IOException {
        ClassPathResource keystoreResource1 = new ClassPathResource(path);
        URL url = keystoreResource1.getURL();

        return url;
    }
}