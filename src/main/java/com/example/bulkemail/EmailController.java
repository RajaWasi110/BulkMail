package com.example.bulkemail;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@Controller
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/")
    public String index() {
        return "index"; // Thymeleaf template
    }

    // Simple health check for Railway
    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "BulkMail is live!";
    }

    @PostMapping("/send")
    public String sendEmails(
            @RequestParam String subject,
            @RequestParam String body,
            @RequestParam(required = false) String recipientsManual,
            @RequestParam(required = false) MultipartFile recipientsFile,
            Model model) {

        try {
            // Send emails and get failed recipients
            List<String> failed = emailService.sendEmails(subject, body, recipientsManual, recipientsFile);

            // Count total recipients
            int totalRecipients = 0;
            if (recipientsManual != null && !recipientsManual.isEmpty()) {
                totalRecipients += recipientsManual.split("[,;\\n]+").length;
            }
            if (recipientsFile != null && !recipientsFile.isEmpty()) {
                totalRecipients += countLines(recipientsFile);
            }

            int successCount = totalRecipients - failed.size();

            model.addAttribute("successCount", successCount);
            model.addAttribute("failed", failed);

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        return "index";
    }

    // Utility method to count lines in uploaded file
    private int countLines(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            int lines = 0;
            while (reader.readLine() != null) lines++;
            return lines;
        } catch (Exception e) {
            return 0;
        }
    }
}
