package cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures.scan.validtypes;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;

/** Not a sheet class: holds valid nested sheet classes. */
public class ValidSheets {

    /** Valid abstract sheet class, nested. */
    @ExcelSheet
    public abstract static class AbstractSheet {

        @ExcelColumn(header = "Name", order = 1)
        private String name;
    }

    /** Valid static nested sheet class. */
    @ExcelSheet
    public static class StaticSheet {

        @ExcelColumn(header = "Name", order = 1)
        private String name;
    }

    /** Valid non-static nested sheet class. */
    @ExcelSheet
    public class InnerSheet {

        @ExcelColumn(header = "Name", order = 1)
        private String name;
    }
}
