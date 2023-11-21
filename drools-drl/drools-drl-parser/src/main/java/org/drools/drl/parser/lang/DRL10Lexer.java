package org.drools.drl.parser.lang;

import java.util.List;

import org.antlr.runtime.Token;
import org.drools.drl.parser.DroolsParserException;

/**
 * No implementation because everything is handled by DRL10Parser
 */
public class DRL10Lexer implements DRLLexer {

    @Override
    public String getSourceName() {
        return null;
    }

    @Override
    public Token nextToken() {
        return null;
    }

    @Override
    public List<DroolsParserException> getErrors() {
        return null;
    }

}
