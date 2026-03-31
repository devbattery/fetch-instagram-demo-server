package com.appg.influencerdemo.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.appg.influencerdemo.common.exception.BusinessException;
import com.appg.influencerdemo.feed.domain.UserRole;
import com.appg.influencerdemo.feed.dto.FeedResponse;
import com.appg.influencerdemo.feed.repository.InMemoryFeedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FeedServiceTest {

    private FeedService feedService;

    @BeforeEach
    void setUp() {
        feedService = new FeedService(new InMemoryFeedRepository(), candidates -> candidates);
    }

    @Test
    void shouldReturnLimitedFeedForGeneralUser() {
        FeedResponse response = feedService.getFeed(UserRole.GENERAL_USER, 3);

        assertThat(response.viewerRole()).isEqualTo(UserRole.GENERAL_USER);
        assertThat(response.canLaunchInstagramVerification()).isFalse();
        assertThat(response.items()).hasSize(3);
        assertThat(response.items().getFirst().embedUrl()).contains("youtube.com/embed");
    }

    @Test
    void shouldRejectLimitOutOfRange() {
        assertThatThrownBy(() -> feedService.getFeed(UserRole.INFLUENCER, 0))
                .isInstanceOf(BusinessException.class);
    }
}
