package com.appg.influencerdemo.youtubelogin.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class YouTubeShortsClassifierTest {

    private final YouTubeShortsClassifier classifier = new YouTubeShortsClassifier();

    @Test
    void 세로_비율과_3분_이하면_shorts_후보로_본다() {
        boolean result = classifier.isShortsCandidate(120L, 1080, 1920, "none", 180);

        assertThat(result).isTrue();
    }

    @Test
    void 가로_비율이면_길이가_짧아도_shorts_후보에서_제외한다() {
        boolean result = classifier.isShortsCandidate(120L, 1920, 1080, "none", 180);

        assertThat(result).isTrue();
    }

    @Test
    void 회전_메타데이터가_있으면_세로_영상으로_정상_보정한다() {
        boolean result = classifier.isShortsCandidate(120L, 1920, 1080, "clockwise", 180);

        assertThat(result).isTrue();
    }

    @Test
    void 비율_메타데이터가_없으면_기존처럼_길이만으로_후보를_판단한다() {
        boolean result = classifier.isShortsCandidate(120L, null, null, null, 180);

        assertThat(result).isTrue();
    }
}
