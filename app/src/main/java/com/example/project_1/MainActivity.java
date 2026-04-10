package com.example.project_1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText etInput;
    private TextView tvResultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        etInput = findViewById(R.id.etInput);
        tvResultado = findViewById(R.id.tvResultado);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // ==================== Button Handlers ====================

    public void onNumberClick(View view) {
        Button b = (Button) view;
        String digit = b.getText().toString();
        String current = etInput.getText().toString();
        if (current.equals("0") && !digit.equals(".")) {
            etInput.setText(digit);
        } else {
            etInput.setText(current + digit);
        }
    }

    public void onOperationClick(View view) {
        Button b = (Button) view;
        String operator = b.getText().toString();
        String current = etInput.getText().toString();
        if (current.isEmpty()) return;
        char lastChar = current.charAt(current.length() - 1);
        if (lastChar == '+' || lastChar == '-' || lastChar == '×' || lastChar == '÷' || lastChar == '^') {
            current = current.substring(0, current.length() - 1);
        }
        etInput.setText(current + operator);
    }

    public void onPercentClick(View view) {
        String current = etInput.getText().toString();
        if (current.isEmpty()) return;

        // Correct % logic: Convert the current number to a percentage (e.g., 50 -> 0.5)
        try {
            // Find the last number in the expression
            Pattern p = Pattern.compile("(\\d+\\.?\\d*)$");
            Matcher m = p.matcher(current);
            if (m.find()) {
                String lastNumStr = m.group(1);
                double value = Double.parseDouble(lastNumStr);
                double percentValue = value / 100.0;
                
                String newExpression = current.substring(0, m.start()) + percentValue;
                etInput.setText(newExpression);
            }
        } catch (Exception e) {
            // Fallback to appending % if parsing fails
            etInput.setText(current + "%");
        }
    }

    public void onSqrtClick(View view) {
        String current = etInput.getText().toString();
        if (current.isEmpty()) {
            current = tvResultado.getText().toString();
            if (current.isEmpty()) return;
            etInput.setText(current);
        }
        etInput.setText(insertFunctionBeforeLastNumber(current, "√"));
    }

    public void onSinClick(View view) { insertFunction("sin("); }
    public void onCosClick(View view) { insertFunction("cos("); }
    public void onTanClick(View view) { insertFunction("tan("); }
    public void onLnClick(View view)  { insertFunction("ln("); }
    public void onLogClick(View view) { insertFunction("log("); }
    public void onExpClick(View view) { insertFunction("exp("); }

    public void onPowerClick(View view) {
        String current = etInput.getText().toString();
        if (current.isEmpty()) return;
        char last = current.charAt(current.length() - 1);
        if (last == '+' || last == '-' || last == '×' || last == '÷' || last == '^') {
            current = current.substring(0, current.length() - 1);
        }
        etInput.setText(current + "^");
    }

    public void onNthRootClick(View view) { insertFunction("root("); }

    public void onParenClick(View view) {
        Button b = (Button) view;
        String paren = b.getText().toString();
        etInput.setText(etInput.getText().toString() + paren);
    }

    public void onPiClick(View view) {
        etInput.setText(etInput.getText().toString() + "π");
    }

    public void onEClick(View view) {
        etInput.setText(etInput.getText().toString() + "e");
    }

    private void insertFunction(String func) {
        String current = etInput.getText().toString();
        if (current.isEmpty()) {
            current = tvResultado.getText().toString();
            if (current.isEmpty()) {
                etInput.setText(func);
                return;
            }
            etInput.setText(current);
        }
        etInput.setText(current + func);
    }

    public void onClearClick(View view) {
        String current = etInput.getText().toString();
        if (!current.isEmpty()) {
            etInput.setText(current.substring(0, current.length() - 1));
        }
    }

    public void onClearAllClick(View view) {
        etInput.setText("");
        tvResultado.setText("");
    }

    public void onEqualsClick(View view) {
        String expr = etInput.getText().toString();
        if (expr.isEmpty()) return;
        try {
            double result = evaluateExpression(expr);
            tvResultado.setText(formatResult(result));
        } catch (Exception e) {
            tvResultado.setText("Error");
        }
    }

    private String formatResult(double d) {
        if (d == (long) d)
            return String.format("%d", (long) d);
        else
            return String.valueOf(d);
    }

    // ==================== Expression Evaluation ====================

    private double evaluateExpression(String expr) {
        expr = expr.replace(" ", "");

        // Replace constants
        expr = expr.replace("π", String.valueOf(Math.PI));
        expr = expr.replace("e", String.valueOf(Math.E));

        // Replace percentage: number% -> (number/100)
        expr = expr.replaceAll("(\\d+\\.?\\d*)%", "($1/100)");

        // Replace square root: √number -> sqrt(number)
        expr = expr.replaceAll("√(\\d+\\.?\\d*)", "sqrt($1)");

        // Now parse and evaluate using a recursive descent parser
        return parseExpression(new Tokenizer(expr));
    }

    // Recursive descent parser
    private double parseExpression(Tokenizer tokens) {
        double result = parseTerm(tokens);
        while (tokens.peek() == '+' || tokens.peek() == '-') {
            char op = tokens.next();
            double right = parseTerm(tokens);
            if (op == '+') result += right;
            else result -= right;
        }
        return result;
    }

    private double parseTerm(Tokenizer tokens) {
        double result = parseFactor(tokens);
        while (tokens.peek() == '×' || tokens.peek() == '÷') {
            char op = tokens.next();
            double right = parseFactor(tokens);
            if (op == '×') result *= right;
            else result /= right;
        }
        return result;
    }

    private double parseFactor(Tokenizer tokens) {
        if (tokens.peek() == '+' || tokens.peek() == '-') {
            char op = tokens.next();
            double val = parseFactor(tokens);
            return op == '-' ? -val : val;
        }
        double result = parsePower(tokens);
        return result;
    }

    private double parsePower(Tokenizer tokens) {
        double result = parsePrimary(tokens);
        while (tokens.peek() == '^') {
            tokens.next(); // consume '^'
            double exponent = parsePrimary(tokens);
            result = Math.pow(result, exponent);
        }
        return result;
    }

    private double parsePrimary(Tokenizer tokens) {
        char c = tokens.peek();
        if (c == '(') {
            tokens.next(); // consume '('
            double val = parseExpression(tokens);
            tokens.expect(')');
            return val;
        } else if (Character.isDigit(c) || c == '.') {
            return tokens.nextNumber();
        } else if (Character.isLetter(c)) {
            // function name
            String func = tokens.nextFunction();
            tokens.expect('(');
            double arg = parseExpression(tokens);
            tokens.expect(')');
            switch (func) {
                case "sin": return Math.sin(Math.toRadians(arg));
                case "cos": return Math.cos(Math.toRadians(arg));
                case "tan": return Math.tan(Math.toRadians(arg));
                case "ln":  return Math.log(arg);
                case "log": return Math.log10(arg);
                case "exp": return Math.exp(arg);
                case "sqrt":return Math.sqrt(arg);
                case "root": {
                    double index = arg;
                    tokens.expect(',');
                    double value = parseExpression(tokens);
                    return Math.pow(value, 1.0 / index);
                }
                default: throw new RuntimeException("Unknown function: " + func);
            }
        } else {
            throw new RuntimeException("Unexpected character: " + c);
        }
    }

    // Helper tokenizer class
    private static class Tokenizer {
        private final String input;
        private int pos;

        Tokenizer(String input) { this.input = input; pos = 0; }

        char peek() {
            if (pos >= input.length()) return 0;
            return input.charAt(pos);
        }

        char next() {
            return input.charAt(pos++);
        }

        void expect(char expected) {
            if (peek() != expected) throw new RuntimeException("Expected '" + expected + "'");
            pos++;
        }

        double nextNumber() {
            int start = pos;
            while (pos < input.length() && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
                pos++;
            }
            return Double.parseDouble(input.substring(start, pos));
        }

        String nextFunction() {
            int start = pos;
            while (pos < input.length() && Character.isLetter(input.charAt(pos))) {
                pos++;
            }
            return input.substring(start, pos);
        }
    }

    // Helper: insert function before the last number (e.g., √)
    private String insertFunctionBeforeLastNumber(String expr, String func) {
        int i = expr.length() - 1;
        while (i >= 0 && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) i--;
        int start = i + 1;
        if (start == expr.length()) return expr + func;
        return expr.substring(0, start) + func + expr.substring(start);
    }
}