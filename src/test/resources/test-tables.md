# Table Border Test

## Simple Table

| Name       | Role         | Status  |
|------------|--------------|---------|
| Alice       | Developer    | Active  |
| Bob         | Designer     | Active  |
| Carol       | Manager      | Inactive|

## Aligned Columns

| Item          | Quantity | Unit Price | Total   |
|:--------------|:--------:|-----------:|--------:|
| Widget A      | 10       | $5.00      | $50.00  |
| Widget B      | 3        | $12.50     | $37.50  |
| Widget C      | 7        | $8.00      | $56.00  |
| **Total**     |          |            | **$143.50** |

## Multi-line Content Table

| Feature            | Description                              | Supported |
|--------------------|------------------------------------------|-----------|
| Markdown to DOCX   | Produces styled Word documents           | Yes       |
| Markdown to PDF    | Generates PDF via XeLaTeX                | Yes       |
| Table of Contents  | Optional TOC with configurable depth     | Yes       |
| Table Borders      | Full row and column borders in output    | Yes       |

## Long-line Content Table

| Feature            | Description                              | Supported |
|--------------------|------------------------------------------|-----------|
| In this column, the feature is Markdown to DOCX   | The description of the feature is Produces styled Word documents           | Yes       |
| In this column, the feature is Markdown to PDF    | The description of the feature is Generates PDF via XeLaTeX                | Yes       |
| In this column, the feature is Table of Contents  | The description of the feature is Optional TOC with configurable depth     | Yes       |
| In this column, the feature is Table Borders      | The description of the feature is Full row and column borders in output    | Yes       |

---

# Strikethrough

This sentence contains ~~strikethrough text~~ that requires the `soul` LaTeX package.

---

# Long Code Lines

## Fenced code block with language

```python
# This line is intentionally very long to verify that the verbatim environment wraps rather than overflows the page margin
very_long_variable_name_that_keeps_going = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
```

## Fenced code block without language

```
command --option-one=value --option-two=value --option-three=value --option-four=value --option-five=value --option-six=value
```
