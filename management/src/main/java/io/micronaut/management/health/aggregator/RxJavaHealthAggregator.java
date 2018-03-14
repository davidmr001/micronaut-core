/*
 * Copyright 2017 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.management.health.aggregator;

import io.micronaut.context.annotation.Requires;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.micronaut.context.annotation.Requires;
import io.micronaut.management.endpoint.health.HealthEndpoint;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import org.reactivestreams.Publisher;

import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>Default implementation of {@link HealthAggregator} that creates
 * a {status: , description: (optional) , details: } response. The top level object
 * represents the most severe status found in the provided health results,
 * or {@link HealthStatus#UNKNOWN} if none found. All registered indicators
 * have their own {status: , description: (optional , details: } object, keyed by the
 * name of the {@link HealthResult} defined inside of the details of the top
 * level object.
 *
 * Example:
 * [status: "UP, details: [diskSpace: [status: UP, details: [:]], cpuUsage: ...]]</p>
 *
 * @author James Kleeh
 * @author Graeme Rocher
 * @since 1.0
 */
@Singleton
@Requires(beans = HealthEndpoint.class)
public class RxJavaHealthAggregator implements HealthAggregator<Map<String, Object>> {

    @Override
    public Publisher<Map<String, Object>> aggregate(HealthIndicator[] indicators) {
        Flowable<HealthResult> results = aggregateResults(indicators);
        Single<Map<String, Object>> result = results.toList().map(list -> {
            HealthStatus overallStatus = calculateOverallStatus(list);
            return buildResult(overallStatus, aggregateDetails(list));
        });
        return result.toFlowable();
    }

    @Override
    public Publisher<HealthResult> aggregate(String name, Publisher<HealthResult> results) {
        Single<HealthResult> result = Flowable.fromPublisher(results).toList().map((list) -> {
            HealthStatus overallStatus = calculateOverallStatus(list);
            Object details = aggregateDetails(list);
            return HealthResult.builder(name, overallStatus).details(details).build();
        });
        return result.toFlowable();
    }

    protected HealthStatus calculateOverallStatus(List<HealthResult> results) {
        return results.stream()
                .map(HealthResult::getStatus)
                .distinct()
                .sorted()
                .reduce((a, b) -> b)
                .orElse(HealthStatus.UNKNOWN);
    }

    protected Flowable<HealthResult> aggregateResults(HealthIndicator[] indicators) {
        return Flowable.merge(
                Arrays.stream(indicators)
                        .map(HealthIndicator::getResult)
                        .collect(Collectors.toList())
        );
    }

    protected Object aggregateDetails(List<HealthResult> results) {
        Map<String, Object> details = new HashMap<>(results.size());
        results.forEach( r -> details.put(r.getName(), buildResult(r.getStatus(), r.getDetails())));
        return details;
    }

    protected Map<String, Object> buildResult(HealthStatus status, Object details) {
        Map<String, Object> healthStatus = new LinkedHashMap<>(3);
        healthStatus.put("status", status.getName());
        status.getDescription().ifPresent(description -> healthStatus.put("description", description));
        if(details != null) {
            healthStatus.put("details", details);
        }
        return healthStatus;
    }
}
