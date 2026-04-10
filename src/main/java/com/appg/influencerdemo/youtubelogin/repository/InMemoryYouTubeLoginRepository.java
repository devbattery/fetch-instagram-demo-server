package com.appg.influencerdemo.youtubelogin.repository;

import com.appg.influencerdemo.youtubelogin.domain.YouTubeChannelSnapshot;
import com.appg.influencerdemo.youtubelogin.domain.YouTubeLoginFlow;
import com.appg.influencerdemo.youtubelogin.domain.YouTubeLoginFlowResult;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryYouTubeLoginRepository {

    private final ConcurrentHashMap<String, YouTubeLoginFlow> flowsByState = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, YouTubeLoginFlowResult> resultsByFlowId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, YouTubeChannelSnapshot> channelsById = new ConcurrentHashMap<>();

    public void saveFlow(YouTubeLoginFlow flow) {
        flowsByState.put(flow.state(), flow);
    }

    public Optional<YouTubeLoginFlow> findFlowByState(String state) {
        return Optional.ofNullable(flowsByState.get(state));
    }

    public void removeFlow(String state) {
        flowsByState.remove(state);
    }

    public void saveResult(YouTubeLoginFlowResult result) {
        resultsByFlowId.put(result.flowId(), result);
    }

    public Optional<YouTubeLoginFlowResult> findResult(String flowId) {
        return Optional.ofNullable(resultsByFlowId.get(flowId));
    }

    public void saveChannel(YouTubeChannelSnapshot channel) {
        channelsById.put(channel.channelId(), channel);
    }

    public Optional<YouTubeChannelSnapshot> findChannel(String channelId) {
        return Optional.ofNullable(channelsById.get(channelId));
    }
}
