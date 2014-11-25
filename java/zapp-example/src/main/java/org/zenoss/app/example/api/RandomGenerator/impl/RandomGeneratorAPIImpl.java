// Copyright 2014 The Serviced Authors.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package org.zenoss.app.example.api.RandomGenerator.impl;

import com.google.common.base.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.zenoss.app.annotations.API;
import org.zenoss.app.example.api.RandomGenerator.RandomGeneratorAPI;
import org.zenoss.app.example.ExampleAppConfiguration;
import org.zenoss.app.example.api.RandomGenerator.RandomResponse;

import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Example API provider class.
 */
@API //API annotation ensures it is loaded via Spring
public class RandomGeneratorAPIImpl implements RandomGeneratorAPI {


    @Autowired
    ExampleAppConfiguration config;

    private static final Random rand = new Random();

    @Override
    public RandomResponse random(Optional<Integer> min, Optional<Integer> max) {
        checkNotNull(min);
        checkNotNull(max);
        Integer minInt = config.getRandomGeneratorConfig().getDefaultMin();
        Integer maxInt = config.getRandomGeneratorConfig().getDefaultMax();

        minInt = min.or(minInt);
        maxInt = max.or(maxInt);

        checkArgument(minInt >=  0, "min cannot be less than 0");
        checkArgument(minInt < maxInt, "min not les than max");
        Integer val = rand.nextInt(maxInt - minInt) + minInt;
        return new RandomResponse(val, minInt, maxInt);
    }
}
