package hr.fer.rgkk.transactions;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;

/**
 * You must implement standard Pay-2-Public-Key-Hash transaction type.
 */
public class PayToPubKeyHash extends ScriptTransaction {
    private final ECKey key;

    public PayToPubKeyHash(WalletKit walletKit, NetworkParameters parameters) {
        super(walletKit, parameters);

        key = getWallet().freshReceiveKey();
    }

    @Override
    public Script createLockingScript() {
        return new ScriptBuilder().op(ScriptOpCodes.OP_DUP)         // Duplicate the top of the stack
                                  .op(ScriptOpCodes.OP_HASH160)     // y = RIPEMD-160(SHA-256(stack[0]))
                                  .data(key.getPubKeyHash())        // Add hashed pub key
                                  .op(ScriptOpCodes.OP_EQUALVERIFY) // Check equality, then verify
                                  .op(ScriptOpCodes.OP_CHECKSIG)    // Check signature of top 2 items
                                  .build();
    }

    @Override
    public Script createUnlockingScript(Transaction unsignedTransaction) {
        TransactionSignature tSignature = sign(unsignedTransaction, key);

        return new ScriptBuilder().data(tSignature.encodeToBitcoin())   // Add key signature
                                  .data(key.getPubKey())                // Add pub key
                                  .build();
    }
}
