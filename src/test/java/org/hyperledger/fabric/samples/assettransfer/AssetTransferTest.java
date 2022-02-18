/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

public final class AssetTransferTest {

    private final class MockKeyValue implements KeyValue {

        private final String key;
        private final String value;

        MockKeyValue(final String key, final String value) {
            super();
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getStringValue() {
            return this.value;
        }

        @Override
        public byte[] getValue() {
            return this.value.getBytes();
        }

    }

    private final class MockAssetResultsIterator implements QueryResultsIterator<KeyValue> {

        private final List<KeyValue> assetList;

        MockAssetResultsIterator() {
            super();

            assetList = new ArrayList<KeyValue>();

            assetList.add(new MockKeyValue("asset1",
                    "{ \"keyId\": \"asset1\", \"reviewUser\": \"blue\", \"reviewDate\": \"blue\", \"reviewDescription\": \"Tomoko\"}"));
            assetList.add(new MockKeyValue("asset2",
                    "{ \"keyId\": \"asset2\", \"reviewUser\": \"red\", \"reviewDate\": \"blue\",\"reviewDescription\": \"Brad\"}"));
            assetList.add(new MockKeyValue("asset3",
                    "{ \"keyId\": \"asset3\", \"reviewUser\": \"green\", \"reviewDate\": \"blue\",\"reviewDescription\": \"Jin Soo\"}"));
            assetList.add(new MockKeyValue("asset4",
                    "{ \"keyId\": \"asset4\", \"reviewUser\": \"yellow\", \"reviewDate\": \"blue\",\"reviewDescription\": \"Max\"}"));
            assetList.add(new MockKeyValue("asset5",
                    "{ \"keyId\": \"asset5\", \"reviewUser\": \"black\", \"reviewDate\": \"blue\",\"reviewDescription\": \"Adrian\"}"));
            assetList.add(new MockKeyValue("asset6",
                    "{ \"keyId\": \"asset6\", \"reviewUser\": \"white\", \"reviewDate\": \"blue\",\"reviewDescription\": \"Michel\"}"));
        }

        @Override
        public Iterator<KeyValue> iterator() {
            return assetList.iterator();
        }

        @Override
        public void close() throws Exception {
            // do nothing
        }

    }

    @Test
    public void invokeUnknownTransaction() {
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);

        Throwable thrown = catchThrowable(() -> {
            contract.unknownTransaction(ctx);
        });

        assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                .hasMessage("Undefined contract method called");
        assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo(null);

        verifyZeroInteractions(ctx);
    }

    @Nested
    class InvokeReadAssetTransaction {

        @Test
        public void whenAssetExists() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("asset1"))
                    .thenReturn("{ \"keyId\": \"asset1\",  \"reviewUser\": \"white\", \"reviewDate\": \"blue\",\"reviewDescription\": \"Michel\"}");

            Asset asset = contract.ReadAsset(ctx, "asset1");

