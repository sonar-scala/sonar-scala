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
package com.buransky.plugins.scoverage.xml.data

object XmlReportFile1 {
  val data =
    """<?xml version="1.0" encoding="UTF-8"?>
      |<scoverage statement-rate="24.53" branch-rate="33.33" version="1.0" timestamp="1391478578154">
      |    <packages>
      |        <package name="aaa" statement-rate="26.00">
      |            <classes>
      |                <class name="MyServiceClientError" filename="/aaa/ErrorCode.scala" statement-rate="0.00" branch-rate="100.00">
      |                    <methods>
      |                        <method name="aaa/MyServiceClientError/&lt;none&gt;" statement-rate="0.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="MyServiceClientError" method="&lt;none&gt;" start="1425" line="51" symbol="aaa.StructuredErrorCode.error" tree="Apply" branch="false" invocation-count="0">
      |                                    MyServiceClientError.this.error(&quot;zipcodeinvalid&quot;)
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |                <class name="$anon" filename="/aaa/Graph.scala" statement-rate="0.00" branch-rate="100.00">
      |                    <methods>
      |                        <method name="aaa/$anon/apply" statement-rate="0.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="$anon" method="apply" start="526" line="16" symbol="&lt;nosymbol&gt;" tree="Literal" branch="false" invocation-count="0">
      |                                    2
      |</statement>
      |                                <statement
      |                                package="aaa" class="$anon" method="apply" start="600" line="17" symbol="&lt;nosymbol&gt;" tree="Literal" branch="false" invocation-count="0">
      |                                    3
      |</statement>
      |                                <statement
      |                                package="aaa" class="$anon" method="apply" start="655" line="18" symbol="scala.Some.apply" tree="Apply" branch="false" invocation-count="0">
      |                                    scala.Some.apply[String](&quot;One&quot;)
      |</statement>
      |                                <statement
      |                                package="aaa" class="$anon" method="apply" start="443" line="15" symbol="aaa.MakeRectangleModelFromFile.$anon.&lt;init&gt;" tree="Apply" branch="false" invocation-count="0">
      |                                    new $anon()
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |                <class name="MyServiceLogicError" filename="/aaa/ErrorCode.scala" statement-rate="100.00" branch-rate="100.00">
      |                    <methods>
      |                        <method name="aaa/MyServiceLogicError/&lt;none&gt;" statement-rate="100.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="MyServiceLogicError" method="&lt;none&gt;" start="1686" line="59" symbol="aaa.StructuredErrorCode.error" tree="Apply" branch="false" invocation-count="1">
      |                                    MyServiceLogicError.this.error(&quot;logicfailed&quot;)
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |                <class name="StructuredErrorCode" filename="/aaa/ErrorCode.scala" statement-rate="64.29" branch-rate="50.00">
      |                    <methods>
      |                        <method name="aaa/StructuredErrorCode/toString" statement-rate="100.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="toString" start="321" line="16" symbol="java.lang.Object.toString" tree="Apply" branch="false" invocation-count="4">
      |                                    StructuredErrorCode.this.parent.toString()
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="toString" start="346" line="17" symbol="java.lang.Object.==" tree="Apply" branch="false" invocation-count="4">
      |                                    p.==(&quot;&quot;)
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="toString" start="355" line="17" symbol="&lt;nosymbol&gt;" tree="Literal" branch="false" invocation-count="1">
      |                                    &quot;&quot;
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="toString" start="355" line="17" symbol="&lt;nosymbol&gt;" tree="Block" branch="true" invocation-count="1">
      |                                    {
      |  scoverage.Invoker.invoked(8, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  &quot;&quot;
      |}
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="toString" start="363" line="17" symbol="java.lang.String.+" tree="Apply" branch="false" invocation-count="3">
      |                                    p.+(&quot;-&quot;)
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="toString" start="363" line="17" symbol="&lt;nosymbol&gt;" tree="Block" branch="true" invocation-count="3">
      |                                    {
      |  scoverage.Invoker.invoked(10, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  p.+(&quot;-&quot;)
      |}
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="toString" start="374" line="17" symbol="aaa.StructuredErrorCode.name" tree="Select" branch="false" invocation-count="4">
      |                                    StructuredErrorCode.this.name
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="toString" start="341" line="17" symbol="java.lang.String.+" tree="Apply" branch="false" invocation-count="4">
      |                                    if ({
      |  scoverage.Invoker.invoked(7, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  p.==(&quot;&quot;)
      |})
      |  {
      |    scoverage.Invoker.invoked(9, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |    {
      |      scoverage.Invoker.invoked(8, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |      &quot;&quot;
      |    }
      |  }
      |else
      |  {
      |    scoverage.Invoker.invoked(11, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |    {
      |      scoverage.Invoker.invoked(10, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |      p.+(&quot;-&quot;)
      |    }
      |  }.+({
      |  scoverage.Invoker.invoked(12, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  StructuredErrorCode.this.name
      |})
      |</statement>
      |                            </statements>
      |                        </method>
      |                        <method name="aaa/StructuredErrorCode/is" statement-rate="0.00" branch-rate="0.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="is" start="210" line="9" symbol="java.lang.Object.==" tree="Apply" branch="false" invocation-count="0">
      |                                    errorCode.==(this)
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="is" start="235" line="10" symbol="&lt;nosymbol&gt;" tree="Literal" branch="false" invocation-count="0">
      |                                    true
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="is" start="235" line="10" symbol="&lt;nosymbol&gt;" tree="Block" branch="true" invocation-count="0">
      |                                    {
      |  scoverage.Invoker.invoked(2, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  true
      |}
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="is" start="255" line="12" symbol="aaa.ErrorCode.is" tree="Apply" branch="false" invocation-count="0">
      |                                    StructuredErrorCode.this.parent.is(errorCode)
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="is" start="255" line="12" symbol="&lt;nosymbol&gt;" tree="Block" branch="true" invocation-count="0">
      |                                    {
      |  scoverage.Invoker.invoked(4, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  StructuredErrorCode.this.parent.is(errorCode)
      |}
      |</statement>
      |                            </statements>
      |                        </method>
      |                        <method name="aaa/StructuredErrorCode/error" statement-rate="100.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="error" start="433" line="20" symbol="aaa.StructuredErrorCode.apply" tree="Apply" branch="false" invocation-count="3">
      |                                    StructuredErrorCode.apply(name, this)
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |                <class name="Demo" filename="/aaa/ErrorCode.scala" statement-rate="0.00" branch-rate="0.00">
      |                    <methods>
      |                        <method name="aaa/Demo/main" statement-rate="0.00" branch-rate="0.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="1934" line="68" symbol="aaa.ClientError.required" tree="Select" branch="false" invocation-count="0">
      |                                    ClientError.required
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="1926" line="68" symbol="scala.Predef.println" tree="Apply" branch="false" invocation-count="0">
      |                                    scala.this.Predef.println({
      |  scoverage.Invoker.invoked(25, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  ClientError.required
      |})
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="1999" line="69" symbol="aaa.ClientError.invalid" tree="Select" branch="false" invocation-count="0">
      |                                    ClientError.invalid
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="1991" line="69" symbol="scala.Predef.println" tree="Apply" branch="false" invocation-count="0">
      |                                    scala.this.Predef.println({
      |  scoverage.Invoker.invoked(27, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  ClientError.invalid
      |})
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2055" line="70" symbol="scala.Predef.println" tree="Apply" branch="false" invocation-count="0">
      |                                    scala.this.Predef.println(MySqlError)
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2125" line="71" symbol="aaa.MySqlError.syntax" tree="Select" branch="false" invocation-count="0">
      |                                    MySqlError.syntax
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2117" line="71" symbol="scala.Predef.println" tree="Apply" branch="false" invocation-count="0">
      |                                    scala.this.Predef.println({
      |  scoverage.Invoker.invoked(30, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  MySqlError.syntax
      |})
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2194" line="72" symbol="aaa.MyServiceLogicError.logicFailed" tree="Select" branch="false" invocation-count="0">
      |                                    MyServiceLogicError.logicFailed
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2186" line="72" symbol="scala.Predef.println" tree="Apply" branch="false" invocation-count="0">
      |                                    scala.this.Predef.println({
      |  scoverage.Invoker.invoked(32, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  MyServiceLogicError.logicFailed
      |})
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2275" line="74" symbol="aaa.ClientError.required" tree="Select" branch="false" invocation-count="0">
      |                                    ClientError.required
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2300" line="75" symbol="aaa.Demo.e" tree="Ident" branch="false" invocation-count="0">
      |                                    e
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2345" line="76" symbol="scala.Predef.println" tree="Apply" branch="false" invocation-count="0">
      |                                    scala.this.Predef.println(&quot;required&quot;)
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2345" line="76" symbol="&lt;nosymbol&gt;" tree="Block" branch="false" invocation-count="0">
      |                                    {
      |  scoverage.Invoker.invoked(36, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  scala.this.Predef.println(&quot;required&quot;)
      |}
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2399" line="77" symbol="scala.Predef.println" tree="Apply" branch="false" invocation-count="0">
      |                                    scala.this.Predef.println(&quot;invalid&quot;)
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2399" line="77" symbol="&lt;nosymbol&gt;" tree="Block" branch="false" invocation-count="0">
      |                                    {
      |  scoverage.Invoker.invoked(38, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  scala.this.Predef.println(&quot;invalid&quot;)
      |}
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2431" line="78" symbol="&lt;nosymbol&gt;" tree="Literal" branch="false" invocation-count="0">
      |                                    ()
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2431" line="78" symbol="&lt;nosymbol&gt;" tree="Block" branch="false" invocation-count="0">
      |                                    {
      |  scoverage.Invoker.invoked(40, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  ()
      |}
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2449" line="81" symbol="aaa.ErrorCode.is" tree="Apply" branch="false" invocation-count="0">
      |                                    MyServiceServerError.mongoDbError.is(ServerError)
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2505" line="82" symbol="scala.Predef.println" tree="Apply" branch="false" invocation-count="0">
      |                                    scala.this.Predef.println(&quot;This is a server error&quot;)
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2505" line="82" symbol="&lt;nosymbol&gt;" tree="Block" branch="true" invocation-count="0">
      |                                    {
      |  scoverage.Invoker.invoked(43, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  scala.this.Predef.println(&quot;This is a server error&quot;)
      |}
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2445" line="81" symbol="&lt;nosymbol&gt;" tree="Literal" branch="false" invocation-count="0">
      |                                    ()
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2445" line="81" symbol="&lt;nosymbol&gt;" tree="Block" branch="true" invocation-count="0">
      |                                    {
      |  scoverage.Invoker.invoked(45, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  ()
      |}
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |                <class name="MySqlError" filename="/aaa/ErrorCode.scala" statement-rate="0.00" branch-rate="100.00">
      |                    <methods>
      |                        <method name="aaa/MySqlError/&lt;none&gt;" statement-rate="0.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="MySqlError" method="&lt;none&gt;" start="1097" line="42" symbol="aaa.StructuredErrorCode.error" tree="Apply" branch="false" invocation-count="0">
      |                                    MySqlError.this.error(&quot;syntax&quot;)
      |</statement>
      |                                <statement
      |                                package="aaa" class="MySqlError" method="&lt;none&gt;" start="1132" line="43" symbol="aaa.StructuredErrorCode.error" tree="Apply" branch="false" invocation-count="0">
      |                                    MySqlError.this.error(&quot;connection&quot;)
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |                <class name="MyServiceServerError" filename="/aaa/ErrorCode.scala" statement-rate="100.00" branch-rate="100.00">
      |                    <methods>
      |                        <method name="aaa/MyServiceServerError/&lt;none&gt;" statement-rate="100.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="MyServiceServerError" method="&lt;none&gt;" start="1553" line="55" symbol="aaa.StructuredErrorCode.error" tree="Apply" branch="false" invocation-count="1">
      |                                    MyServiceServerError.this.error(&quot;mongodberror&quot;)
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |                <class name="RootError" filename="/aaa/ErrorCode.scala" statement-rate="50.00" branch-rate="100.00">
      |                    <methods>
      |                        <method name="aaa/RootError/&lt;none&gt;" statement-rate="100.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="RootError" method="&lt;none&gt;" start="715" line="28" symbol="&lt;nosymbol&gt;" tree="Literal" branch="false" invocation-count="1">
      |                                    &quot;&quot;
      |</statement>
      |                            </statements>
      |                        </method>
      |                        <method name="aaa/RootError/is" statement-rate="0.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="RootError" method="is" start="760" line="29" symbol="&lt;nosymbol&gt;" tree="Literal" branch="false" invocation-count="0">
      |                                    false
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |                <class name="ServerError" filename="/aaa/ErrorCode.scala" statement-rate="100.00" branch-rate="100.00">
      |                    <methods>
      |                        <method name="aaa/ServerError/&lt;none&gt;" statement-rate="100.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="ServerError" method="&lt;none&gt;" start="994" line="38" symbol="aaa.StructuredErrorCode.error" tree="Apply" branch="false" invocation-count="1">
      |                                    ServerError.this.error(&quot;solar&quot;)
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |                <class name="ClientError" filename="/aaa/ErrorCode.scala" statement-rate="0.00" branch-rate="100.00">
      |                    <methods>
      |                        <method name="aaa/ClientError/&lt;none&gt;" statement-rate="0.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="ClientError" method="&lt;none&gt;" start="856" line="33" symbol="aaa.StructuredErrorCode.error" tree="Apply" branch="false" invocation-count="0">
      |                                    ClientError.this.error(&quot;required&quot;)
      |</statement>
      |                                <statement
      |                                package="aaa" class="ClientError" method="&lt;none&gt;" start="890" line="34" symbol="aaa.StructuredErrorCode.error" tree="Apply" branch="false" invocation-count="0">
      |                                    ClientError.this.error(&quot;invalid&quot;)
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |            </classes>
      |        </package>
      |        <package name="bbb" statement-rate="0.00">
      |            <classes>
      |                <class name="Main" filename="/aaa/Graph.scala" statement-rate="0.00" branch-rate="100.00">
      |                    <methods>
      |                        <method name="bbb/Main/main" statement-rate="0.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="bbb" class="Main" method="main" start="791" line="30" symbol="aaa.MakeRectangleModelFromFile.apply" tree="Apply" branch="false" invocation-count="0">
      |                                    aaa.MakeRectangleModelFromFile.apply(null)
      |</statement>
      |                                <statement
      |                                package="bbb" class="Main" method="main" start="875" line="31" symbol="scala.Any.isInstanceOf" tree="TypeApply" branch="false" invocation-count="0">
      |                                    x.isInstanceOf[Serializable]
      |</statement>
      |                                <statement
      |                                package="bbb" class="Main" method="main" start="867" line="31" symbol="scala.Predef.println" tree="Apply" branch="false" invocation-count="0">
      |                                    scala.this.Predef.println({
      |  scoverage.Invoker.invoked(52, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  x.isInstanceOf[Serializable]
      |})
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |            </classes>
      |        </package>
      |    </packages>
      |</scoverage>
    """.stripMargin

