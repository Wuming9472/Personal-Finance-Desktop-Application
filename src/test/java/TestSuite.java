package it.unicas.project.template.address;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
    "it.unicas.project.template.address.util",
    "it.unicas.project.template.address.view"
})
@ExcludeClassNamePatterns({".*DateUtilTest"})
public class TestSuite {
}
