
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
