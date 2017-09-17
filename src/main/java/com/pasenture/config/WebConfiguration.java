package com.pasenture.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.multipart.MultipartException;

import javax.servlet.Filter;
import java.nio.charset.Charset;

/**
 * Created by Jeon on 2017-06-04.
 */
@Configuration
public class WebConfiguration {

    /* 스프림의 기본 객체 매퍼를 사용자정의할 때 사용.
       * Spring MVC에서는 ObjectMapper를 제공한다 */
    @Bean
    @Primary
    public ObjectMapper objectsMapper(Jackson2ObjectMapperBuilder builder) {

        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }

    /*
        모든 HTTP 메시지를 UTF-8로 강제 변환
     */
    @Bean
    public HttpMessageConverter<String> responseBodyConverter() {
        return new StringHttpMessageConverter(Charset.forName("UTF-8"));
    }

    /*
        외부 API 활용을 위한 RestTemplate 빈 선언.
     */
    /*
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
*/
    /*

     */
    @Bean
    public Filter characterEncodingFilter() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);
        return characterEncodingFilter;
    }

    /* 톰캣에서 발생하는 익셉션은 컨트롤러 단에서 처리하기 위한 빈 */
    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {
    /*multipartException을 커스터마이징하여 처리
    * 람다를 활용하면 아래와 같이 작성 가능하다.*/
        EmbeddedServletContainerCustomizer embeddedServletContainerCustomizer
                = new EmbeddedServletContainerCustomizer() {
            @Override
            public void customize(ConfigurableEmbeddedServletContainer container) {

                container.addErrorPages(new ErrorPage(MultipartException.class, "/upload-error"));
            }
        } ;
        return embeddedServletContainerCustomizer;
    }

    @Bean
    public MessageSource messageSource() {

        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
