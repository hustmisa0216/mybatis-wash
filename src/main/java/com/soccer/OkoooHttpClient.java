package com.soccer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.http.MediaType; // 添加缺少的导入语句
import java.nio.charset.Charset;

/**
 * Okooo网站HTTP客户端，用于获取竞彩数据
 */
@Component
public class OkoooHttpClient {

    private final RestTemplate restTemplate;

    public OkoooHttpClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 获取指定日期的竞彩数据
     * @param date 日期，格式为yyyy-MM-dd
     * @return 网页内容
     */
    public String getJingCaiData(String date) {
        String url = "https://www.okooo.cn/jingcai/" + date + "/";
        return fetchUrl(url,"GB2312");
    }
    
    /**
     * 获取比赛详细数据
     * @param url 比赛详细数据URL
     * @return 网页内容
     */
    public String getMatchDetailData(String url) {
        return fetchUrl(url,StandardCharsets.UTF_8.name());
    }
    
    /**
     * 通用的URL获取方法
     * @param url 要获取的URL
     * @return 网页内容
     */
    private String fetchUrl(String url,String charset) {
        // 创建HTTP头
        HttpHeaders headers = createHeaders();

        // 创建HTTP实体
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // 发送GET请求
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, byte[].class);

            // 检查响应状态
            if (response.getStatusCode().is2xxSuccessful()) {
                // 获取响应头中的Content-Type
                // 假设原代码中此处是获取响应头中的Content-Type
                org.springframework.http.MediaType contentType = response.getHeaders().getContentType();
                System.out.println("响应Content-Type: " + contentType);

                // 获取响应体
                byte[] bodyBytes = response.getBody();
                System.out.println("响应字节长度: " + (bodyBytes != null ? bodyBytes.length : 0));
                
                // 尝试使用不同编码解析响应内容
                if (bodyBytes != null) {
                    String content = new String(bodyBytes, Charset.forName(charset));
                    if(StringUtils.isNotBlank(content)) {
                        return content;
                    }
                    

                }
            }
        } catch (Exception e) {
            throw new RuntimeException("获取竞彩数据失败: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 创建HTTP请求头，包含必要的headers信息
     * @return 配置好的HttpHeaders对象
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        
        // 设置请求头信息
        headers.add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        //headers.add("Accept-Encoding", "gzip, deflate, br");
        headers.add("Accept-Language", "zh-CN,zh;q=0.9");
        headers.add("Sec-Ch-Ua", "\"Not)A;Brand\";v=\"24\", \"Chromium\";v=\"116\"");
        headers.add("Sec-Ch-Ua-Mobile", "?0");
        headers.add("Sec-Ch-Ua-Platform", "\"Windows\"");
        headers.add("Sec-Fetch-Dest", "document");
        headers.add("Sec-Fetch-Mode", "navigate");
        headers.add("Sec-Fetch-Site", "none");
        headers.add("Sec-Fetch-User", "?1");
        headers.add("Upgrade-Insecure-Requests", "1");
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.5845.97 Safari/537.36 SE 2.X MetaSr 1.0");
        
        // 设置Cookie（从用户提供的headers中提取）
        headers.add("Cookie","Hm_lvt_213d524a1d07274f17dfa17b79db318f=1758266782; HMACCOUNT=80FDC7871CDA93C6; _ga=GA1.1.2086009437.1758266782; PHPSESSID=e0a9ee427e8139271e113191a9af4d27318edac3; pm=; FirstURL=www.okooo.cn/; FirstOKURL=https%3A//www.okooo.cn/jingcai/; First_Source=www.okooo.cn; acw_tc=ac11000117585273236652078e65efecd912a523b47e734ec351f84a016ec0; Hm_lpvt_213d524a1d07274f17dfa17b79db318f=1758527310; LStatus=N; LoginStr=%7B%22welcome%22%3A%22%u60A8%u597D%uFF0C%u6B22%u8FCE%u60A8%22%2C%22login%22%3A%22%u767B%u5F55%22%2C%22register%22%3A%22%u6CE8%u518C%22%2C%22TrustLoginArr%22%3A%7B%22alipay%22%3A%7B%22LoginCn%22%3A%22%22%7D%2C%22tenpay%22%3A%7B%22LoginCn%22%3A%22%u8D22%u4ED8%u901A%22%7D%2C%22weibo%22%3A%7B%22LoginCn%22%3A%22%u65B0%u6D6A%u5FAE%u535A%22%7D%2C%22renren%22%3A%7B%22LoginCn%22%3A%22%22%7D%2C%22baidu%22%3A%7B%22LoginCn%22%3A%22%22%7D%2C%22snda%22%3A%7B%22LoginCn%22%3A%22%22%7D%7D%2C%22userlevel%22%3A%22%22%2C%22flog%22%3A%22hidden%22%2C%22UserInfo%22%3A%22%22%2C%22loginSession%22%3A%22___GlobalSession%22%7D; _ga_PHV6HH5CV1=GS2.1.s1758527309$o8$g1$t1758527310$j59$l0$h790582317");
        
        return headers;
    }
}