# sheetsmith

Framework-agnostic core of sheetsmith: annotation-driven Excel (.xlsx) generation from Java POJOs and records with Apache POI.

https://sheetsmith.baldilorenzo.cloud/

Describe a table once with annotations, pass a list of objects, get an `.xlsx` file as a `byte[]` or written to an `OutputStream`: title, header, rows, styles, presets and formats included.

## Quick start

Add the starter:

```xml
<dependency>
    <groupId>cloud.baldilorenzo.sheetsmith</groupId>
    <artifactId>sheetsmith-spring-boot-starter</artifactId>
    <version>${sheetsmith.version}</version>
</dependency>
```

Describe the sheet:

```java
@ExcelSheet(title = "Customers", preset = TablePreset.MEDIUM, accentColor = "#1F4E79")
public record CustomerRow(
        @ExcelColumn(header = "Name", order = 10) String name,
        @ExcelColumn(header = "Customer since", order = 20, format = "dd/mm/yyyy") LocalDate since,
        @ExcelColumn(header = "Revenue", order = 30, format = "#,##0.00") BigDecimal revenue) {
}
```

Generate the file with the auto-configured bean:

```java
byte[] file = sheetsmith.generate(List.of(SheetData.of("Customers", CustomerRow.class, customers)));
```

Without Spring, depend on `sheetsmith-core` and create the generator with `Sheetsmith.builder().build()`.

## Modules

| Module | Purpose |
| --- | --- |
| `sheetsmith-core` | Framework-agnostic core: annotations, metadata extraction, style resolution, workbook writing |
| `sheetsmith-spring-boot-autoconfigure` | Spring Boot auto-configuration |
| `sheetsmith-spring-boot-starter` | The only dependency Spring Boot applications need to declare |

## Requirements

- Java 17 or later
- Spring Boot 4, for the Spring Boot integration only

## Building

```shell
./mvnw verify
```

## License

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).