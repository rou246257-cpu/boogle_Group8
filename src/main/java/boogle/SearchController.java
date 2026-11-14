
package boogle;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientException;

@Controller
public class SearchController {

    private final TavilySearchService tavilySearchService;

    public SearchController(TavilySearchService tavilySearchService) {
        this.tavilySearchService = tavilySearchService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam String query,
            Model model) {

        model.addAttribute("query", query);

        if (query == null || query.trim().isEmpty()) {
            model.addAttribute("error", "請輸入搜尋關鍵字。");
            return "index";
        }

        try {
            Map<String, String> results =
                    tavilySearchService.search(query);

            model.addAttribute("results", results);

        } catch (RestClientException e) {
            model.addAttribute(
                    "error",
                    "搜尋服務暫時無法連線，請稍後再試。"
            );
        }

        return "index";
    }
}
