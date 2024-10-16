package com.artillexstudios.axafkzone.utils;

import com.artillexstudios.axafkzone.reward.Reward;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RandomUtils {
    public static Reward randomValue(@NotNull HashMap<Reward, Double> rewardMap) {
        List<Pair<Reward, Double>> rewardPairs = new ArrayList<>();
        rewardMap.forEach((reward, weight) -> rewardPairs.add(new Pair<>(reward, weight)));

        EnumeratedDistribution<Reward> distribution = new EnumeratedDistribution<>(rewardPairs);

        return distribution.sample();
    }
}
