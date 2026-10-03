package cloud.baldilorenzo.sheetsmith.autoconfigure.fixtures;

import cloud.baldilorenzo.sheetsmith.annotation.ExcelColumn;
import cloud.baldilorenzo.sheetsmith.annotation.ExcelSheet;
import cloud.baldilorenzo.sheetsmith.convert.CellConverter;
import cloud.baldilorenzo.sheetsmith.convert.CellValue;
import cloud.baldilorenzo.sheetsmith.convert.ConversionContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/** Types used by the auto-configuration tests. */
public final class Fixtures {

    private Fixtures() {
    }

    /** A type without built-in converter. */
    public record Money(BigDecimal amount, String currency) {
    }

    /** Converter registered application-wide as a bean. */
    public static class MoneyConverter implements CellConverter<Money> {
        @Override
        public CellValue convert(Money value, ConversionContext context) {
            return CellValue.text(value.currency() + " " + value.amount().toPlainString());
        }
    }

    /** A dependency of {@link FieldMoneyConverter}. */
    public record CurrencyFormatter(String prefix) {
        public String format(Money money) {
            return prefix + money.amount().toPlainString();
        }
    }

    /** Field-level converter with a constructor dependency. */
    public static class FieldMoneyConverter implements CellConverter<Money> {

        private final CurrencyFormatter formatter;

        public FieldMoneyConverter(CurrencyFormatter formatter) {
            this.formatter = formatter;
        }

        @Override
        public CellValue convert(Money value, ConversionContext context) {
            return CellValue.text(formatter.format(value));
        }
    }

    @ExcelSheet
    public record Payment(
            @ExcelColumn(header = "Amount", order = 1) Money amount,
            @ExcelColumn(header = "Paid on", order = 2) Date paidOn) {
    }

    @ExcelSheet
    public record Order(
            @ExcelColumn(header = "Total", order = 1, converter = FieldMoneyConverter.class) Money total) {
    }

    @ExcelSheet
    public record PlainMoney(@ExcelColumn(header = "Amount", order = 1) Money amount) {
    }

    @ExcelSheet
    public record Dated(
            @ExcelColumn(header = "Day", order = 1) LocalDate day,
            @ExcelColumn(header = "Count", order = 2) int count) {
    }
}
