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


import com.google.common.base.Optional;

public interface RandomGeneratorAPI {

    RandomResponse random(Optional<Integer>min, Optional<Integer>max);

}
