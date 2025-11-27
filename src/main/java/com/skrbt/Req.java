package com.skrbt;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/11/24
 * @Description
 */
public class Req {



    private static final String BASE_URL="https://skrbtmv.top";

    public static void main(String[] args) {
        // 检查是否传入keyword参数


        String keyword = "哪吒之魔童降世" ;
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            // 对keyword进行URL编码
            String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");

            // 构建完整请求URL
            String url = "https://skrbtmv.top/search?keyword=" + encodedKeyword + "&sos=date&sofs=all&sot=all&soft=all&som=auto&p=1";

            // 创建连接对象
            connection = (HttpURLConnection) new URL(url).openConnection();

            // 设置请求方法
            connection.setRequestMethod("GET");

            // 设置请求头信息
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.5845.97 Safari/537.36 SE 2.X MetaSr 1.0");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
            connection.setRequestProperty("Cookie", "aywcUid=mvfj7MEQ4d_20251124203359; JSESSIONID=3A8DFCC7438B2CB5DC0A0DA0581611A7; fct=dHYyfFY0fG12Zmo3TUVRNGRfMjAyNTExMjQyMDMzNTl8MTc2NDI0NDc0Nzk5MHwxQTUxQQ==");
            connection.setRequestProperty("Referer", "https://skrbtmv.top/search?keyword=%E5%93%AA%E5%90%92%E4%B9%8B%E9%AD%94%E7%AB%A5%E9%99%8D%E4%B8%96&sos=relevance&sofs=all&sot=all&soft=all&som=auto&p=1");

            // 设置超时时间
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            // 建立连接
            connection.connect();

            // 获取响应状态码
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 获取输入流，处理gzip压缩
                InputStream inputStream = connection.getInputStream();
                if ("gzip".equals(connection.getContentEncoding())) {
                    inputStream = new GZIPInputStream(inputStream);
                }

                // 读取响应内容
                reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // 输出响应内容
                parseResourceInfo(response.toString());
            } else {
                System.out.println("请求失败，响应码: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // 添加解析资源信息的方法
    // 添加解析资源信息的方法
    private static void parseResourceInfo(String html) {

        Document doc = Jsoup.parse(html);
        Elements resourceLists = doc.select("ul.list-unstyled");

        // 检查是否获取到资源列表
        if (resourceLists.isEmpty()) {
            System.out.println("未获取到资源列表，程序停止");
            System.exit(1); // 停止程序
        }

        for (Element list : resourceLists) {
            // 获取资源标题和编码
            Element titleLink = list.selectFirst("li > a.rrt.common-link");
            if (titleLink == null) continue;

            String resourceName = titleLink.text();
            String detailPath = titleLink.attr("href");
            String resourceCode = detailPath.replace("/detail/", "");

            // 新增：提取资源元数据（总大小、文件数量、收录时间）
            Element metaContainer = list.selectFirst("li.rrmi");
            if (metaContainer == null) {
                System.out.println("跳过缺少元数据的资源: " + resourceName);
                continue;
            }

            Elements metaSpans = metaContainer.select("span.rrmiv");
            if (metaSpans.size() < 3) {
                System.out.println("跳过元数据不完整的资源: " + resourceName);
                continue;
            }

            String totalSizeText = metaSpans.get(0).text().trim();  // 总大小文本
            String fileCountText = metaSpans.get(1).text().trim();  // 文件数量文本
            String collectionTime = metaSpans.get(2).text().trim(); // 收录时间

            double totalSizeMB = 0;
            try {
                // 使用正则提取数字和单位（如"45MB" -> 数字"45"，单位"MB"）
                // 使用正则提取数字和单位（允许数字和单位间有空格）
                Pattern pattern = Pattern.compile("([\\d.]+)\\s*([A-Za-z]+)");
                Matcher matcher = pattern.matcher(totalSizeText);
                if (matcher.find()) {
                    double size = Double.parseDouble(matcher.group(1));
                    String unit = matcher.group(2).toUpperCase();

                    // 转换为MB单位
                    switch (unit) {
                        case "KB":
                            totalSizeMB = size / 1024;  // KB转MB
                            break;
                        case "MB":
                            totalSizeMB = size;         // 直接使用MB值
                            break;
                        case "GB":
                            totalSizeMB = size * 1024;   // GB转MB
                            break;
                        default:
                            System.out.println("未知大小单位: " + unit + "，跳过资源: " + resourceName);
                            continue;
                    }
                } else {
                    System.out.println("无法解析大小格式: " + totalSizeText + "，跳过资源: " + resourceName);
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("大小格式解析错误: " + totalSizeText + "，跳过资源: " + resourceName);
                continue;
            }
            if (totalSizeMB < 50) {
                continue;
            }

            try {
                String detailContent = Detail.getDetailPageContent(BASE_URL + detailPath);
                // 修改：传递新增的元数据到详情页处理方法
                Detail.parseAndSaveToCSV(detailContent, resourceName, collectionTime, fileCountText, totalSizeText);
                Thread.sleep(100);
            } catch (Exception e) {
                System.out.println("获取详情页失败: " + e.getMessage());
            }
        }
    }

}


