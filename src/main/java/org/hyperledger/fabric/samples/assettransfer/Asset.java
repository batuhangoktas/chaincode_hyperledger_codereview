/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.util.Objects;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class Asset {

    @Property()
    private final String keyId;
    @Property()
    private final String reviewUser;
    @Property()
    private final String reviewDate;
    @Property()
    private final String reviewDescription;

    public String getKeyId() {
        return keyId;
    }

    public String getReviewUser() {
        return reviewUser;
    }

    public String getReviewDate() {
        return reviewDate;
    }

    public String getReviewDescription() {
        return reviewDescription;
    }

    public Asset(@JsonProperty("keyId") final String keyId, @JsonProperty("reviewUser") final String reviewUser,
                 @JsonProperty("reviewDate") final String reviewDate, @JsonProperty("reviewDescription") final String reviewDescription) {
        this.keyId = keyId;
        this.reviewUser = reviewUser;
        this.reviewDate = reviewDate;
        this.reviewDescription = reviewDescription;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Asset other = (Asset) obj;

        return Objects.deepEquals(
                new String[] {getKeyId(), getReviewUser(), getReviewDate(), getReviewDescription()},
                new String[] {other.getKeyId(), other.getReviewUser(), other.getReviewDate(), other.getReviewDescription()});
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKeyId(), getReviewUser(), getReviewDate(), getReviewDescription());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [keyId=" + keyId + ", reviewUser="
                + reviewUser + ", reviewDate=" + reviewDate + ", reviewDescription=" + reviewDescription + "]";
    }
}
