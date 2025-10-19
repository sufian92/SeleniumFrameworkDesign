package rahulshettyacademy.enums;

public enum SnapShotEnum {
    PRODUCTS_PAGE("products-page"),;
    private final String snapShotName;


    SnapShotEnum(final String snapShotName) {
        this.snapShotName = snapShotName;

    }

    public String getName () {

        return this.snapShotName;
    }

    public String getFile () {

        return "./src/test/resources/expectedImages/" + this.snapShotName +".png";
    }
}
