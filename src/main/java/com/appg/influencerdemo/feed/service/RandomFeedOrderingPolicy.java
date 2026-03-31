package com.appg.influencerdemo.feed.service;

import com.appg.influencerdemo.feed.domain.InfluencerVideo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RandomFeedOrderingPolicy implements FeedOrderingPolicy {

    @Override
    public List<InfluencerVideo> order(List<InfluencerVideo> candidates) {
        List<InfluencerVideo> shuffled = new ArrayList<>(candidates);
        Collections.shuffle(shuffled);
        return shuffled;
    }
}