            assertThat(asset).isEqualTo(new Asset("asset1", "white", "blue", "Michel"));
        }

        @Test
        public void whenAssetDoesNotExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("asset1")).thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.ReadAsset(ctx, "asset1");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Asset asset1 does not exist");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_NOT_FOUND".getBytes());
        }
    }

    @Test
    void invokeInitLedgerTransaction() {
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        when(ctx.getStub()).thenReturn(stub);

        contract.InitLedger(ctx);

        InOrder inOrder = inOrder(stub);
//       inOrder.verify(stub).putStringState("asset1", "{\"reviewUser\":\"blue\",\"reviewDate\":\"blue\",\"reviewDescription\":\"Tomoko\"}");
    }

    @Nested
    class InvokeCreateAssetTransaction {

        @Test
        public void whenAssetExists() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("asset1"))
                    .thenReturn("{ \"keyId\": \"asset1\", \"reviewUser\":\"blue\",\"reviewDate\":\"blue\",\"reviewDescription\":\"Tomoko\"}");

            Throwable thrown = catchThrowable(() -> {
                contract.CreateAsset(ctx, "asset1", "blue", "444", "555");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Asset asset1 already exists");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_ALREADY_EXISTS".getBytes());
        }

        @Test
        public void whenAssetDoesNotExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("asset1")).thenReturn("");

            Asset asset = contract.CreateAsset(ctx, "asset1", "blue", "", "");

            assertThat(asset).isEqualTo(new Asset("asset1", "blue", "", ""));
        }
    }

    @Test
    void invokeGetAllAssetsTransaction() {
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        when(ctx.getStub()).thenReturn(stub);
        when(stub.getStateByRange("", "")).thenReturn(new MockAssetResultsIterator());

//        String assets = contract.GetAllAssets(ctx);
//
//        assertThat(assets).isEqualTo("[{\"appraisedValue\":300,\"assetID\":\"asset1\",\"color\":\"blue\",\"owner\":\"Tomoko\",\"size\":5},"
//                + "{\"appraisedValue\":400,\"assetID\":\"asset2\",\"color\":\"red\",\"owner\":\"Brad\",\"size\":5},"
//                + "{\"appraisedValue\":500,\"assetID\":\"asset3\",\"color\":\"green\",\"owner\":\"Jin Soo\",\"size\":10},"
//                + "{\"appraisedValue\":600,\"assetID\":\"asset4\",\"color\":\"yellow\",\"owner\":\"Max\",\"size\":10},"
//                + "{\"appraisedValue\":700,\"assetID\":\"asset5\",\"color\":\"black\",\"owner\":\"Adrian\",\"size\":15},"
//                + "{\"appraisedValue\":800,\"assetID\":\"asset6\",\"color\":\"white\",\"owner\":\"Michel\",\"size\":15}]");

    }

    @Nested
    class TransferAssetTransaction {

        @Test
        public void whenAssetExists() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState("asset1"))
//                    .thenReturn("{ \"assetID\": \"asset1\", \"color\": \"blue\", \"size\": 5, \"owner\": \"Tomoko\", \"appraisedValue\": 300 }");
//
//            Asset asset = contract.TransferAsset(ctx, "asset1", "Dr Evil");
//
//            assertThat(asset).isEqualTo(new Asset("asset1", "blue", "", ""));
        }

        @Test
        public void whenAssetDoesNotExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState("asset1")).thenReturn("");
//
//            Throwable thrown = catchThrowable(() -> {
//                contract.TransferAsset(ctx, "asset1", "Dr Evil");
//            });
//
//            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
//                    .hasMessage("Asset asset1 does not exist");
//            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_NOT_FOUND".getBytes());
        }
    }

    @Nested
    class UpdateAssetTransaction {

        @Test
        public void whenAssetExists() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState("asset1"))
//                    .thenReturn("{ \"assetID\": \"asset1\", \"color\": \"blue\", \"size\": 45, \"owner\": \"Arturo\", \"appraisedValue\": 60 }");
//
//            Asset asset = contract.UpdateAsset(ctx, "asset1", "pink", "", "");
//
//            assertThat(asset).isEqualTo(new Asset("asset1", "pink", "", ""));
        }

        @Test
        public void whenAssetDoesNotExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState("asset1")).thenReturn("");
//
//            Throwable thrown = catchThrowable(() -> {
//                contract.TransferAsset(ctx, "asset1", "Alex");
//            });
//
//            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
//                    .hasMessage("Asset asset1 does not exist");
//            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_NOT_FOUND".getBytes());
        }
    }

    @Nested
    class DeleteAssetTransaction {

        @Test
        public void whenAssetDoesNotExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
//            when(ctx.getStub()).thenReturn(stub);
//            when(stub.getStringState("asset1")).thenReturn("");
//
//            Throwable thrown = catchThrowable(() -> {
//                contract.DeleteAsset(ctx, "asset1");
//            });
//
//            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
//                    .hasMessage("Asset asset1 does not exist");
//            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_NOT_FOUND".getBytes());
        }
    }
}
