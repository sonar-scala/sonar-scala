package com.buransky.plugins.scoverage.widget;

import org.sonar.api.web.AbstractRubyTemplate;
import org.sonar.api.web.RubyRailsWidget;

public class ScoverageWidget extends AbstractRubyTemplate implements RubyRailsWidget {

    public String getId() {
        return "scoverage";
    }

    public String getTitle() {
        return "Statement coverage";
    }

    @Override
    protected String getTemplatePath() {
        return "/com/buransky/plugins/scoverage/widget.html.erb";
    }
}
