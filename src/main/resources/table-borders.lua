-- Pandoc Lua filter: render all tables with full borders (row lines + column separators)
-- Targets PDF/LaTeX output only; other formats are passed through unchanged.
-- Tested against Pandoc 3.x AST (colspecs, cell.contents).

local function is_pdf_output()
    return FORMAT == "latex" or FORMAT == "pdf" or FORMAT == "beamer"
end

local function col_spec(align)
    if align == "AlignLeft"   then return "l" end
    if align == "AlignRight"  then return "r" end
    if align == "AlignCenter" then return "c" end
    return "l"
end

local function render_cell(cell)
    local blocks = cell.contents
    if not blocks or #blocks == 0 then return "" end
    return pandoc.write(pandoc.Pandoc(blocks), "latex")
        :gsub("^%s+", ""):gsub("%s+$", "")
        :gsub("\n", " ")
end

local function rows_to_latex(rows, bold)
    local lines = {}
    for _, row in ipairs(rows) do
        local cells = {}
        for _, cell in ipairs(row.cells) do
            local content = render_cell(cell)
            if bold then
                content = "\\textbf{" .. content .. "}"
            end
            cells[#cells+1] = content
        end
        lines[#lines+1] = table.concat(cells, " & ") .. " \\\\"
        lines[#lines+1] = "\\hline"
    end
    return lines
end

local function table_to_latex(tbl)
    local colspecs = tbl.colspecs
    local col_fmt = "|"
    for _, spec in ipairs(colspecs) do
        col_fmt = col_fmt .. col_spec(spec[1]) .. "|"
    end

    local lines = {}
    lines[#lines+1] = "\\begin{tabular}{" .. col_fmt .. "}"
    lines[#lines+1] = "\\hline"

    -- Header rows
    local head_rows = tbl.head and tbl.head.rows or {}
    if #head_rows > 0 then
        for _, line in ipairs(rows_to_latex(head_rows, true)) do
            lines[#lines+1] = line
        end
    end

    -- Body rows
    for _, body in ipairs(tbl.bodies) do
        for _, line in ipairs(rows_to_latex(body.body, false)) do
            lines[#lines+1] = line
        end
    end

    lines[#lines+1] = "\\end{tabular}"

    return table.concat(lines, "\n")
end

function Table(tbl)
    if not is_pdf_output() then
        return nil
    end
    local latex = table_to_latex(tbl)
    return pandoc.RawBlock("latex", latex)
end