  val dataWithoutDeclaration =
    """<scoverage statement-rate="24.53" branch-rate="33.33" version="1.0" timestamp="1391478578154">
      |    <packages>
      |        <package name="aaa" statement-rate="26.00">
      |            <classes>
      |                <class name="MyServiceClientError" filename="/aaa/ErrorCode.scala" statement-rate="0.00" branch-rate="100.00">
      |                    <methods>
      |                        <method name="aaa/MyServiceClientError/&lt;none&gt;" statement-rate="0.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="MyServiceClientError" method="&lt;none&gt;" start="1425" line="51" symbol="aaa.StructuredErrorCode.error" tree="Apply" branch="false" invocation-count="0">
      |                                    MyServiceClientError.this.error(&quot;zipcodeinvalid&quot;)
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |                <class name="$anon" filename="/aaa/Graph.scala" statement-rate="0.00" branch-rate="100.00">
      |                    <methods>
      |                        <method name="aaa/$anon/apply" statement-rate="0.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="$anon" method="apply" start="526" line="16" symbol="&lt;nosymbol&gt;" tree="Literal" branch="false" invocation-count="0">
      |                                    2
      |</statement>
      |                                <statement
      |                                package="aaa" class="$anon" method="apply" start="600" line="17" symbol="&lt;nosymbol&gt;" tree="Literal" branch="false" invocation-count="0">
      |                                    3
      |</statement>
      |                                <statement
      |                                package="aaa" class="$anon" method="apply" start="655" line="18" symbol="scala.Some.apply" tree="Apply" branch="false" invocation-count="0">
      |                                    scala.Some.apply[String](&quot;One&quot;)
      |</statement>
      |                                <statement
      |                                package="aaa" class="$anon" method="apply" start="443" line="15" symbol="aaa.MakeRectangleModelFromFile.$anon.&lt;init&gt;" tree="Apply" branch="false" invocation-count="0">
      |                                    new $anon()
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |                <class name="MyServiceLogicError" filename="/aaa/ErrorCode.scala" statement-rate="100.00" branch-rate="100.00">
      |                    <methods>
      |                        <method name="aaa/MyServiceLogicError/&lt;none&gt;" statement-rate="100.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="MyServiceLogicError" method="&lt;none&gt;" start="1686" line="59" symbol="aaa.StructuredErrorCode.error" tree="Apply" branch="false" invocation-count="1">
      |                                    MyServiceLogicError.this.error(&quot;logicfailed&quot;)
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |                <class name="StructuredErrorCode" filename="/aaa/ErrorCode.scala" statement-rate="64.29" branch-rate="50.00">
      |                    <methods>
      |                        <method name="aaa/StructuredErrorCode/toString" statement-rate="100.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="toString" start="321" line="16" symbol="java.lang.Object.toString" tree="Apply" branch="false" invocation-count="4">
      |                                    StructuredErrorCode.this.parent.toString()
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="toString" start="346" line="17" symbol="java.lang.Object.==" tree="Apply" branch="false" invocation-count="4">
      |                                    p.==(&quot;&quot;)
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="toString" start="355" line="17" symbol="&lt;nosymbol&gt;" tree="Literal" branch="false" invocation-count="1">
      |                                    &quot;&quot;
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="toString" start="355" line="17" symbol="&lt;nosymbol&gt;" tree="Block" branch="true" invocation-count="1">
      |                                    {
      |  scoverage.Invoker.invoked(8, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  &quot;&quot;
      |}
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="toString" start="363" line="17" symbol="java.lang.String.+" tree="Apply" branch="false" invocation-count="3">
      |                                    p.+(&quot;-&quot;)
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="toString" start="363" line="17" symbol="&lt;nosymbol&gt;" tree="Block" branch="true" invocation-count="3">
      |                                    {
      |  scoverage.Invoker.invoked(10, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  p.+(&quot;-&quot;)
      |}
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="toString" start="374" line="17" symbol="aaa.StructuredErrorCode.name" tree="Select" branch="false" invocation-count="4">
      |                                    StructuredErrorCode.this.name
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="toString" start="341" line="17" symbol="java.lang.String.+" tree="Apply" branch="false" invocation-count="4">
      |                                    if ({
      |  scoverage.Invoker.invoked(7, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  p.==(&quot;&quot;)
      |})
      |  {
      |    scoverage.Invoker.invoked(9, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |    {
      |      scoverage.Invoker.invoked(8, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |      &quot;&quot;
      |    }
      |  }
      |else
      |  {
      |    scoverage.Invoker.invoked(11, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |    {
      |      scoverage.Invoker.invoked(10, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |      p.+(&quot;-&quot;)
      |    }
      |  }.+({
      |  scoverage.Invoker.invoked(12, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  StructuredErrorCode.this.name
      |})
      |</statement>
      |                            </statements>
      |                        </method>
      |                        <method name="aaa/StructuredErrorCode/is" statement-rate="0.00" branch-rate="0.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="is" start="210" line="9" symbol="java.lang.Object.==" tree="Apply" branch="false" invocation-count="0">
      |                                    errorCode.==(this)
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="is" start="235" line="10" symbol="&lt;nosymbol&gt;" tree="Literal" branch="false" invocation-count="0">
      |                                    true
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="is" start="235" line="10" symbol="&lt;nosymbol&gt;" tree="Block" branch="true" invocation-count="0">
      |                                    {
      |  scoverage.Invoker.invoked(2, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  true
      |}
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="is" start="255" line="12" symbol="aaa.ErrorCode.is" tree="Apply" branch="false" invocation-count="0">
      |                                    StructuredErrorCode.this.parent.is(errorCode)
      |</statement>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="is" start="255" line="12" symbol="&lt;nosymbol&gt;" tree="Block" branch="true" invocation-count="0">
      |                                    {
      |  scoverage.Invoker.invoked(4, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  StructuredErrorCode.this.parent.is(errorCode)
      |}
      |</statement>
      |                            </statements>
      |                        </method>
      |                        <method name="aaa/StructuredErrorCode/error" statement-rate="100.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="StructuredErrorCode" method="error" start="433" line="20" symbol="aaa.StructuredErrorCode.apply" tree="Apply" branch="false" invocation-count="3">
      |                                    StructuredErrorCode.apply(name, this)
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |                <class name="Demo" filename="/aaa/ErrorCode.scala" statement-rate="0.00" branch-rate="0.00">
      |                    <methods>
      |                        <method name="aaa/Demo/main" statement-rate="0.00" branch-rate="0.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="1934" line="68" symbol="aaa.ClientError.required" tree="Select" branch="false" invocation-count="0">
      |                                    ClientError.required
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="1926" line="68" symbol="scala.Predef.println" tree="Apply" branch="false" invocation-count="0">
      |                                    scala.this.Predef.println({
      |  scoverage.Invoker.invoked(25, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  ClientError.required
      |})
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="1999" line="69" symbol="aaa.ClientError.invalid" tree="Select" branch="false" invocation-count="0">
      |                                    ClientError.invalid
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="1991" line="69" symbol="scala.Predef.println" tree="Apply" branch="false" invocation-count="0">
      |                                    scala.this.Predef.println({
      |  scoverage.Invoker.invoked(27, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  ClientError.invalid
      |})
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2055" line="70" symbol="scala.Predef.println" tree="Apply" branch="false" invocation-count="0">
      |                                    scala.this.Predef.println(MySqlError)
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2125" line="71" symbol="aaa.MySqlError.syntax" tree="Select" branch="false" invocation-count="0">
      |                                    MySqlError.syntax
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2117" line="71" symbol="scala.Predef.println" tree="Apply" branch="false" invocation-count="0">
      |                                    scala.this.Predef.println({
      |  scoverage.Invoker.invoked(30, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  MySqlError.syntax
      |})
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2194" line="72" symbol="aaa.MyServiceLogicError.logicFailed" tree="Select" branch="false" invocation-count="0">
      |                                    MyServiceLogicError.logicFailed
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2186" line="72" symbol="scala.Predef.println" tree="Apply" branch="false" invocation-count="0">
      |                                    scala.this.Predef.println({
      |  scoverage.Invoker.invoked(32, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  MyServiceLogicError.logicFailed
      |})
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2275" line="74" symbol="aaa.ClientError.required" tree="Select" branch="false" invocation-count="0">
      |                                    ClientError.required
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2300" line="75" symbol="aaa.Demo.e" tree="Ident" branch="false" invocation-count="0">
      |                                    e
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2345" line="76" symbol="scala.Predef.println" tree="Apply" branch="false" invocation-count="0">
      |                                    scala.this.Predef.println(&quot;required&quot;)
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2345" line="76" symbol="&lt;nosymbol&gt;" tree="Block" branch="false" invocation-count="0">
      |                                    {
      |  scoverage.Invoker.invoked(36, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  scala.this.Predef.println(&quot;required&quot;)
      |}
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2399" line="77" symbol="scala.Predef.println" tree="Apply" branch="false" invocation-count="0">
      |                                    scala.this.Predef.println(&quot;invalid&quot;)
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2399" line="77" symbol="&lt;nosymbol&gt;" tree="Block" branch="false" invocation-count="0">
      |                                    {
      |  scoverage.Invoker.invoked(38, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  scala.this.Predef.println(&quot;invalid&quot;)
      |}
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2431" line="78" symbol="&lt;nosymbol&gt;" tree="Literal" branch="false" invocation-count="0">
      |                                    ()
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2431" line="78" symbol="&lt;nosymbol&gt;" tree="Block" branch="false" invocation-count="0">
      |                                    {
      |  scoverage.Invoker.invoked(40, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  ()
      |}
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2449" line="81" symbol="aaa.ErrorCode.is" tree="Apply" branch="false" invocation-count="0">
      |                                    MyServiceServerError.mongoDbError.is(ServerError)
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2505" line="82" symbol="scala.Predef.println" tree="Apply" branch="false" invocation-count="0">
      |                                    scala.this.Predef.println(&quot;This is a server error&quot;)
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2505" line="82" symbol="&lt;nosymbol&gt;" tree="Block" branch="true" invocation-count="0">
      |                                    {
      |  scoverage.Invoker.invoked(43, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  scala.this.Predef.println(&quot;This is a server error&quot;)
      |}
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2445" line="81" symbol="&lt;nosymbol&gt;" tree="Literal" branch="false" invocation-count="0">
      |                                    ()
      |</statement>
      |                                <statement
      |                                package="aaa" class="Demo" method="main" start="2445" line="81" symbol="&lt;nosymbol&gt;" tree="Block" branch="true" invocation-count="0">
      |                                    {
      |  scoverage.Invoker.invoked(45, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  ()
      |}
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |                <class name="MySqlError" filename="/aaa/ErrorCode.scala" statement-rate="0.00" branch-rate="100.00">
      |                    <methods>
      |                        <method name="aaa/MySqlError/&lt;none&gt;" statement-rate="0.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="MySqlError" method="&lt;none&gt;" start="1097" line="42" symbol="aaa.StructuredErrorCode.error" tree="Apply" branch="false" invocation-count="0">
      |                                    MySqlError.this.error(&quot;syntax&quot;)
      |</statement>
      |                                <statement
      |                                package="aaa" class="MySqlError" method="&lt;none&gt;" start="1132" line="43" symbol="aaa.StructuredErrorCode.error" tree="Apply" branch="false" invocation-count="0">
      |                                    MySqlError.this.error(&quot;connection&quot;)
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |                <class name="MyServiceServerError" filename="/aaa/ErrorCode.scala" statement-rate="100.00" branch-rate="100.00">
      |                    <methods>
      |                        <method name="aaa/MyServiceServerError/&lt;none&gt;" statement-rate="100.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="MyServiceServerError" method="&lt;none&gt;" start="1553" line="55" symbol="aaa.StructuredErrorCode.error" tree="Apply" branch="false" invocation-count="1">
      |                                    MyServiceServerError.this.error(&quot;mongodberror&quot;)
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |                <class name="RootError" filename="/aaa/ErrorCode.scala" statement-rate="50.00" branch-rate="100.00">
      |                    <methods>
      |                        <method name="aaa/RootError/&lt;none&gt;" statement-rate="100.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="RootError" method="&lt;none&gt;" start="715" line="28" symbol="&lt;nosymbol&gt;" tree="Literal" branch="false" invocation-count="1">
      |                                    &quot;&quot;
      |</statement>
      |                            </statements>
      |                        </method>
      |                        <method name="aaa/RootError/is" statement-rate="0.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="RootError" method="is" start="760" line="29" symbol="&lt;nosymbol&gt;" tree="Literal" branch="false" invocation-count="0">
      |                                    false
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |                <class name="ServerError" filename="/aaa/ErrorCode.scala" statement-rate="100.00" branch-rate="100.00">
      |                    <methods>
      |                        <method name="aaa/ServerError/&lt;none&gt;" statement-rate="100.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="ServerError" method="&lt;none&gt;" start="994" line="38" symbol="aaa.StructuredErrorCode.error" tree="Apply" branch="false" invocation-count="1">
      |                                    ServerError.this.error(&quot;solar&quot;)
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |                <class name="ClientError" filename="/aaa/ErrorCode.scala" statement-rate="0.00" branch-rate="100.00">
      |                    <methods>
      |                        <method name="aaa/ClientError/&lt;none&gt;" statement-rate="0.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="aaa" class="ClientError" method="&lt;none&gt;" start="856" line="33" symbol="aaa.StructuredErrorCode.error" tree="Apply" branch="false" invocation-count="0">
      |                                    ClientError.this.error(&quot;required&quot;)
      |</statement>
      |                                <statement
      |                                package="aaa" class="ClientError" method="&lt;none&gt;" start="890" line="34" symbol="aaa.StructuredErrorCode.error" tree="Apply" branch="false" invocation-count="0">
      |                                    ClientError.this.error(&quot;invalid&quot;)
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |            </classes>
      |        </package>
      |        <package name="bbb" statement-rate="0.00">
      |            <classes>
      |                <class name="Main" filename="/aaa/Graph.scala" statement-rate="0.00" branch-rate="100.00">
      |                    <methods>
      |                        <method name="bbb/Main/main" statement-rate="0.00" branch-rate="100.00">
      |                            <statements>
      |                                <statement
      |                                package="bbb" class="Main" method="main" start="791" line="30" symbol="aaa.MakeRectangleModelFromFile.apply" tree="Apply" branch="false" invocation-count="0">
      |                                    aaa.MakeRectangleModelFromFile.apply(null)
      |</statement>
      |                                <statement
      |                                package="bbb" class="Main" method="main" start="875" line="31" symbol="scala.Any.isInstanceOf" tree="TypeApply" branch="false" invocation-count="0">
      |                                    x.isInstanceOf[Serializable]
      |</statement>
      |                                <statement
      |                                package="bbb" class="Main" method="main" start="867" line="31" symbol="scala.Predef.println" tree="Apply" branch="false" invocation-count="0">
      |                                    scala.this.Predef.println({
      |  scoverage.Invoker.invoked(52, &quot;/home/rado/workspace/aaa/target/scala-2.10/scoverage.measurement&quot;);
      |  x.isInstanceOf[Serializable]
      |})
      |</statement>
      |                            </statements>
      |                        </method>
      |                    </methods>
      |                </class>
      |            </classes>
      |        </package>
      |    </packages>
      |</scoverage>
    """.stripMargin
}
