package rahulshettyacademy.enums;

public enum DBInsertEnum {

    DEFAULT("./src/test/resources/dbunit/default.xml"),;
    private final String xml;

    DBInsertEnum(final String xml) {
        this.xml = xml;
    }

    public String getXml () {

        return this.xml;
    }
}
