package hr.fer.rgkk.transactions;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.script.Script;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ScriptTest {

    private WalletKit walletKit;
    private NetworkParameters networkParameters;

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptTest.class);

    public ScriptTest() {
        String walletName = "wallet";
        this.networkParameters = RegTestParams.get();
        this.walletKit = new WalletKit(networkParameters, new File(walletName), "password");
    }

    @Test
    public void printAddress() {
        LOGGER.info("Importing key");
        LOGGER.info("Your address is {}", walletKit.getWallet().currentReceiveAddress());
        LOGGER.info("Your balance is {}", walletKit.getWallet().getBalance());
        walletKit.close();
    }

    private void testTransaction(ScriptTransaction scriptTransaction) throws InsufficientMoneyException {
        Script lockingScript = scriptTransaction.createLockingScript();
        Transaction transaction = scriptTransaction.createOutgoingTransaction(lockingScript, Coin.CENT);
        transaction.getOutputs().stream()
                .filter(to -> to.getScriptPubKey().equals(lockingScript))
                .findAny()
                .ifPresent(relevantOutput -> {
                    Transaction unlockingTransaction = scriptTransaction.createUnsignedUnlockingTransaction(relevantOutput, scriptTransaction.getReceiveAddress());
                    Script unlockingScript = scriptTransaction.createUnlockingScript(unlockingTransaction);
                    scriptTransaction.testScript(lockingScript, unlockingScript, unlockingTransaction);
                    unlockingTransaction.getInput(0).setScriptSig(unlockingScript);
                    scriptTransaction.sendTransaction(transaction);
                    scriptTransaction.sendTransaction(unlockingTransaction);
                });
    }

    //////////////////////////////////
    // Pay to public key hash tests //
    //////////////////////////////////

    @Test
    public void testPayToPubKey() {
        try (ScriptTransaction payToPubKey = new PayToPubKey(walletKit, networkParameters)) {
            testTransaction(payToPubKey);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }


    //////////////////////////////////
    // Pay to public key hash tests //
    //////////////////////////////////

    @Test
    public void testPayToPubKeyHash() {
        try (ScriptTransaction payToPubKeyHash = new PayToPubKeyHash(walletKit, networkParameters)) {
            testTransaction(payToPubKeyHash);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    //////////////////////////////////
    // Linear equation tests        //
    //////////////////////////////////

    @Test
    public void testLinearEquation() {
        try (LinearEquationTransaction linEq = new LinearEquationTransaction(walletKit, networkParameters)) {
            testTransaction(linEq);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    //////////////////////////////////
    // Odds and even tests          //
    //////////////////////////////////

    @Test
    public void testEvenPlayerWinsWithTwoZeros() {
        try (ScriptTransaction oddsAndEvens = OddsAndEvens.of(
                walletKit,
                networkParameters,
                OddsAndEvens.OddsEvenChoice.ZERO,
                OddsAndEvens.OddsEvenChoice.ZERO,
                OddsAndEvens.WinningPlayer.EVEN
        )) {
            testTransaction(oddsAndEvens);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testOddPlayerLoosesWithTwoZeros() {
        try (ScriptTransaction oddsAndEvens = OddsAndEvens.of(
                walletKit,
                networkParameters,
                OddsAndEvens.OddsEvenChoice.ZERO,
                OddsAndEvens.OddsEvenChoice.ZERO,
                OddsAndEvens.WinningPlayer.ODD
        )) {
            testTransaction(oddsAndEvens);
            Assert.fail("Odd player should loose.");
        } catch (Exception ignore) {
        }
    }

    @Test
    public void testEvenPlayerWinsWithTwoOnes() {
        try (ScriptTransaction oddsAndEvens = OddsAndEvens.of(
                walletKit,
                networkParameters,
                OddsAndEvens.OddsEvenChoice.ONE,
                OddsAndEvens.OddsEvenChoice.ONE,
                OddsAndEvens.WinningPlayer.EVEN
        )) {
            testTransaction(oddsAndEvens);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testOddPlayerLoosesWithTwoOnes() {
        try (ScriptTransaction oddsAndEvens = OddsAndEvens.of(
                walletKit,
                networkParameters,
                OddsAndEvens.OddsEvenChoice.ONE,
                OddsAndEvens.OddsEvenChoice.ONE,
                OddsAndEvens.WinningPlayer.ODD
        )) {
            testTransaction(oddsAndEvens);
            Assert.fail("Odd player should loose.");
        } catch (Exception ignore) {
        }
    }

    @Test
    public void testEvenPlayerLoosesWithZeroAndOne() {
        try (ScriptTransaction oddsAndEvens = OddsAndEvens.of(
                walletKit,
                networkParameters,
                OddsAndEvens.OddsEvenChoice.ZERO,
                OddsAndEvens.OddsEvenChoice.ONE,
                OddsAndEvens.WinningPlayer.EVEN
        )) {
            testTransaction(oddsAndEvens);
            Assert.fail("Even player should loose");
        } catch (Exception ignore) {
        }
    }

    @Test
    public void testOddPlayerWinsWithZeroAndOne() {
        try (ScriptTransaction oddsAndEvens = OddsAndEvens.of(
                walletKit,
                networkParameters,
                OddsAndEvens.OddsEvenChoice.ZERO,
                OddsAndEvens.OddsEvenChoice.ONE,
                OddsAndEvens.WinningPlayer.ODD
        )) {
            testTransaction(oddsAndEvens);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testEvenPlayerLoosesWithOneAndZero() {
        try (ScriptTransaction oddsAndEvens = OddsAndEvens.of(
                walletKit,
                networkParameters,
                OddsAndEvens.OddsEvenChoice.ONE,
                OddsAndEvens.OddsEvenChoice.ZERO,
                OddsAndEvens.WinningPlayer.EVEN
        )) {
            testTransaction(oddsAndEvens);
            Assert.fail("Even player should loose");
        } catch (Exception ignore) {
        }
    }

    @Test
    public void testOddPlayerWinsWithOneAndZero() {
        try (ScriptTransaction oddsAndEvens = OddsAndEvens.of(
                walletKit,
                networkParameters,
                OddsAndEvens.OddsEvenChoice.ONE,
                OddsAndEvens.OddsEvenChoice.ZERO,
                OddsAndEvens.WinningPlayer.ODD
        )) {
            testTransaction(oddsAndEvens);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

}
