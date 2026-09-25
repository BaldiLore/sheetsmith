# sheetsmith

Annotation-driven Excel generation for Spring Boot, built on Apache POI.

> Work in progress: the API is not stable until version 1.0.

## Modules

| Module | Purpose |
| --- | --- |
| `sheetsmith-core` | Framework-agnostic core: annotations, metadata extraction, style resolution, workbook writing |
| `sheetsmith-spring-boot-autoconfigure` | Spring Boot auto-configuration |
| `sheetsmith-spring-boot-starter` | The only dependency applications need to declare |

## Requirements

- Java 17 or later
- Spring Boot 4

## Building

```shell
./mvnw verify
```

## License

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
