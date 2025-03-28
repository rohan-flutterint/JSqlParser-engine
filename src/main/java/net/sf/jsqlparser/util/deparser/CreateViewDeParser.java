/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.statement.create.view.AutoRefreshOption;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.create.view.TemporaryOption;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;

public class CreateViewDeParser extends AbstractDeParser<CreateView> {

    private final SelectVisitor<StringBuilder> selectVisitor;

    public CreateViewDeParser(StringBuilder buffer) {
        super(buffer);
        SelectDeParser selectDeParser = new SelectDeParser();
        selectDeParser.setBuilder(buffer);
        ExpressionDeParser expressionDeParser = new ExpressionDeParser(selectDeParser, buffer);
        selectDeParser.setExpressionVisitor(expressionDeParser);
        selectVisitor = selectDeParser;
    }

    public CreateViewDeParser(StringBuilder buffer, SelectVisitor<StringBuilder> selectVisitor) {
        super(buffer);
        this.selectVisitor = selectVisitor;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public void deParse(CreateView createView) {
        builder.append("CREATE ");
        if (createView.isOrReplace()) {
            builder.append("OR REPLACE ");
        }
        switch (createView.getForce()) {
            case FORCE:
                builder.append("FORCE ");
                break;
            case NO_FORCE:
                builder.append("NO FORCE ");
                break;
            case NONE:
                break;
            default:
                // nothing
        }
        if (createView.isSecure()) {
            builder.append("SECURE ");
        }
        if (createView.getTemporary() != TemporaryOption.NONE) {
            builder.append(createView.getTemporary().name()).append(" ");
        }
        if (createView.isMaterialized()) {
            builder.append("MATERIALIZED ");
        }
        builder.append("VIEW ").append(createView.getView().getFullyQualifiedName());
        if (createView.isIfNotExists()) {
            builder.append(" IF NOT EXISTS");
        }
        if (createView.getAutoRefresh() != AutoRefreshOption.NONE) {
            builder.append(" AUTO REFRESH ").append(createView.getAutoRefresh().name());
        }
        if (createView.getColumnNames() != null) {
            builder.append("(");
            builder.append(createView.getColumnNames());
            builder.append(")");
        }
        if (createView.getViewCommentOptions() != null) {
            builder.append(
                    PlainSelect.getStringList(createView.getViewCommentOptions(), false, false));
        }
        builder.append(" AS ");

        Select select = createView.getSelect();
        select.accept(selectVisitor, null);
        if (createView.isWithReadOnly()) {
            builder.append(" WITH READ ONLY");
        }
    }

}
