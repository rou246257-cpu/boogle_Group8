
package boogle;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Service
public class TavilySearchService {

    @Value("${tavily.apiKey}")
    private String apiKey;

    private final RestTemplate restTemplate;

    // 建立 Tavily 搜尋服務
    public TavilySearchService() {

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        // 避免網路異常時一直等待
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        restTemplate = new RestTemplate(factory);
    }

    public Map<String, String> search(String query) {

        Map<String, String> results = new LinkedHashMap<>();

        // 自動補上「獎學金」
        String searchQuery = query.trim();

        if (!searchQuery.contains("獎學金")) {
            searchQuery = searchQuery + " 獎學金";
        }

        // Tavily API 網址
        String url = "https://api.tavily.com/search";

        // 建立 API 請求資料
        Map<String, Object> request = new LinkedHashMap<>();

        request.put("api_key", apiKey);
        request.put("query", searchQuery);
        request.put("max_results", 5);

        // 呼叫 Tavily API
        Map<?, ?> response = restTemplate.postForObject(
                url,
                request,
                Map.class
        );

        // 解析搜尋結果
        if (response != null
                && response.get("results") instanceof List<?>) {

            List<?> items = (List<?>) response.get("results");

            for (Object item : items) {

                if (item instanceof Map<?, ?>) {

                    Map<?, ?> result = (Map<?, ?>) item;

                    Object titleObject = result.get("title");
                    Object urlObject = result.get("url");

                    if (titleObject == null || urlObject == null) {
                        continue;
                    }

                    String title = titleObject.toString();
                    String link = urlObject.toString();

                    // 檢查相關性分數
                    Object scoreObject = result.get("score");

                    double score = 0.0;

                    if (scoreObject instanceof Number) {
                        score = ((Number) scoreObject).doubleValue();
                    }

                    // 保留相關性較高的搜尋結果
                    if (score >= 0.5
                            && link.startsWith("https://")) {

                        results.put(title, link);
                    }
                }
            }
        }

        return results;
    }
}
