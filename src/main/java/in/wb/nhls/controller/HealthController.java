package in.wb.nhls.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping("/api/v1")
public class HealthController {
     
    @GetMapping("/health")
    public Map<String, String> healthCheck() {

        return Map.of(
                "status", "UP",
                "message", "বাংলা হোটেল ও রেস্তোরাঁ লাইসেন্সিং সিস্টেম চালু আছে",
                "application", "পশ্চিমবঙ্গ হসপিটালিটি লাইসেন্সিং সিস্টেম"
        );
    }
}
