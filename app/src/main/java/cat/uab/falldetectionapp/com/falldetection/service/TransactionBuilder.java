/*  Copyright (C) 2015-2018 Andreas Shimokawa, Carsten Pfeiffer

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package cat.uab.falldetectionapp.com.falldetection.service;

import android.bluetooth.BluetoothGattCharacteristic;
import android.support.annotation.Nullable;


public class TransactionBuilder {

    private final Transaction mTransaction;
    private boolean mQueued;

    public TransactionBuilder(String taskName) {
        mTransaction = new Transaction(taskName);
    }

    public TransactionBuilder read(BluetoothGattCharacteristic characteristic) {
        if (characteristic == null) {
            System.out.println("Unable to read characteristic: null");
            return this;
        }
        ReadAction action = new ReadAction(characteristic);
        return add(action);
    }

    public TransactionBuilder add(BtLEAction action) {
        mTransaction.add(action);
        return this;
    }

    /**
     * Sets a GattCallback instance that will be called when the transaction is executed,
     * resulting in GattCallback events.
     *
     * @param callback the callback to set, may be null
     */
    public void setGattCallback(@Nullable GattCallback callback) {
        mTransaction.setGattCallback(callback);
    }

    public
    @Nullable
    GattCallback getGattCallback() {
        return mTransaction.getGattCallback();
    }

    public Transaction getTransaction() {
        return mTransaction;
    }
}
