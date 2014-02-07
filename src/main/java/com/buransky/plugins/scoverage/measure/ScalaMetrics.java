/*
 * Sonar Scoverage Plugin
 * Copyright (C) 2013 Rado Buransky
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.buransky.plugins.scoverage.measure;

import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import java.util.Arrays;
import java.util.List;

/**
 * Statement coverage metric definition.
 *
 * @author Rado Buransky
 */
public final class ScalaMetrics implements Metrics {
    private static final String STATEMENT_COVERAGE_KEY = "scoverage";
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
