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
package com.buransky.plugins.scoverage.widget

import org.sonar.api.web.{RubyRailsWidget, AbstractRubyTemplate}

/**
 * UI widget that can be added to the main dashboard to display overall statement coverage for the project.
 *
 * @author Rado Buransky
 */
class ScoverageWidget extends AbstractRubyTemplate with RubyRailsWidget {
  val getId = "scoverage"
  val getTitle = "Statement coverage"
  override val getTemplatePath = "/com/buransky/plugins/scoverage/widget.html.erb"
}
