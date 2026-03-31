package com.appg.influencerdemo.instagramlogin.repository;

import com.appg.influencerdemo.instagramlogin.domain.InstagramAccountSnapshot;
import com.appg.influencerdemo.instagramlogin.domain.InstagramLoginFlow;
import com.appg.influencerdemo.instagramlogin.domain.InstagramLoginFlowResult;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryInstagramLoginRepository {

    private final ConcurrentHashMap<String, InstagramLoginFlow> flowsByState = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, InstagramLoginFlowResult> resultsByFlowId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, InstagramAccountSnapshot> accountsById = new ConcurrentHashMap<>();

    public void saveFlow(InstagramLoginFlow flow) {
        flowsByState.put(flow.state(), flow);
    }

    public Optional<InstagramLoginFlow> findFlowByState(String state) {
        return Optional.ofNullable(flowsByState.get(state));
    }

    public void removeFlow(String state) {
        flowsByState.remove(state);
    }

    public void saveResult(InstagramLoginFlowResult result) {
        resultsByFlowId.put(result.flowId(), result);
    }

    public Optional<InstagramLoginFlowResult> findResult(String flowId) {
        return Optional.ofNullable(resultsByFlowId.get(flowId));
    }

    public void saveAccount(InstagramAccountSnapshot account) {
        accountsById.put(account.accountId(), account);
    }

    public Optional<InstagramAccountSnapshot> findAccount(String accountId) {
        return Optional.ofNullable(accountsById.get(accountId));
    }
}
