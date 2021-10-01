package xyz.e3ndr.notabene.assembler;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class NBAssembler {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");

    private static final String REGISTER_REGEX = "#[a-pA-P]";
    private static final String NUMBER_REGEX = "\\[(?:-)(?:0x|0b)?[a-fA-F0-9]*\\]";
    private static final String TEXT_REGEX = "\".*\"";
    private static final String LABEL_DECLARATION_REGEX = "[0-9a-zA-Z_]*:";
    private static final String LABEL_REGEX = "\\([0-9a-zA-Z_]*\\)";

    private static final Map<String, Integer> OPCODES = new HashMap<>();

    private FastLogger logger = new FastLogger("Assembler");

    private String contents;
    private File destFile;

    static {
        OPCODES.put("$nop", 0);
        OPCODES.put("$jmp", 1);
        OPCODES.put("$jnr", 2);
        OPCODES.put("$pur", 3);
        OPCODES.put("$rer", 4);
        OPCODES.put("$ret", 5);
        OPCODES.put("$inc", 6);
        OPCODES.put("$dec", 7);
        OPCODES.put("$add", 8);
        OPCODES.put("$sub", 9);
        OPCODES.put("$mul", 10);
        OPCODES.put("$div", 11);
        OPCODES.put("$set", 12);
        OPCODES.put("$mst", 13);
        OPCODES.put("$ast", 14);
        OPCODES.put("$mld", 15);
        OPCODES.put("$ald", 16);
        OPCODES.put("$jeq", 17);
        OPCODES.put("$jne", 18);
        OPCODES.put("$pop", 19);
        OPCODES.put("$hlt", 20);
        OPCODES.put("$cpy", 21);
        OPCODES.put("$cpr", 22);
        OPCODES.put("$clr", 23);
        OPCODES.put("$jgt", 24);
        OPCODES.put("$jlt", 25);
        OPCODES.put("$mod", 26);

        OPCODES.put("$var", 31);
        OPCODES.put("$int", 32);
    }

    public NBAssembler(File sourceFile, File destFile, byte[] contents) throws IOException {
        this.contents = new String(contents, StandardCharsets.UTF_8);
        this.destFile = destFile;

        this.logger.info("Assembling %s into %s", sourceFile.getName(), destFile.getName());

        this.destFile.createNewFile();
    }

    public void assemble() throws IOException, AssemblyException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        long start = System.currentTimeMillis();

        List<String> tokens = new LinkedList<>();

        // Use a matcher to split input by space,
        // and allowing spaces in arguments via quotes
        {
            boolean commentMode = false;

            Matcher m = TOKEN_PATTERN.matcher(this.contents);

            while (m.find()) {
                String token = m.group(1).trim();

                if (token.equals("/*")) {
                    commentMode = true;
                } else if (token.equals("*/")) {
                    commentMode = false;
                } else if (!commentMode) {
                    tokens.add(token);
                }
            }
        }

        Map<String, Integer> labels = new HashMap<>();

        Map<Integer, String> futureLabels = new HashMap<>();

        for (String token : tokens) {
            // DIV 4 because integers are worth 4 bytes.
            int opPos = out.size() / 4;

            int opValue = OPCODES.getOrDefault(token.toLowerCase(), -1);

            if (opValue == -1) {
                if (token.equals(";")) {
                    this.logger.debug("Found ';'.");
                } else if (token.equals("null")) {
                    this.logger.debug("Found null.");
                    out.writeInt(0);
                } else if (token.matches(LABEL_DECLARATION_REGEX)) {
                    this.logger.debug("Found label declaration: %s", token);
                    String label = token.substring(0, token.length() - 1);

                    out.writeInt(0); // No-OP in it's place.
                    labels.put(label, opPos);
                } else if (token.matches(TEXT_REGEX)) {
                    this.logger.debug("Found string: %s", token);
                    String text = unescapeString(token.substring(1, token.length() - 1));

                    for (char c : text.toCharArray()) {
                        out.writeInt(c);
                    }

                    out.writeInt(0); // NULL terminated.
                } else if (token.matches(LABEL_REGEX)) {
                    String label = token.substring(1, token.length() - 1);

                    // We'll make a second pass and overwrite the label.
                    out.writeInt(0); // Temporary No-OP in it's place.
                    futureLabels.put(opPos, label);
                    this.logger.debug("Found label: %s", token);
                } else if (token.matches(NUMBER_REGEX)) {
                    this.logger.debug("Found number symbol: %s", token);
                    String num = token.substring(1, token.length() - 1);

                    out.writeInt(getNumber(num));
                } else if (token.matches(REGISTER_REGEX)) {
                    this.logger.debug("Found register symbol: %s", token);
                    int ordinal = this.registerToOrdinal(token);

                    out.writeInt(ordinal);
                } else {
                    try {
                        int number = getNumber(token);

                        this.logger.debug("Found number literal: %s (%d)", token, number);

                        out.writeInt(number);
                    } catch (NumberFormatException nfe) {
                        this.logger.severe("Unknown symbol: %s", token);
                        throw new AssemblyException();
                    }
                }
            } else {
                this.logger.debug("Found opcode: %s", token);
                out.writeInt(opValue);
            }
        }

        byte[] bytes = baos.toByteArray();

        // Resolve labels.
        for (Map.Entry<Integer, String> entry : futureLabels.entrySet()) {
            int startPosition = entry.getKey() * 4;
            String label = entry.getValue();
            int labelLocation = labels.getOrDefault(label, -1);

            if (labelLocation == -1) {
                this.logger.severe("Unknown label: %s", label);
                throw new AssemblyException();
            } else {
                byte[] labelLocBytes = intToBytes(labelLocation);

                bytes[startPosition] = labelLocBytes[0];
                bytes[startPosition + 1] = labelLocBytes[1];
                bytes[startPosition + 2] = labelLocBytes[2];
                bytes[startPosition + 3] = labelLocBytes[3];
            }
        }

        Files.write(this.destFile.toPath(), bytes);

        long end = System.currentTimeMillis();

        this.logger.info("Finished assembling! (Took %d ms)", end - start);
    }

    private static int getNumber(String num) throws NumberFormatException {
        if (num.startsWith("0x")) {
            return Integer.parseInt(num.substring(2), 16);
        } else if (num.startsWith("0b")) {
            return Integer.parseInt(num.substring(2), 2);
        } else {
            return Integer.parseInt(num);
        }
    }

    private static String unescapeString(String str) {
        return str
            .replace("\\\\", "\\")     // \ <- \\
            .replace("\\\"", "\"")     // " <- \"
            .replace("\\b", "\b")      // ? <- \b
            .replace("\\f", "\f")      // ? <- \f
            .replace("\\n", "\n")      // ? <- \n
            .replace("\\r", "\r")      // ? <- \r
            .replace("\\t", "\t")      // ? <- \t
            .replace("\\v", "\u000b")  // ? <- \v
            .replace("\\'", "'");      // ' <- \'
    }

    private int registerToOrdinal(String str) throws AssemblyException {
        switch (str) {
            case "#a":
                return 0;
            case "#b":
                return 1;
            case "#c":
                return 2;
            case "#d":
                return 3;
            case "#e":
                return 4;
            case "#f":
                return 5;
            case "#g":
                return 6;
            case "#h":
                return 7;
            case "#i":
                return 8;
            case "#j":
                return 9;
            case "#k":
                return 10;
            case "#l":
                return 11;
            case "#m":
                return 12;
            case "#n":
                return 13;
            case "#o":
                return 14;
            case "#p":
                return 15;

            default:
                this.logger.severe("Unknown register: #%s", str);
                throw new AssemblyException();
        }
    }

    private static byte[] intToBytes(int val) {
        return ByteBuffer.allocate(4).putInt(val).array();
    }

}
