package com.hiresphere.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiresphere.ai.dto.AiRequests;
import com.hiresphere.ai.dto.AiResults;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MockAiClientTest {

    private final MockAiClient client = new MockAiClient(new ObjectMapper());

    @Test
    void summaryIncludesRoleAndSkills() {
        String summary = client.generateSummary(new AiRequests.Summary("Backend Engineer", List.of("Java", "Kafka"), ""));
        assertThat(summary).contains("Backend Engineer").contains("Java");
    }

    @Test
    void screeningComputesMatchFromOverlappingSkills() {
        String resume = "{\"skills\":[{\"name\":\"Java\"},{\"name\":\"Spring\"}]}";
        String job = "{\"skills\":[\"Java\",\"Spring\",\"Kafka\",\"AWS\"]}";

        AiResults.Screening screening = client.screen(resume, job);

        assertThat(screening.matchScore()).isEqualTo(50);
        assertThat(screening.matchedSkills()).contains("Java", "Spring");
        assertThat(screening.missingSkills()).contains("Kafka", "AWS");
        assertThat(screening.recommendation()).isEqualTo("POSSIBLE_MATCH");
    }

    @Test
    void parseSearchExtractsStructuredFilters() {
        AiResults.SearchFilters filters = client.parseSearch("senior remote java roles in bangalore above 2000000");
        assertThat(filters.workMode()).isEqualTo("REMOTE");
        assertThat(filters.experienceLevel()).isEqualTo("SENIOR");
        assertThat(filters.skills()).contains("Java");
        assertThat(filters.location()).isEqualTo("Bangalore");
        assertThat(filters.salaryMin()).isNotNull();
    }

    @Test
    void analyzeProducesScoreAndFeedback() {
        String resume = "{\"completionScore\":70,\"summary\":\"Experienced engineer with impact\","
                + "\"skills\":[\"Java\",\"Spring\"],\"experiences\":[{}]}";
        AiResults.ResumeAnalysis analysis = client.analyzeResume(resume);
        assertThat(analysis.score()).isBetween(35, 100);
        assertThat(analysis.strengths()).isNotEmpty();
        assertThat(analysis.atsTips()).isNotEmpty();
    }

    @Test
    void mockReportsNotLive() {
        assertThat(client.isLive()).isFalse();
    }
}
