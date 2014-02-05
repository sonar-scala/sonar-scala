package com.buransky.plugins.scoverage.measure;

import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import java.util.Arrays;
import java.util.List;

public final class ScalaMetrics implements Metrics {
    public static final String STATEMENT_COVERAGE_KEY = "scoverage";
    public static final Metric STATEMENT_COVERAGE = new Metric.Builder(STATEMENT_COVERAGE_KEY,
            "Statement coverage", Metric.ValueType.PERCENT)
            .setDescription("Statement coverage by unit tests")
            .setDirection(Metric.DIRECTION_BETTER)
            .setQualitative(true)
            .setDomain(CoreMetrics.DOMAIN_TESTS)
            .setWorstValue(0.0)
            .setBestValue(100.0)
            .create();

    @Override
    public List<Metric> getMetrics() {
        return Arrays.asList(STATEMENT_COVERAGE);
    }
}
