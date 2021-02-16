package net.lightning.proxy.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MotdUtil {

    private final int CENTER_PX = 127;
    private final int MAX_PX = 240;

    public enum MinecraftChar {
        A('A', 5),
        a('a', 5),
        B('B', 5),
        b('b', 5),
        C('C', 5),
        c('c', 5),
        D('D', 5),
        d('d', 5),
        E('E', 5),
        e('e', 5),
        F('F', 5),
        f('f', 4),
        G('G', 5),
        g('g', 5),
        H('H', 5),
        h('h', 5),
        I('I', 3),
        i('i', 1),
        J('J', 5),
        j('j', 5),
        K('K', 5),
        k('k', 4),
        L('L', 5),
        l('l', 1),
        M('M', 5),
        m('m', 5),
        N('N', 5),
        n('n', 5),
        O('O', 5),
        o('o', 5),
        P('P', 5),
        p('p', 5),
        Q('Q', 5),
        q('q', 5),
        R('R', 5),
        r('r', 5),
        S('S', 5),
        s('s', 5),
        T('T', 5),
        t('t', 4),
        U('U', 5),
        u('u', 5),
        V('V', 5),
        v('v', 5),
        W('W', 5),
        w('w', 5),
        X('X', 5),
        x('x', 5),
        Y('Y', 5),
        y('y', 5),
        Z('Z', 5),
        z('z', 5),
        NUM_1('1', 5),
        NUM_2('2', 5),
        NUM_3('3', 5),
        NUM_4('4', 5),
        NUM_5('5', 5),
        NUM_6('6', 5),
        NUM_7('7', 5),
        NUM_8('8', 5),
        NUM_9('9', 5),
        NUM_0('0', 5),
        EXCLAMATION_POINT('!', 1),
        AT_SYMBOL('@', 6),
        NUM_SIGN('#', 5),
        DOLLAR_SIGN('$', 5),
        PERCENT('%', 5),
        UP_ARROW('^', 5),
        AMPERSAND('&', 5),
        ASTERISK('*', 5),
        LEFT_PARENTHESIS('(', 4),
        RIGHT_PARENTHESIS(')', 4),
        MINUS('-', 5),
        UNDERSCORE('_', 5),
        PLUS_SIGN('+', 5),
        EQUALS_SIGN('=', 5),
        LEFT_CURL_BRACE('{', 4),
        RIGHT_CURL_BRACE('}', 4),
        LEFT_BRACKET('[', 3),
        RIGHT_BRACKET(']', 3),
        COLON(':', 1),
        SEMI_COLON(';', 1),
        DOUBLE_QUOTE('"', 3),
        SINGLE_QUOTE('\'', 1),
        LEFT_ARROW('<', 4),
        RIGHT_ARROW('>', 4),
        QUESTION_MARK('?', 5),
        SLASH('/', 5),
        BACK_SLASH('\\', 5),
        LINE('|', 1),
        TILDE('~', 5),
        TICK('`', 2),
        PERIOD('.', 1),
        COMMA(',', 1),
        SPACE(' ', 3),
        DEFAULT('a', 5);

        private final char character;
        private final int length;

        MinecraftChar(char character, int length) {
            this.character = character;
            this.length = length;
        }

        public char getCharacter() {
            return this.character;
        }

        public int getLength() {
            return this.length;
        }

        public int getBoldLength() {
            if (this == MinecraftChar.SPACE) {
                return this.getLength();
            }
            return this.length + 1;
        }

        public static MinecraftChar fromNative(char c) {
            for (MinecraftChar minecraftChar : MinecraftChar.values()) {
                if (minecraftChar.getCharacter() == c) {
                    return minecraftChar;
                }
            }
            return MinecraftChar.DEFAULT;
        }

    }

    public String center(String message) {
        int pixelLength = 0;
        boolean isChatCode = false, isBold = false;
        int charIndex = 0, lastSpaceIndex = 0;

        for (char c : message.toCharArray()) {
            if (c == '§') {
                isChatCode = true;
                continue;
            }
            else if (isChatCode) {
                isChatCode = false;
                if (c == 'l' || c == 'L') {
                    isBold = true;
                    continue;
                }
                else {
                    isBold = false;
                }
            }
            else if (c == ' ') {
                lastSpaceIndex = charIndex;
            }
            else {
                MinecraftChar minecraftChar = MinecraftChar.fromNative(c);
                pixelLength += isBold ? minecraftChar.getBoldLength() : minecraftChar.getLength();
                pixelLength++;
            }

            if (pixelLength >= MAX_PX) {
                message = message.substring(0, lastSpaceIndex + 1);
                break;
            }
            charIndex++;
        }

        int halvedMessageSize = pixelLength / 2;
        int toCompensate = CENTER_PX - halvedMessageSize, compensated = 0;
        final int spaceLength = MinecraftChar.SPACE.getLength() + 1;

        StringBuilder stringBuilder = new StringBuilder();
        while (compensated < toCompensate) {
            stringBuilder.append(" ");
            compensated += spaceLength;
        }
        return stringBuilder.toString() + message;
    }

}
