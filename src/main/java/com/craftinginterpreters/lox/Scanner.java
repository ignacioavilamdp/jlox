package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private static final Map<String, TokenType> keywords;
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    static {
        keywords = new HashMap<>();
        keywords.put("and",   TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else",  TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for",   TokenType.FOR);
        keywords.put("fun",   TokenType.FUN);
        keywords.put("if",    TokenType.IF);
        keywords.put("nil",   TokenType.NIL);
        keywords.put("or",    TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return",TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this",  TokenType.THIS);
        keywords.put("true",  TokenType.TRUE);
        keywords.put("var",   TokenType.VAR);
        keywords.put("while", TokenType.WHILE   );
    }

    Scanner(String source){
        this.source = source;
    }

    List<Token> scanTokens(){
       while (!isAtEnd()){
           // We are at the beginning of the next lexeme
           start = current;
           scanToken();
       }
       tokens.add(new Token(TokenType.EOF, "", null, line));
       return tokens;
    }

    private void scanToken(){
        char c = advance();
        switch (c){
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;

            case '!':{
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            }
            case '=':{
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            }
            case '<':{
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            }
            case '>':{
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            }

            case '/':{
                if (match('/')){
                    // This is a comment until the end of the line
                    while (peek() != '\n' && !isAtEnd()) advance();
                }
                else{
                    addToken(TokenType.SLASH);
                }
                break;
            }

            // Ignore whitespaces
            case ' ':
            case '\r':
            case '\t':
                break;

            case '\n':
                line++;
                break;

            case '"': string(); break;

            default:
                if (isDigit(c)){
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void addToken(TokenType type){
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal){
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private void string(){
        while (peek() != '"' && !isAtEnd()){
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()){
            Lox.error(line, "Unterminated string.");
            return;
        }

        advance(); // Consume the closing "
        String value = source.substring(start + 1, current - 1); // Trim the surrounding quotes.
        addToken(TokenType.STRING, value);
    }

    private void number(){
        // Integer part
        while (isDigit(peek())) advance();

        // Fractional part handling
        if (peek() == '.' && isDigit(peekNext())){
            advance();  // Consume the "."
            while (isDigit(peek())) advance();
        }

        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void identifier(){
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }

    private boolean match(char expected){
        if (isAtEnd())  return false;
        if (source.charAt(current) != expected)  return false;  // TODO: Probably should use peek()

        current++;  // TODO: Probably should use advance()
        return true;
    }

    private char advance(){
        return source.charAt(current++);
    }

    private char peek(){
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext(){
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAtEnd(){
        return current >= source.length();
    }

    private boolean isDigit(char c){
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c){
        return  ('A' <= c && c <= 'Z') ||
                ('a' <= c && c <= 'z') ||
                 c == '_';
    }

    private boolean isAlphaNumeric(char c){
        return isAlpha(c) || isDigit(c);
    }

}
