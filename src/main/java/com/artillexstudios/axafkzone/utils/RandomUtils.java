package com.artillexstudios.axafkzone.utils;

import com.artillexstudios.axafkzone.reward.Reward;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RandomUtils {
    public static Reward randomValue(@NotNull HashMap<Reward, Double> map) {
        List<Pair<Reward, Double>> list = new ArrayList<>();
        map.forEach((key, value) -> list.add(new Pair<>(key, value)));

        EnumeratedDistribution<Reward> e = new EnumeratedDistribution<>(list);

        return e.sample();
    }
}
