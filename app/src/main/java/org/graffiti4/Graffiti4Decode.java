package org.graffiti4;

public class Graffiti4Decode {
    public static final char LEFT = 1;
    public static final char RIGHT = 2;
    public static final char DELETE_LEFT = 3;

    public enum Position {
        LOWERCASE, UPPERCASE, NUMBERS
    }

    public static char decodeGraffiti(char code, boolean dotMode, Position positionMode) {
        if (code == Graffiti4Engine.NULL_CHAR) {
            return 0;
        }
        if (dotMode) {
            switch (code) {
                case '.':
                    return '.';
                case 'a':
                    return '@';
                case 'n':
                    return '~';
                case 'h':
                    return '#';
                case 's':
                    return '&';
                case '$':
                    return '=';
                case 'i':
                    return '\n';
                case '!':
                    return '|';
                case 'z':
                    return '?';
                case 'j':
                    return ',';
                case 'c':
                    return '[';
                case 'D':
                    return ']';
                case 'e':
                    return '{';
                case 'b':
                    return '}';
                case 'v':
                case 'u':
                    return ':';
                case 't':
                    return '\\';
                case 'f':
                case 'r':
                    return '/';
                case '<':
                    return '_';
                case '>':
                    return '-';
                default:
                    return code;
            }

        } else {
            // normal mode
            if (positionMode == Position.NUMBERS) {
                switch (code) {
                    case 'o':
                    case 'q':
                    case 'p':
                        return '0';
                    case 'i':
                    case '!':
                    case 'a':
                        return '1';
                    case 'z':
                    case '?':
                        return '2';
                    case 'b':
                        return '3';
                    case 'l':
                    case 'c':
                        return '4';
                    case 'f':
                    case 's':
                        return '5';
                    case 'g':
                        return '6';
                    case 't':
                    case 'D':
                        return '7';
                    case 'y':
                        return '8';
                    case 'h':
                    case 'u':
                        return '9';
                    case 'k':
                        return '+';
                    case 'x':
                        return '*';
                    case '$':
                        return '=';
                    case '<': //back space
                        return DELETE_LEFT;
                    case '>': //forward
                        return ' ';
                    case '_': //back move
                        return LEFT;
                    case '-': //forward move
                        return RIGHT;
                    default:
                        return code;
                }

            } else {
                //letter mode
                switch (code) {
                    case '!':
                        return 'I';
                    case '<': //back space
                        return DELETE_LEFT;
                    case '>': //forward
                        return ' ';
                    case '_': //back move
                        return LEFT;
                    case '-': //forward move
                        return RIGHT;
                    case 'D':
                        code = 'd';
                        break;
                }
                if (positionMode == Position.UPPERCASE) {
                    if (code >= 'a' && code <= 'z') {
                        return (char) (code - 0x20);
                    }
                }
                return code;
            }
        }
    }
}
