package com.skrbt;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * @author liukunpeng@zhidaoauto.com
 * @version 1.0.0
 * @date 2025/11/25
 * @Description
 */
public class Detail {

    private static final String UNIFIED_CSV_FILENAME = "d:/lkp/mogo/skr/resource.csv";

    // 新增：解析详情页并生成CSV文件的方法
    public  static void parseAndSaveToCSV(String html, String resourceName, String collectionTime, String fileCountText, String totalSizeText) {
        Document doc = Jsoup.parse(html);

        String magnetLink = "";
        Element magnetPanel = doc.selectFirst("div#detail-magnet-panel");
        if (magnetPanel != null) {
            Element magnetElement = magnetPanel.selectFirst("a#magnet");
            if (magnetElement != null) {
                magnetLink = magnetElement.attr("href");
            } else {
                System.out.println("未获取详情");
                return ;
            }
        } else {
            System.out.println("未获取详情");
            return ;
        }

        List<FileInfo> fileList = new ArrayList<>();

// 修正：根据实际HTML结构查找文件列表面板
        Element fileListPanel = null;
        Elements panels = doc.select("div.panel");
        for (Element panel : panels) {
            Element titleElement = panel.selectFirst("div.panel-heading h3.panel-title");
            if (titleElement != null && "文件列表".equals(titleElement.text().trim())) {
                fileListPanel = panel;
                break;
            }
        }

        if (fileListPanel != null) {
            Element fileListBody = fileListPanel.selectFirst("div.panel-body");
            if (fileListBody != null) {
                // 修正：文件项是<li>元素而非div.file-item
                Elements fileItems = fileListBody.select("ul.list-unstyled li");
                for (Element item : fileItems) {
                    // 修正：文件名在第一个<span>中（包含图标后的文本）
                    Element nameSpan = item.selectFirst("span");
                    // 修正：文件大小在class为"badge"的<span>中
                    Element sizeSpan = item.selectFirst("span.badge");

                    if (nameSpan != null && sizeSpan != null) {
                        // 移除开头的非 breaking 空格并修剪，同时处理可能的 &nbsp; 字符串
                        String fileName = nameSpan.text().trim()
                                .replace("\u00a0", "")  // 移除 Unicode 非-breaking 空格
                                .replace("&nbsp", ""); // 移除 literal &nbsp; 字符串
                        String sizeText = sizeSpan.text().trim();
                        double sizeMB = parseSizeToMB(sizeText); // 转换为 MB
                        fileList.add(new FileInfo(fileName, sizeMB, sizeText));
                    }
                }
                System.out.println("成功解析 " + fileList.size() + " 个文件信息");
            } else {
                System.out.println("未找到文件列表面板内容区域");
            }
        } else {
            System.out.println("未找到文件列表面板");
        }

// 4. 取前10名并保存为CSV
        List<FileInfo> top10Files = fileList.stream()
                // 确保按大小降序排序（如果原逻辑未排序需添加此行）
                .sorted((f1, f2) -> Double.compare(f2.getSizeMB(), f1.getSizeMB())).limit(10).collect(java.util.stream.Collectors.toList());

// 4. 使用统一文件名（移除文件存在检查，改为追加模式）
        File csvFile = new File(UNIFIED_CSV_FILENAME);
        boolean fileExists = csvFile.exists();

        try (FileWriter writer = new FileWriter(csvFile, true)) { // 追加模式写入
            // 写入表头（仅文件不存在时）
            if (!fileExists) {
                StringBuilder header = new StringBuilder();
                header.append("磁力链接");
                for (int i = 1; i <= 3; i++) {
                    header.append(String.format(",文件%d名称,文件%d大小,文件%d大小(MB)", i, i, i));
                }
                header.append("\n");
                writer.write(header.toString());
            }

            // 构建数据行（一个磁力链接一行）
            StringBuilder dataRow = new StringBuilder();
            // 磁力链接列
            dataRow.append(magnetLink.replace("\"", "\"\""));

            // 追加10个文件的信息（不足10个则补空列）
            for (int i = 0; i < 3; i++) {
                if (i < top10Files.size()) {
                    FileInfo file = top10Files.get(i);
                    String fileName = file.getFileName().replace("\"", "\"\"");
                    String sizeText = file.getSizeText().replace("\"", "\"\"");
                    dataRow.append(String.format(",%s,%s,%s,%s,%s,%s,%.2f",resourceName,totalSizeText,fileCountText,collectionTime,
                            fileName, sizeText, file.getSizeMB()));
                } else {
                    // 不足10个文件时补空列
                    dataRow.append(",,,");
                }
            }
            dataRow.append("\n");

            // 写入数据行
            writer.write(dataRow.toString());
            System.out.println("CSV行写入成功: " + UNIFIED_CSV_FILENAME);
        } catch (IOException e) {
            System.out.println("生成CSV文件失败: " + e.getMessage());
            e.printStackTrace();
        }
// ... 现有代码 ...
    }

    // 新增：文件信息实体类
    private static class FileInfo {
        private String fileName;
        private double sizeMB;
        private String sizeText;

        public FileInfo(String fileName, double sizeMB, String sizeText) {
            this.fileName = fileName;
            this.sizeMB = sizeMB;
            this.sizeText = sizeText;
        }

        public String getFileName() { return fileName; }
        public double getSizeMB() { return sizeMB; }
        public String getSizeText() { return sizeText; }
    }


    public static String getDetailPageContent(String url) throws Exception {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            // 创建连接对象
            connection = (HttpURLConnection) new URL(url).openConnection();

            // 设置请求方法和请求头
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.5845.97 Safari/537.36 SE 2.X MetaSr 1.0");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
            connection.setRequestProperty("Cookie", "aywcUid=mvfj7MEQ4d_20251124203359; JSESSIONID=3A8DFCC7438B2CB5DC0A0DA0581611A7; fct=dHYyfFY0fG12Zmo3TUVRNGRfMjAyNTExMjQyMDMzNTl8MTc2NDI0NDc0Nzk5MHwxQTUxQQ==");
            connection.setRequestProperty("Referer", "https://skrbtmv.top/search?keyword=%E8%A7%A6%E4%B8%8D%E5%8F%AF%E5%8F%8A&sos=relevance&sofs=all&sot=all&soft=all&som=auto&p=1");

            // 设置超时时间
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            // 建立连接
            connection.connect();

            // 获取响应状态码
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 处理gzip压缩
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

                return response.toString();
            } else {
                throw new Exception("详情页请求失败，响应码: " + responseCode);
            }
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



    // 新增：将文件大小文本转换为 MB（辅助方法）
    private static double parseSizeToMB(String sizeText) {
        if (sizeText == null || sizeText.isEmpty()) {
            return 0;
        }
        sizeText = sizeText.replaceAll("[^0-9.]+", " ").trim().toLowerCase();
        double size = 0;
        try {
            // 提取数值部分
            double num = Double.parseDouble(sizeText.replaceAll("[a-zA-Z]", "").trim());
            // 根据单位转换为 MB
            if (sizeText.contains("gb")) {
                size = num * 1024; // 1GB = 1024MB
            } else if (sizeText.contains("mb")) {
                size = num;
            } else if (sizeText.contains("kb")) {
                size = num / 1024; // 1KB = 0.0009765625MB
            } else if (sizeText.contains("b")) {
                size = num / (1024 * 1024); // 1B = 0.000000953674MB
            }
        } catch (NumberFormatException e) {
            System.out.println("解析文件大小失败: " + sizeText);
        }
        return size;
    }

    // 新增：文件信息实体类（原有代码，保持不变）

}
