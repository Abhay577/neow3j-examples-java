package io.neow3j.examples.contract_development;

import io.neow3j.compiler.CompilationUnit;
import io.neow3j.compiler.Compiler;
import io.neow3j.contract.NefFile;
import io.neow3j.contract.SmartContract;
import io.neow3j.examples.contract_development.contracts.BongoCatToken;
import io.neow3j.model.NeoConfig;
import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.ObjectMapperFactory;
import io.neow3j.protocol.core.methods.response.ContractManifest;
import io.neow3j.protocol.core.methods.response.NeoSendRawTransaction;
import io.neow3j.protocol.http.HttpService;
import io.neow3j.transaction.Signer;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

// Shows how to read a smart contract's files from the disk and deployed it on through a local
// neo-node.
public class DeployFromFiles {

    public static void main(String[] args) throws Throwable {

        // Set the magic number according to the Neo network's configuration. It is used when
        // signing transactions.
        NeoConfig.setMagicNumber(new byte[]{0x01, 0x03, 0x00, 0x0}); // Magic number 769

        // Set up the connection to the neo-node and the wallet for signing the transaction.
        Neow3j neow3j = Neow3j.build(new HttpService("http://localhost:40332"));
        Account a = Account.fromWIF("L1WMhxazScMhUrdv34JqQb1HFSQmWeN2Kpc1R9JGKwL7CDNP21uR");
        Wallet w = Wallet.withAccounts(a);

        // Retrieve the BongotCatToken contract files and construct a SmartContract object from it.
        NefFile nefFile = NefFile.readFromFile(new File("./build/neow3j/CustomObjectContract.nef"));
        ContractManifest manifest;
        try (FileInputStream s = new FileInputStream(
                "./build/neow3j/CustomObjectContract.manifest.json")) {
            manifest = ObjectMapperFactory.getObjectMapper().readValue(s, ContractManifest.class);
        }
        SmartContract sc = new SmartContract(nefFile, manifest, neow3j);

        // Deploy the contract's NEF and manifest. This creates, signs and send a transaction to
        // the neo-node.
        NeoSendRawTransaction response = sc.deploy()
                .wallet(w)
                .signers(Signer.calledByEntry(a.getScriptHash()))
                .sign()
                .send();

        if (response.hasError()) {
            System.out.printf("Deployment was not successful. Error message from neo-node "
                    + "was: '%s'\n", response.getError().getMessage());
        }
        System.out.printf("Script hash of the deployd contract: %s\n", sc.getScriptHash());
    }

}
