package com.soccer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * RestTemplate配置类 - 修复版
 * 主要解决GB2312编码处理问题，确保澳客网站数据获取时不会出现乱码
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // 清理并重新配置消息转换器
        configureMessageConverters(restTemplate);
        
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 设置连接超时时间（毫秒）
        factory.setConnectTimeout(15000);
        // 设置读取超时时间（毫秒）
        factory.setReadTimeout(30000);
        return factory;
    }
    
    /**
     * 配置RestTemplate的消息转换器，重点处理字符串编码问题
     */
    private void configureMessageConverters(RestTemplate restTemplate) {
        // 创建新的消息转换器列表，保留原始转换器但替换StringHttpMessageConverter
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        
        // 遍历现有转换器
        for (HttpMessageConverter<?> converter : restTemplate.getMessageConverters()) {
            // 跳过StringHttpMessageConverter，我们将用自定义的替换
            if (!(converter instanceof StringHttpMessageConverter)) {
                converters.add(converter);
            }
        }
        
        // 创建自定义的StringHttpMessageConverter，专门处理GB2312编码
        CustomStringHttpMessageConverter stringConverter = new CustomStringHttpMessageConverter();
        
        // 添加到转换器列表的开头，确保优先使用
        converters.add(0, stringConverter);
        
        // 应用新的转换器列表
        restTemplate.setMessageConverters(converters);
    }
    
    /**
     * 自定义StringHttpMessageConverter，专门优化处理GB2312编码
     */
    private static class CustomStringHttpMessageConverter extends StringHttpMessageConverter {
        
        public CustomStringHttpMessageConverter() {
            // 支持的字符集列表，优先考虑中文编码
            List<Charset> charsets = new ArrayList<>();
            charsets.add(Charset.forName("GB2312"));
            charsets.add(Charset.forName("GBK"));
            charsets.add(StandardCharsets.UTF_8);
            
            // 设置支持的字符集
            super.setDefaultCharset(Charset.forName("GB2312"));
            super.setWriteAcceptCharset(true);
          
        }
        
        @Override
        protected String readInternal(Class<? extends String> clazz, org.springframework.http.HttpInputMessage inputMessage) throws IOException {
            // 首先尝试从响应头中检测编码
            Charset charset = detectCharsetFromHeaders(inputMessage);
            
            // 使用Reader进行更可靠的字符流读取
            try (Reader reader = new InputStreamReader(inputMessage.getBody(), charset)) {
                StringBuilder sb = new StringBuilder();
                char[] buffer = new char[4096];
                int bytesRead;
                
                while ((bytesRead = reader.read(buffer)) != -1) {
                    sb.append(buffer, 0, bytesRead);
                }
                
                String content = sb.toString();
                
                // 二次检查：如果内容中明确包含gb2312声明，且当前不是用GB2312解码的，重新解码
                if (content.contains("gb2312") && !charset.name().equalsIgnoreCase("GB2312")) {
                    // 重置输入流并使用GB2312重新读取
                    try (Reader gb2312Reader = new InputStreamReader(
                            inputMessage.getBody(), Charset.forName("GB2312"))) {
                        StringBuilder gb2312Sb = new StringBuilder();
                        char[] gb2312Buffer = new char[4096];
                        int gb2312BytesRead;
                        
                        while ((gb2312BytesRead = gb2312Reader.read(gb2312Buffer)) != -1) {
                            gb2312Sb.append(gb2312Buffer, 0, gb2312BytesRead);
                        }
                        
                        content = gb2312Sb.toString();
                    }
                }
                
                return content;
            }
        }
        
        /**
         * 从响应头中检测字符编码
         */
        private Charset detectCharsetFromHeaders(org.springframework.http.HttpInputMessage inputMessage) {
            // 默认使用GB2312，因为我们主要处理澳客网站
            Charset charset = Charset.forName("GB2312");
            
            // 获取Content-Type头
            org.springframework.http.MediaType contentType = inputMessage.getHeaders().getContentType();
            
            if (contentType != null) {
                // 如果响应头中明确指定了字符集，则使用该字符集
                if (contentType.getCharset() != null) {
                    charset = contentType.getCharset();
                } else {
                    // 检查Content-Type中是否包含编码相关信息
                    String contentTypeStr = contentType.toString().toLowerCase();
                    if (contentTypeStr.contains("charset=gb2312")) {
                        charset = Charset.forName("GB2312");
                    } else if (contentTypeStr.contains("charset=utf-8")) {
                        charset = StandardCharsets.UTF_8;
                    } else if (contentTypeStr.contains("charset=gbk")) {
                        charset = Charset.forName("GBK");
                    }
                }
            }
            
            return charset;
        }
    }
}