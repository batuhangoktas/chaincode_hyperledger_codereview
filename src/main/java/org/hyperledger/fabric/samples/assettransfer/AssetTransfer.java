/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.util.ArrayList;
import java.util.List;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

@Contract(
        name = "basic",
        info = @Info(
                title = "Asset Transfer",
                description = "The hyperlegendary asset transfer",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "batuhangoktas@hotmail.com",
                        name = "Batuhan GÖKTAŞ",
                        url = "batuhangoktas.com")))
@Default
public final class AssetTransfer implements ContractInterface {

    private final Genson genson = new Genson();

    private enum AssetTransferErrors {
        ASSET_NOT_FOUND,
        ASSET_ALREADY_EXISTS
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        CreateAsset(ctx, "firstId", "firstowner", "123", "First Desc");

    }


    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset CreateAsset(final Context ctx, final String keyId, final String reviewUser, final String reviewDate, final String reviewDescription) {
        ChaincodeStub stub = ctx.getStub();

        if (AssetExists(ctx, keyId)) {
            String errorMessage = String.format("Asset %s already exists", keyId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        Asset asset = new Asset(keyId, reviewUser, reviewDate, reviewDescription);
        String assetJSON = genson.serialize(asset);
        stub.putStringState(keyId, assetJSON);

        return asset;
    }


    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Asset ReadAsset(final Context ctx, final String assetID) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(assetID);

        if (assetJSON == null || assetJSON.isEmpty()) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Asset asset = genson.deserialize(assetJSON, Asset.class);
        return asset;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset UpdateAsset(final Context ctx, final String keyId, final String reviewUser, final String reviewDate, final String reviewDescription) {
        ChaincodeStub stub = ctx.getStub();

        if (!AssetExists(ctx, keyId)) {
            String errorMessage = String.format("Asset %s does not exist", keyId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Asset newAsset = new Asset(keyId, reviewUser, reviewDate, reviewDescription);
        String newAssetJSON = genson.serialize(newAsset);
        stub.putStringState(keyId, newAssetJSON);

        return newAsset;
    }


    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void DeleteAsset(final Context ctx, final String assetID) {
        ChaincodeStub stub = ctx.getStub();

        if (!AssetExists(ctx, assetID)) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        stub.delState(assetID);
    }


    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean AssetExists(final Context ctx, final String assetID) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(assetID);

        return (assetJSON != null && !assetJSON.isEmpty());
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset TransferAsset(final Context ctx, final String assetID, final String newOwner) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(assetID);

        if (assetJSON == null || assetJSON.isEmpty()) {
            String errorMessage = String.format("Asset %s does not exist", assetID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Asset asset = genson.deserialize(assetJSON, Asset.class);

        Asset newAsset = new Asset(asset.getKeyId(), newOwner, asset.getReviewDate(), asset.getReviewDescription());
        String newAssetJSON = genson.serialize(newAsset);
        stub.putStringState(assetID, newAssetJSON);

        return newAsset;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllAssets(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Asset> queryResults = new ArrayList<Asset>();

        // To retrieve all assets from the ledger use getStateByRange with empty startKey & endKey.
        // Giving empty startKey & endKey is interpreted as all the keys from beginning to end.
        // As another example, if you use startKey = 'asset0', endKey = 'asset9' ,
        // then getStateByRange will retrieve asset with keys between asset0 (inclusive) and asset9 (exclusive) in lexical order.
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            Asset asset = genson.deserialize(result.getStringValue(), Asset.class);
            queryResults.add(asset);
            System.out.println(asset.toString());
        }

        final String response = genson.serialize(queryResults);

        return response;
    }

@Transaction(intent = Transaction.TYPE.SUBMIT)
    public String ReviewSubmit(final Context ctx, final String keyId, final String reviewUser, final String reviewDate, final String reviewDescription) {
        ChaincodeStub stub = ctx.getStub();


            Asset asset = new Asset(keyId, reviewUser, reviewDate, reviewDescription);
            String assetJSON = genson.serialize(asset);

            final String compositeKey = stub.createCompositeKey(keyId, reviewUser).toString();
            stub.putStringState(compositeKey, assetJSON); // use this

            return compositeKey;
    }

@Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetReviewList(final Context ctx, final String keyId) {
        ChaincodeStub stub = ctx.getStub();

        List<Asset> queryResults = new ArrayList<Asset>();

        // To retrieve all assets from the ledger use getStateByRange with empty startKey & endKey.
        // Giving empty startKey & endKey is interpreted as all the keys from beginning to end.
        // As another example, if you use startKey = 'asset0', endKey = 'asset9' ,
        // then getStateByRange will retrieve asset with keys between asset0 (inclusive) and asset9 (exclusive) in lexical order.
        QueryResultsIterator<KeyValue> results = stub.getStateByPartialCompositeKey(keyId);

        for (KeyValue result: results) {
            Asset asset = genson.deserialize(result.getStringValue(), Asset.class);
            queryResults.add(asset);
            System.out.println(asset.toString());
        }

        final String response = genson.serialize(queryResults);

        return response;
    }

}
