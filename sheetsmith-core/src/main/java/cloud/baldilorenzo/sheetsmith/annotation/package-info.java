/**
 * Annotations that describe how a class is exported to an Excel sheet.
 * <p>
 * A class annotated with {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet} is exported; only its fields
 * annotated with {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn} become columns. Styles are declared
 * with {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle} and referenced by name from the slot
 * annotations.
 */
package cloud.baldilorenzo.sheetsmith.annotation;
