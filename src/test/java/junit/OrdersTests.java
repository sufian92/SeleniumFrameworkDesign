package junit;

import org.junit.Test;
import org.junit.Before;
import org.openqa.selenium.WebElement;
import rahulshettyacademy.enums.DBInsertEnum;
import rahulshettyacademy.enums.SnapShotEnum;
import rahulshettyacademy.helpers.SeleniumHelper;
import rahulshettyacademy.helpers.SnapShotHelper;
import rahulshettyacademy.helpers.UITestBase;
import rahulshettyacademy.pageobjects.CartPage;
import rahulshettyacademy.pageobjects.LandingPage;
import rahulshettyacademy.pageobjects.ProductCatalogue;
import rahulshettyacademy.helpers.DBUnitHelper;


import java.io.File;
import java.util.List;

public class OrdersTests extends UITestBase {

    public OrdersTests() {
        super();
    }

    @Before
    public void setUp() throws Exception {

//        DBUnitHelper.initDB(CONN_URL, USER, PASSWORD, DBInsertEnum.DEFAULT.getXml(), false,100000);

        // Initiate the selenium driver and navigates to the ASAPP application
        driver = SeleniumHelper.init(driver);

        // sets the driver to thread
        driverThread.set(driver);
    }


    /**
     * Dependency : NONE
     */
    @Test
    public void submitOrderTest() throws Exception {

        // gets the test name to be used to create the screenshot name
        testNameThread.set(new Object() {
        }.getClass().getEnclosingMethod().getName());

        LandingPage landingPage = new LandingPage(driver);
        landingPage.goTo();
        ProductCatalogue productCatalogue = landingPage.loginApplication("anshika@gmail.com", "Iamking@000");

//        // to be set in the extent report
//        this.setActualAndExpectedImgagesThread(SnapShotEnum.PRODUCTS_PAGE.getName());
//
//        // compare two snapshots
//        SnapShotHelper.compareSnapShots(driverThread.get(), SnapShotEnum.PRODUCTS_PAGE.getName(),
//                new File(SnapShotEnum.PRODUCTS_PAGE.getFile()));


    }
}
