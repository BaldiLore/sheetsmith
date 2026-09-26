/**
 * Annotations that describe how a class is exported to an Excel sheet, and the reference of the style model.
 * <p>
 * A <em>sheet class</em> is annotated with {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet} and describes
 * one table. Each of its fields annotated with {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn} is a
 * <em>column</em>; other fields are ignored. The look of the table is described with named styles and slots.
 *
 * <h2>Named styles and slots</h2>
 * A <em>named style</em> is a set of formatting attributes declared once with
 * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle}, on the sheet class or on a <em>style sheet</em>
 * annotated with {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelStyleSheet}. A <em>slot</em> is a place
 * where a named style is applied, by name:
 * <ul>
 *   <li>the title: {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet#titleStyle()};</li>
 *   <li>the header slots of {@link cloud.baldilorenzo.sheetsmith.annotation.HeaderStyles}, and the header style of
 *       each column, {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn#headerStyle()};</li>
 *   <li>the body slots of {@link cloud.baldilorenzo.sheetsmith.annotation.BodyStyles}, for the data rows at table
 *       level;</li>
 *   <li>the column slots of {@link cloud.baldilorenzo.sheetsmith.annotation.ColumnStyles}, for the data cells of
 *       one column.</li>
 * </ul>
 * Every attribute of a named style left at its default is unset and overrides nothing. The styles that apply to a
 * cell are merged attribute by attribute, following a fixed order called the <em>cascade</em>: each level
 * overrides only the attributes it sets and keeps the others. Attributes that no level sets keep the Excel
 * defaults.
 *
 * <h2 id="roles">Roles</h2>
 * The <em>role</em> of a data cell is its position in the table, and decides which slots apply to it:
 * <ul>
 *   <li>odd or even row: data rows are numbered from 1, in the order of the data list, so the first data row is
 *       odd;</li>
 *   <li>first row and last row: the first and the last element of the data list;</li>
 *   <li>first column and last column: by position after sorting by
 *       {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn#order()}.</li>
 * </ul>
 * Roles are independent: with one data row, that row is both first and last; with one column, it is both first
 * and last. Header cells have only the first and last column roles. The title has no role.
 *
 * <h2 id="cascade">Cascade</h2>
 * Data cells, from the least to the most specific level:
 * <ol>
 *   <li>the preset body styles, if any: base, then odd or even;</li>
 *   <li>body {@code base};</li>
 *   <li>body {@code odd} or {@code even};</li>
 *   <li>the edges of the outer border;</li>
 *   <li>body {@code lastColumn}, then {@code firstColumn};</li>
 *   <li>body {@code lastRow}, then {@code firstRow};</li>
 *   <li>column {@code base};</li>
 *   <li>column {@code odd} or {@code even};</li>
 *   <li>column {@code lastRow}, then {@code firstRow};</li>
 *   <li>the column {@code format}, as the data format.</li>
 * </ol>
 * Header cells, from the least to the most specific level:
 * <ol>
 *   <li>the preset header style, if any;</li>
 *   <li>header {@code base};</li>
 *   <li>the edges of the outer border;</li>
 *   <li>header {@code lastColumn}, then {@code firstColumn};</li>
 *   <li>the {@code headerStyle} of the column.</li>
 * </ol>
 * The title uses the preset title style, if any, then {@code titleStyle}.
 * <p>
 * The rules that follow from this order:
 * <ul>
 *   <li><b>The row wins over the column.</b> When a body row slot and a body column slot set the same attribute on
 *       one cell, the row slot is applied later and wins.</li>
 *   <li><b>The first wins over the last.</b> With one data row, {@code lastRow} is applied before
 *       {@code firstRow}, so {@code firstRow} wins; the same holds for {@code firstColumn} and {@code lastColumn}
 *       with one column.</li>
 *   <li><b>The column is the most specific level.</b> Column slots come after all table slots, so a column can
 *       always override the table; only the column {@code format} comes after them.</li>
 *   <li><b>Header and data are separate.</b> Header slots and column header styles never apply to data cells;
 *       body and column slots never apply to header cells; none of them applies to the title.</li>
 * </ul>
 *
 * <h2 id="outer-border">Outer border</h2>
 * {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet#outerBorder()} draws a frame around the header and
 * the data rows with one attribute: along the top of the header row, the left side of the first column, the right
 * side of the last column and the bottom of the last data row, or of the header row when there are no data rows.
 * The title is outside the frame. The frame is a level of the cascade that sets only the border sides on the edge
 * of the table, placed above the base and odd or even slots and below the role slots: a first or last row or
 * column slot, a column slot or a column header style that sets a border side overrides the frame on that side.
 *
 * <h2 id="presets">Presets</h2>
 * A preset, chosen with {@link cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet#preset()} or the application
 * defaults, generates title, header and body styles from one accent colour; the families are described in
 * {@link cloud.baldilorenzo.sheetsmith.style.TablePreset}. The preset is the lowest level of every cascade, so any
 * declared style overrides it attribute by attribute.
 *
 * @see cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet
 * @see cloud.baldilorenzo.sheetsmith.annotation.ExcelStyle
 */
package cloud.baldilorenzo.sheetsmith.annotation;
