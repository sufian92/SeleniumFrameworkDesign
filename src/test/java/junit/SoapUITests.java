//package junit;
//
//import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
//import com.eviware.soapui.model.support.PropertiesMap;
//import com.eviware.soapui.model.testsuite.TestCase;
//import com.eviware.soapui.model.testsuite.TestSuite;
//import org.junit.Before;
//import org.junit.Test;
//import rahulshettyacademy.enums.DBInsertEnum;
//import rahulshettyacademy.helpers.DBUnitHelper;
//import rahulshettyacademy.helpers.SeleniumHelper;
//import rahulshettyacademy.helpers.UITestBase;
//import com.eviware.soapui.impl.wsdl.WsdlProject;
//
//public class SoapUITests extends UITestBase {
//
//    public SoapUITests() {
//        super();
//    }
//
//
//
//
//    /**
//     * Dependency : NONE
//     */
//    @Test
//    public void firstTest() throws Exception {
//
//        // gets the test name to be used to create the screenshot name
//        testNameThread.set(new Object() {
//        }.getClass().getEnclosingMethod().getName());
//
//        WsdlProject project = new WsdlProject("C:\\Users\\sufia\\Documents\\Automation-soapui-project.xml");
//        TestSuite suite = project.getTestSuiteByName("Testing");
//
//        for (int i = 0; i <suite.getTestCaseCount(); i++) {
//
//            TestCase testCase = suite.getTestCaseAt(i);
//
//            testCase.run(new PropertiesMap(),false);
//        }
//    }
//}
