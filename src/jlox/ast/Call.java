package jlox.ast;

import java.util.List;

import jlox.Token;

public class Call extends Expr {
    public final Expr callee;
    public final Token paren; // We keep the closing parenthesis for error location tracking
    public final List<Expr> arguments;

    public Call(Expr callee, Token paren, List<Expr> arguments) {
        this.callee = callee;
        this.paren = paren;
        this.arguments = arguments;
    }
}
