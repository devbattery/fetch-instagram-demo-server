package com.appg.influencerdemo.feed.service;

import com.appg.influencerdemo.feed.domain.InfluencerVideo;
import java.util.List;

@FunctionalInterface
public interface FeedOrderingPolicy {

    List<InfluencerVideo> order(List<InfluencerVideo> candidates);
}
