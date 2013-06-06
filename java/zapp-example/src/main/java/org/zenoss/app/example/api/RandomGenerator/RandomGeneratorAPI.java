package org.zenoss.app.example.api.RandomGenerator;


import com.google.common.base.Optional;

public interface RandomGeneratorAPI {

    RandomResponse random(Optional<Integer>min, Optional<Integer>max);

}
