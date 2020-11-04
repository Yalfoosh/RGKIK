package hr.fer.rgkk.transactions;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;

/**
 * You must implement locking and unlocking script such that transaction output is locked by 2 integers x and y
 * such that they are solution to the equation system:
 * <pre>
 *     x + y = first four digits of your student id
 *     abs(x-y) = last four digits of your student id
 * </pre>
 * If needed change last digit of your student id such that x and y have same parity. This is needed so that equation
 * system has integer solutions.
 */
public class LinearEquationTransaction extends ScriptTransaction {

    private final int firstFourDigits = 36;
    private final int lastFourDigits = 1442;

    // JMBAG: 0036XX1442
    // x + y   = 36
    // |x - y| = 1442
    // -----------------
    // x            = 36 - y
    // |36 - y - y| = 1442
    // |36 - 2y|    = 1442
    // |18 - y|     = 721
    // y            = 721 + 18
    // y            = 739
    private final int y = 739;

    // x            = 36 - 739
    // x            = -703
    private final int x = -703;


    public LinearEquationTransaction(WalletKit walletKit, NetworkParameters parameters) {
        super(walletKit, parameters);
    }

    @Override
    public Script createLockingScript() {
        return new ScriptBuilder().op(ScriptOpCodes.OP_2DUP)        // Duplicate top 2 items
                                  .op(ScriptOpCodes.OP_ADD)         // Add top 2 items
                                  .number(firstFourDigits)          // Add the first four digits as int to stack
                                  .op(ScriptOpCodes.OP_EQUALVERIFY) // Verify that they are equal
                                  .op(ScriptOpCodes.OP_SUB)         // Then subtract top 2 items
                                  .op(ScriptOpCodes.OP_ABS)         // get their absolute value
                                  .number(lastFourDigits)           // Add the last four digits as int to stack
                                  .op(ScriptOpCodes.OP_EQUAL)       // And verify that they are equal
                                  .build();
    }

    @Override
    public Script createUnlockingScript(Transaction unsignedScript) {
        // The order is reversed so x is the first stack element :)
        return new ScriptBuilder().number(y)    // Add y
                                  .number(x)    // Add x
                                  .build();
    }
}
