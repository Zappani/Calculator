package br.te914.MyCalc;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvVisor;
    private String currentInput = "";
    private String operator = "";
    private double firstNumber = 0;
    private boolean isOperatorPressed = false;
    private boolean isResultDisplayed = false;
    private boolean waitingForSecondNumber = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvVisor = findViewById(R.id.tvVisor);
        tvVisor.setText("0");
    }

    public void onClickButton(View view) {
        Button button = (Button) view;
        String buttonText = button.getText().toString();

        switch (buttonText) {
            case "AC":
                clearAll();
                break;
            case "<--":
                backspace();
                break;
            case "=":
                calculateResult();
                break;
            case "+":
            case "-":
            case "*":
            case "/":
                handleOperator(buttonText);
                break;
            case "+/-":
                changeSign();
                break;
            case ".":
                addDecimalPoint();
                break;
            case "x^n":
                handleOperator("^");
                break;
            case "TC":
                convertAnnualToMonthlyRate();
                break;
            default:
                // Números 0-9
                if (buttonText.matches("[0-9]") && currentInput.length() < 20) {
                    // Só adiciona se o visor tiver menos de 20 caracteres
                     handleNumber(buttonText);

                }
                break;
        }
    }

    private void handleNumber(String number) {
        if (isResultDisplayed) {
            currentInput = "";
            isResultDisplayed = false;
        }

        if (waitingForSecondNumber) {
            currentInput = "";
            waitingForSecondNumber = false;
        }

        // Evita zeros à esquerda desnecessários
        if (currentInput.equals("0")) {
            currentInput = number;
        } else if (currentInput.length() < 19) {  // <-- Limite aplicado
            currentInput += number;
        }

        updateDisplay(currentInput);
    }

    private void handleOperator(String op) {
        if (!currentInput.isEmpty()) {
            // Se já tem uma operação pendente, calcula primeiro
            if (waitingForSecondNumber && !operator.isEmpty()) {
                calculateResult();
            }

            try {
                firstNumber = Double.parseDouble(currentInput);
                operator = op;
                waitingForSecondNumber = true;
                isOperatorPressed = true;
                isResultDisplayed = false;

                // Para mostrar visualmente que o operador foi pressionado
                // você pode adicionar aqui uma indicação visual se desejar

            } catch (NumberFormatException e) {
                updateDisplay("Error");
                clearAll();
            }
        }
    }

    private void calculateResult() {
        if (!operator.isEmpty() && !currentInput.isEmpty()) {
            try {
                double secondNumber = Double.parseDouble(currentInput);
                double result = 0;

                switch (operator) {
                    case "+":
                        result = firstNumber + secondNumber;
                        break;
                    case "-":
                        result = firstNumber - secondNumber;
                        break;
                    case "*":
                        result = firstNumber * secondNumber;
                        break;
                    case "/":
                        if (secondNumber != 0) {
                            result = firstNumber / secondNumber;
                        } else {
                            updateDisplay("Error: Div/0");
                            return;
                        }
                        break;
                    case "^":
                        if (firstNumber == 0 && secondNumber == 0) {
                            updateDisplay("Error: ?");
                            return;
                        } else if (firstNumber != 0 && secondNumber == 0) {
                            updateDisplay("1");
                            return;
                        } else {
                            result = Math.pow(firstNumber, secondNumber);
                        }
                        break;

                }

                // Verifica se o resultado é válido
                if (Double.isNaN(result) || Double.isInfinite(result)) {
                    updateDisplay("Error");
                    return;
                }

                // Formatar resultado
                String resultString = formatResult(result);
                updateDisplay(resultString);
                currentInput = resultString;
                firstNumber = result; // Permite operações em cadeia
                operator = "";
                waitingForSecondNumber = false;
                isResultDisplayed = true;

            } catch (NumberFormatException e) {
                updateDisplay("Error");
                clearAll();
            }
        }
    }

    private void convertAnnualToMonthlyRate() {
        if (!currentInput.isEmpty()) {
            try {
                // Pega a taxa anual (em formato decimal, ex: 0.12 para 12%)
                double annualRate = Double.parseDouble(currentInput);

                // Aplica a fórmula: Imensal = ((1 + Ianual)^(1/12)) - 1
                double monthlyRate = Math.pow(1 + annualRate, 1.0/12.0) - 1;

                // Verifica se o resultado é válido
                if (Double.isNaN(monthlyRate) || Double.isInfinite(monthlyRate)) {
                    updateDisplay("Error");
                    clearAll();
                    return;
                }

                // Formata o resultado (geralmente queremos ver com mais precisão para taxas)
                String resultString = formatRateResult(monthlyRate);
                updateDisplay(resultString);
                currentInput = resultString;
                isResultDisplayed = true;

            } catch (NumberFormatException e) {
                updateDisplay("Error");
                clearAll();
            }
        }
    }

    private String formatRateResult(double rate) {

        if (rate == 0) {
            return "0";
        }

        if (Math.abs(rate) < 0.000001) {
            return String.format("%.4e", rate);
        }

        String formatted = String.format("%.6f", rate);
        formatted = formatted.replaceAll("0*$", "");
        formatted = formatted.replaceAll("\\.$", "");
        return formatted;
    }

    private void changeSign() {
        if (!currentInput.isEmpty() && !currentInput.equals("0")) {
            try {
                double value = Double.parseDouble(currentInput);
                value = -value;
                currentInput = formatResult(value);
                updateDisplay(currentInput);
            } catch (NumberFormatException e) {
                // Se não conseguir converter, tenta adicionar/remover o sinal manualmente
                if (currentInput.startsWith("-")) {
                    currentInput = currentInput.substring(1);
                } else {
                    currentInput = "-" + currentInput;
                }
                updateDisplay(currentInput);
            }
        }
    }

    private void addDecimalPoint() {
        // Se acabou de mostrar um resultado, começa um novo número
        if (isResultDisplayed) {
            currentInput = "0";
            isResultDisplayed = false;
        }

        // Se está esperando o segundo número, começa um novo
        if (waitingForSecondNumber && isOperatorPressed) {
            currentInput = "0";
            isOperatorPressed = false;
        }

        // Se currentInput está vazio, inicializa com "0"
        if (currentInput.isEmpty()) {
            currentInput = "0";
        }

        // Adiciona o ponto decimal se ainda não existe
        if (!currentInput.contains(".")) {
            currentInput += ".";
            updateDisplay(currentInput);
        }
    }

    private void backspace() {
        if (!currentInput.isEmpty() && !isResultDisplayed) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            if (currentInput.isEmpty() || currentInput.equals("-")) {
                currentInput = "0";
            }
            updateDisplay(currentInput);
        }
    }

    private void clearAll() {
        currentInput = "";
        operator = "";
        firstNumber = 0;
        isOperatorPressed = false;
        isResultDisplayed = false;
        waitingForSecondNumber = false;
        updateDisplay("0");
    }

    private void updateDisplay(String text) {
        // Se o texto está vazio, mostra "0"
        if (text.isEmpty()) {
            tvVisor.setText("0");
        } else {
            tvVisor.setText(text);
        }
    }

    private String formatResult(double result) {
        // Verifica se o número é muito grande ou muito pequeno
        if (Math.abs(result) >= 1e9 || (Math.abs(result) < 1e-8 && result != 0)) {
            // Usa notação científica para números muito grandes ou muito pequenos
            return String.format("%.4e", result);
        }

        // Se o resultado é um número inteiro, mostra sem casas decimais
        if (result == (long) result && Math.abs(result) < 1e9) {
            return String.valueOf((long) result);
        } else {
            // Limita a 8 casas decimais e remove zeros desnecessários
            String formatted = String.format("%.8f", result);
            // Remove zeros à direita
            formatted = formatted.replaceAll("0*$", "");
            // Remove o ponto se não há casas decimais
            formatted = formatted.replaceAll("\\.$", "");
            return formatted;
        }
    }
}