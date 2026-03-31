package com.appg.influencerdemo.feed.repository;

import com.appg.influencerdemo.feed.domain.InfluencerVideo;
import java.util.List;

public interface FeedRepository {

    List<InfluencerVideo> findAll();
}
