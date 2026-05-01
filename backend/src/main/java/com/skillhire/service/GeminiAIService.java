package com.skillhire.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Gemini AI Service - Production-ready integration
 * API key loaded securely from environment variable GEMINI_API_KEY.
 * Never hardcode secrets.
 */
@Service
@Slf4j
public class GeminiAIService {

    @Value("${app.gemini.api.key}")
    private String apiKey;

    @Value("${app.gemini.api.url}")
    private String apiUrl;

    @Value("${app.gemini.timeout.connect:30}")
    private int connectTimeout;

    @Value("${app.gemini.timeout.read:60}")
    private int readTimeout;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private OkHttpClient buildHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Core method: send a message to Gemini and return the response
     */
    public String chat(String userMessage, String systemContext) {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("your_gemini_api_key_here")) {
            log.warn("Gemini API key not configured");
            return "AI service is not configured. Please contact support.";
        }

        try {
            String fullPrompt = systemContext != null
                    ? systemContext + "\n\nUser: " + userMessage
                    : userMessage;

            String requestBody = buildRequestBody(fullPrompt);

            Request request = new Request.Builder()
                    .url(apiUrl + "?key=" + apiKey)
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json; charset=utf-8")))
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = buildHttpClient().newCall(request).execute()) {
                if (response.code() == 429) {
                    log.warn("Gemini API rate limit hit");
                    return "I'm receiving too many requests right now. Please wait a moment and try again.";
                }

                if (response.code() == 403) {
                    log.error("Gemini API key invalid or unauthorized");
                    return "AI service authentication failed. Please contact support.";
                }

                if (!response.isSuccessful()) {
                    log.error("Gemini API error: HTTP {}", response.code());
                    return "AI service is temporarily unavailable. Please try again later.";
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                return extractTextFromResponse(responseBody);
            }
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                log.error("Gemini API timeout: {}", e.getMessage());
                return "The AI request timed out. Please try with a shorter message.";
            }
            log.error("Gemini API IOException: {}", e.getMessage());
            return "I encountered a connection error. Please try again.";
        } catch (Exception e) {
            log.error("Unexpected error calling Gemini API: {}", e.getMessage(), e);
            return "An unexpected error occurred. Please try again.";
        }
    }

    /**
     * Analyze a resume and return ATS score + feedback
     */
    public String analyzeResume(String resumeText) {
        if (resumeText == null || resumeText.isBlank()) {
            return "No resume text provided to analyze.";
        }

        String prompt = """
                You are an expert ATS (Applicant Tracking System) and HR professional.
                Analyze the following resume and provide:
                1. ATS Score out of 100
                2. Key Strengths (3-5 points)
                3. Areas for Improvement (3-5 points)
                4. Missing Important Sections
                5. Skill Gap Suggestions
                6. Overall Recommendation
                
                Resume:
                """ + resumeText.substring(0, Math.min(resumeText.length(), 4000)) + """
                
                Format your response with clear headers and bullet points.
                """;

        return chat(prompt, null);
    }

    /**
     * Generate a tailored cover letter
     */
    public String generateCoverLetter(String candidateName, String candidateSkills,
                                       String jobTitle, String companyName, String jobDescription) {
        String prompt = String.format("""
                Generate a professional, compelling cover letter for:
                - Candidate: %s
                - Key Skills: %s
                - Applying for: %s at %s
                - Job Description: %s
                
                Instructions:
                - Make it personalized and enthusiastic
                - Highlight relevant skills from the job description
                - Keep it to 3-4 paragraphs
                - Use professional business letter format
                - Start with "Dear Hiring Manager,"
                """, candidateName, candidateSkills, jobTitle, companyName,
                jobDescription != null ? jobDescription.substring(0, Math.min(jobDescription.length(), 1000)) : "N/A");

        return chat(prompt, null);
    }

    /**
     * Mock interview preparation
     */
    public String prepareInterview(String jobTitle, String userMessage) {
        String systemContext = String.format("""
                You are an expert HR interviewer helping a candidate prepare for a %s interview.
                - Ask relevant technical and behavioral questions
                - Evaluate answers and provide constructive feedback
                - Be encouraging but realistic
                - If the user asks for a question, provide one interview question
                - If they provide an answer, evaluate it with the STAR method feedback
                """, jobTitle);

        return chat(userMessage, systemContext);
    }

    /**
     * Get job recommendations based on skills
     */
    public String getJobRecommendations(String skills, String experience) {
        String prompt = String.format("""
                Based on these skills: %s
                And %s years of experience:
                
                Recommend:
                1. Top 5 job roles that match these skills
                2. Companies in India known to hire for these skills
                3. 3-5 skills to add for career growth
                4. Expected salary range in India (INR LPA)
                5. Career progression path (3-5 years)
                
                Be specific and actionable. Focus on the Indian job market.
                """, skills, experience);

        return chat(prompt, null);
    }

    /**
     * Career assistant chatbot
     */
    public String careerChat(String userMessage) {
        String systemContext = """
                You are SkillBot, an AI career assistant for SkillHire AI job portal.
                You help candidates with:
                - Job search strategies
                - Resume improvement tips
                - Interview preparation
                - Skill development advice
                - Salary negotiation (especially Indian market)
                - Career transitions
                
                Be friendly, professional, and provide actionable advice.
                Keep responses concise but comprehensive. Use bullet points when helpful.
                """;

        return chat(userMessage, systemContext);
    }

    /**
     * Build Gemini API request body using Jackson for safe JSON serialization
     */
    private String buildRequestBody(String prompt) throws IOException {
        // Use ObjectMapper for proper JSON escaping — no manual string replacement
        ObjectMapper mapper = new ObjectMapper();
        String escapedPrompt = mapper.writeValueAsString(prompt);
        // writeValueAsString adds surrounding quotes, so strip them
        escapedPrompt = escapedPrompt.substring(1, escapedPrompt.length() - 1);

        return """
                {
                    "contents": [{
                        "parts": [{
                            "text": "%s"
                        }]
                    }],
                    "generationConfig": {
                        "temperature": 0.7,
                        "topK": 40,
                        "topP": 0.95,
                        "maxOutputTokens": 2048
                    },
                    "safetySettings": [
                        {"category": "HARM_CATEGORY_HARASSMENT", "threshold": "BLOCK_MEDIUM_AND_ABOVE"},
                        {"category": "HARM_CATEGORY_HATE_SPEECH", "threshold": "BLOCK_MEDIUM_AND_ABOVE"}
                    ]
                }
                """.formatted(escapedPrompt);
    }

    /**
     * Extract text from Gemini API response safely
     */
    private String extractTextFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // Check for error response
            if (root.has("error")) {
                String errorMsg = root.path("error").path("message").asText("Unknown error");
                log.error("Gemini API returned error: {}", errorMsg);
                return "AI service error: " + errorMsg;
            }

            JsonNode candidates = root.path("candidates");
            if (candidates.isEmpty()) {
                log.warn("Gemini returned empty candidates array");
                return "No response generated. Please rephrase your message and try again.";
            }

            String text = candidates.get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            if (text == null || text.isBlank()) {
                return "I couldn't generate a response. Please try again.";
            }

            return text;
        } catch (Exception e) {
            log.error("Error parsing Gemini response: {}", e.getMessage());
            return "Error processing AI response. Please try again.";
        }
    }
}
