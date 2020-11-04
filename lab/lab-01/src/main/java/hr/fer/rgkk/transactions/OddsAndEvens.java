package hr.fer.rgkk.transactions;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import org.bitcoinj.core.Utils;

import java.security.SecureRandom;

/**
 * You must implement Odds and Evens game between two players such that only
 * the winner can unlock the coins.
 */
public class OddsAndEvens extends ScriptTransaction {

    // Alice's private key
    private final ECKey aliceKey;
    // Alice's nonce
    private final byte[] aliceNonce;
    // Bob's private key
    private final ECKey bobKey;
    // Bob's nonce
    private final byte[] bobNonce;

    // Key used in unlocking script to select winning player.
    // Winner is preselected here so that we can run tests.
    private final ECKey winningPlayerKey;

    private OddsAndEvens(
            WalletKit walletKit, NetworkParameters parameters,
            ECKey aliceKey, byte[] aliceNonce,
            ECKey bobKey, byte[] bobNonce,
            ECKey winningPlayerKey
    ) {
        super(walletKit, parameters);
        this.aliceKey = aliceKey;
        this.aliceNonce = aliceNonce;
        this.bobKey = bobKey;
        this.bobNonce = bobNonce;
        this.winningPlayerKey = winningPlayerKey;
    }

    @Override
    public Script createLockingScript() {
        ECKey winnerKey = (aliceNonce.length + bobNonce.length) % 2 == 0 ? aliceKey : bobKey;

        return new ScriptBuilder().op(ScriptOpCodes.OP_HASH160)             // Hash the aliceNonce on stack
                                  .data(Utils.sha256hash160(aliceNonce))    // Put the hashed aliceNonce on the stack
                                  .op(ScriptOpCodes.OP_EQUALVERIFY)         // Assert that they're equal, return otherwise
                                  .op(ScriptOpCodes.OP_HASH160)             // Hash the bobNonce on stack
                                  .data(Utils.sha256hash160(bobNonce))      // Put the hashed bobNonce on the stack
                                  .op(ScriptOpCodes.OP_EQUALVERIFY)         // Assert that they're equal, return otherwise
                                  .data(winnerKey.getPubKey())              // Put the calculated winner public key on the stack
                                  .op(ScriptOpCodes.OP_CHECKSIG)            // Check that the transaction signature is equal to the winner pub key
                                  .build();
    }

    @Override
    public Script createUnlockingScript(Transaction unsignedTransaction) {
        TransactionSignature signature = sign(unsignedTransaction, winningPlayerKey);
        return new ScriptBuilder()
                .data(signature.encodeToBitcoin())
                .data(bobNonce)   // Odds player
                .data(aliceNonce) // Evens player
                .build();
    }

    public static OddsAndEvens of(
            WalletKit walletKit, NetworkParameters parameters,
            OddsEvenChoice aliceChoice, OddsEvenChoice bobChoice,
            WinningPlayer winningPlayer
    ) {
        byte[] aliceNonce = randomBytes(16 + aliceChoice.value);
        byte[] bobNonce = randomBytes(16 + bobChoice.value);

        ECKey aliceKey = randKey();
        ECKey bobKey = randKey();

        // Alice is EVEN, bob is ODD
        ECKey winningPlayerKey = WinningPlayer.EVEN == winningPlayer ? aliceKey : bobKey;

        return new OddsAndEvens(
                walletKit, parameters,
                aliceKey, aliceNonce,
                bobKey, bobNonce,
                winningPlayerKey
        );
    }

    private static byte[] randomBytes(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    public enum WinningPlayer {
        ODD, EVEN
    }

    public enum OddsEvenChoice {

        ZERO(0),
        ONE(1);

        public final int value;

        OddsEvenChoice(int value) {
            this.value = value;
        }
    }
}

