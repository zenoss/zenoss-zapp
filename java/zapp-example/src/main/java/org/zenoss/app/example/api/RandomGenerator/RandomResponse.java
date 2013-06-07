/*
 * ****************************************************************************
 *
 *  Copyright (C) Zenoss, Inc. 2013, all rights reserved.
 *
 *  This content is made available according to terms specified in
 *  License.zenoss under the directory where your Zenoss product is installed.
 *
 * ***************************************************************************
 */

package org.zenoss.app.example.api.RandomGenerator;

public final class RandomResponse {

    private int random;
    private int min;
    private int max;

    public RandomResponse(int random, int min, int max) {

        this.random = random;
        this.min = min;
        this.max = max;
    }

    public int getRandom() {
        return random;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
